package com.example.tickets.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(Long id,
                              Long eventId,
                              String customerName,
                              Integer tickets,
                              String status,
                              BigDecimal amount,
                              LocalDateTime expiresAt) {

    static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getEventId(),
                booking.getCustomerName(),
                booking.getTickets(),
                booking.getStatus() != null ? booking.getStatus().name() : null,
                booking.getAmount(),
                booking.getExpiresAt());
    }
}
