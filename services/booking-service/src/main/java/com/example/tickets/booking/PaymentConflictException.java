package com.example.tickets.booking;

/**
 * Raised when a payment attempt cannot proceed because of the reservation's current state
 * (already confirmed/cancelled/expired, past its payment window, or already paid). Mapped to
 * HTTP 409 with the given message as the RFC 9457 detail.
 */
public class PaymentConflictException extends RuntimeException {

    public PaymentConflictException(String message) {
        super(message);
    }
}
