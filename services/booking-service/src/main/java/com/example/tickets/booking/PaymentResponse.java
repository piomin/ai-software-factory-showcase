package com.example.tickets.booking;

import java.math.BigDecimal;

/**
 * Masked view of a persisted payment. Only the last four card digits are exposed (N1); the
 * {@code reason} is derived from the status so no extra column is needed on the payments table.
 */
public record PaymentResponse(long paymentId,
                              String status,
                              BigDecimal amount,
                              String cardLast4,
                              String reason) {

    static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getStatus().name(),
                payment.getAmount(),
                payment.getCardLast4(),
                reasonFor(payment.getStatus()));
    }

    private static String reasonFor(Payment.Status status) {
        return status == Payment.Status.DECLINED ? "card declined" : null;
    }
}
