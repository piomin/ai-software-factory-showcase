package com.example.tickets.booking;

import java.time.LocalDateTime;

public record Booking(Long id, Long eventId, String customerName, Integer tickets, Status status) {

    public enum Status {
        CONFIRMED
    }
}
