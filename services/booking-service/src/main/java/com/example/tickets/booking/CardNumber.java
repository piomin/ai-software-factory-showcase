package com.example.tickets.booking;

/**
 * Validates a full card number and keeps only its last four digits (N1: the full number is
 * never stored, so it can never leak through fields, {@code toString()} or logging).
 */
public final class CardNumber {

    private static final int MIN_LENGTH = 12;
    private static final int MAX_LENGTH = 19;
    private static final String MASK_PREFIX = "**** **** **** ";

    private final String last4;

    private CardNumber(String last4) {
        this.last4 = last4;
    }

    public static CardNumber of(String cardNumber) {
        if (cardNumber == null) {
            throw new IllegalArgumentException("Card number must not be null");
        }
        if (cardNumber.length() < MIN_LENGTH || cardNumber.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Card number must be between " + MIN_LENGTH + " and " + MAX_LENGTH + " digits");
        }
        for (int i = 0; i < cardNumber.length(); i++) {
            char c = cardNumber.charAt(i);
            if (c < '0' || c > '9') {
                throw new IllegalArgumentException("Card number must contain digits only");
            }
        }
        return new CardNumber(cardNumber.substring(cardNumber.length() - 4));
    }

    public String last4() {
        return last4;
    }

    @Override
    public String toString() {
        return MASK_PREFIX + last4;
    }
}
