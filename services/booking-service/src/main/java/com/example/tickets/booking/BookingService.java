package com.example.tickets.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cancels a reservation and returns its tickets to the event service (req 4.1-4.3). A pending
 * reservation releases tickets and moves straight to CANCELLED with no money moved; a confirmed
 * reservation additionally refunds its SUCCESS payment in full by flipping the internal Payment
 * record to REFUNDED (same amount/currency, no external gateway). The whole flow runs inside a
 * pessimistically-locked transaction so concurrent cancels on the same booking serialize and a
 * payment can never be refunded twice (req 4.3).
 */
@Service
class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final EventServiceClient eventServiceClient;

    BookingService(BookingRepository bookingRepository, PaymentRepository paymentRepository,
                   EventServiceClient eventServiceClient) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.eventServiceClient = eventServiceClient;
    }

    @Transactional
    Booking cancel(long bookingId) {
        Booking booking = bookingRepository.findByIdForUpdate(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        switch (booking.getStatus()) {
            case CANCELLED -> throw new PaymentConflictException("reservation is already cancelled");
            case EXPIRED -> throw new PaymentConflictException("reservation is expired");
            case PENDING_PAYMENT -> {
                releaseTickets(booking);
                booking.setStatus(Booking.Status.CANCELLED);
                log.info("Cancelled pending bookingId={} (no refund)", bookingId);
            }
            case CONFIRMED -> cancelConfirmed(booking);
        }
        return booking;
    }

    private void cancelConfirmed(Booking booking) {
        long bookingId = booking.getId();
        List<Payment> refunded = paymentRepository.findByBookingIdAndStatus(bookingId, Payment.Status.REFUNDED);
        if (!refunded.isEmpty()) {
            throw new PaymentConflictException("payment already refunded");
        }

        List<Payment> payments = paymentRepository.findByBookingIdAndStatus(bookingId, Payment.Status.SUCCESS);
        if (payments.isEmpty()) {
            // Legacy confirmed reservation (Assumption 4): no payment record, release and cancel only.
            releaseTickets(booking);
            booking.setStatus(Booking.Status.CANCELLED);
            log.info("Cancelled legacy confirmed bookingId={} (no payment record)", bookingId);
            return;
        }

        Payment payment = payments.get(0);
        payment.setStatus(Payment.Status.REFUNDED);
        releaseTickets(booking);
        booking.setStatus(Booking.Status.CANCELLED);
        log.info("Refunded bookingId={} amount={} (full refund)", bookingId, payment.getAmount());
    }

    private void releaseTickets(Booking booking) {
        eventServiceClient.releaseTickets(booking.getEventId(), booking.getTickets());
    }
}
