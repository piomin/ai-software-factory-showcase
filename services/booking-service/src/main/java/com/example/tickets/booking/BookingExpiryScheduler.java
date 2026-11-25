package com.example.tickets.booking;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs the expiry sweep every minute (req 5.1/5.2); {@code fixedDelay} keeps a slow run from
 * overlapping the next one.
 */
@Component
class BookingExpiryScheduler {

    private final ExpiryService expiryService;

    BookingExpiryScheduler(ExpiryService expiryService) {
        this.expiryService = expiryService;
    }

    @Scheduled(fixedDelay = 60_000)
    void expireOverdueBookings() {
        expiryService.expireOverdueBookings();
    }
}
