package com.example.tickets.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Internal payment simulation (N4): no external gateway is called. A card that is invalid or the
 * documented test-decline number ending in {@code 0002} is declined; every other valid card is
 * approved. Guards and the check-and-insert run inside one pessimistically-locked transaction so
 * concurrent submits cannot double charge (req 3.4). The request DTO is never logged; only the
 * booking id, amount and last four digits are (N1).
 */
@Service
class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private static final String DECLINE_LAST4 = "0002";

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final Clock clock;

    PaymentService(BookingRepository bookingRepository, PaymentRepository paymentRepository, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.clock = clock;
    }

    @Transactional
    PaymentResponse pay(long bookingId, PaymentRequest request) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        guardReservation(booking);

        if (paymentRepository.existsSuccessByBookingId(bookingId)) {
            throw new PaymentConflictException("reservation already paid");
        }

        CardNumber card = CardNumber.of(request.cardNumber());
        Payment.Status status = DECLINE_LAST4.equals(card.last4())
                ? Payment.Status.DECLINED
                : Payment.Status.SUCCESS;

        Payment payment = paymentRepository.save(new Payment(
                null, bookingId, booking.getAmount(), status, LocalDateTime.now(clock), card.last4()));

        if (status == Payment.Status.SUCCESS) {
            booking.setStatus(Booking.Status.CONFIRMED);
        }

        log.info("Payment bookingId={} status={} amount={} cardLast4={}",
                bookingId, status, booking.getAmount(), card.last4());
        return PaymentResponse.from(payment);
    }

    @Transactional(readOnly = true)
    List<PaymentResponse> history(long bookingId) {
        bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        return paymentRepository.findByBookingId(bookingId).stream()
                .map(PaymentResponse::from)
                .toList();
    }

    private void guardReservation(Booking booking) {
        if (booking.getStatus() != Booking.Status.PENDING_PAYMENT) {
            throw new PaymentConflictException(
                    "reservation is already " + booking.getStatus().name().toLowerCase());
        }
        LocalDateTime expiresAt = booking.getExpiresAt();
        if (expiresAt != null && expiresAt.isBefore(LocalDateTime.now(clock))) {
            throw new PaymentConflictException("reservation expired");
        }
    }
}
