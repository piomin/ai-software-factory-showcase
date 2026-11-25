package com.example.tickets.booking;

import jakarta.validation.constraints.NotBlank;

/**
 * Card details for a payment attempt. Card format validation is deliberately kept out of Bean
 * Validation: the shared exception handler echoes rejected values in {@code fieldErrors}, and a
 * rejected full card number would leak through. Only blank checks run here; the number is parsed
 * later through {@link CardNumber}, which keeps just its last four digits (N1).
 */
public record PaymentRequest(@NotBlank String cardNumber,
                             @NotBlank String holderName,
                             @NotBlank String expiry,
                             @NotBlank String cvv) {
}
