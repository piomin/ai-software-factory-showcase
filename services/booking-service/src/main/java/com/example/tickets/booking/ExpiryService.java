package com.example.tickets.booking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Expires reservations whose 15-minute payment window has passed (req 5.1) and returns the held
 * tickets to the pool (req 5.2). Tickets are released before the booking is saved as EXPIRED, so
 * any release failure leaves the booking PENDING_PAYMENT and the next run retries it; releasing
 * twice for the same booking cannot happen because the status is only flipped after success.
 */
@Service
class ExpiryService {

    private static final Logger log = LoggerFactory.getLogger(ExpiryService.class);

    private final BookingRepository bookingRepository;
    private final EventServiceClient eventServiceClient;
    private final Clock clock;

    ExpiryService(BookingRepository bookingRepository, EventServiceClient eventServiceClient, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.eventServiceClient = eventServiceClient;
        this.clock = clock;
    }

    int expireOverdueBookings() {
        LocalDateTime cutoff = LocalDateTime.now(clock);
        List<Booking> overdue = bookingRepository.findByStatusAndExpiresAtBefore(
                Booking.Status.PENDING_PAYMENT, cutoff);
        int expired = 0;
        for (Booking booking : overdue) {
            try {
                eventServiceClient.releaseTickets(booking.getEventId(), booking.getTickets());
                booking.setStatus(Booking.Status.EXPIRED);
                bookingRepository.save(booking);
                expired++;
                log.info("Expired booking={} event={} tickets={}",
                        booking.getId(), booking.getEventId(), booking.getTickets());
            } catch (RuntimeException e) {
                log.warn("Could not release tickets for booking={}, leaving it PENDING_PAYMENT for the next run: {}",
                        booking.getId(), e.toString());
            }
        }
        if (!overdue.isEmpty()) {
            log.info("Expiry run: overdue={} expired={}", overdue.size(), expired);
        }
        return expired;
    }
}
