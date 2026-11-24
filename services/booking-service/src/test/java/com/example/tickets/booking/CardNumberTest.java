package com.example.tickets.booking;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CardNumberTest {

    @Test
    void extractsLast4AtBoundaryLengths() {
        assertThat(CardNumber.of("123456789012").last4()).isEqualTo("9012");
        assertThat(CardNumber.of("1234567890123456789").last4()).isEqualTo("6789");
    }

    @Test
    void toStringMasksEverythingButLast4() {
        CardNumber card = CardNumber.of("4111111111111234");

        assertThat(card.toString()).isEqualTo("**** **** **** 1234");
    }

    @Test
    void toStringNeverContainsFullCardNumber() {
        String fullNumber = "4111111111111234";

        assertThat(CardNumber.of(fullNumber).toString()).doesNotContain(fullNumber);
        assertThat(new Payment(null, 1L, null, null, null, CardNumber.of(fullNumber).last4())
                .toString()).doesNotContain(fullNumber);
    }

    @Test
    void nullCardNumberIsRejected() {
        assertThatThrownBy(() -> CardNumber.of(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not be null");
    }

    @Test
    void nonDigitInputIsRejected() {
        assertThatThrownBy(() -> CardNumber.of("4111 1111 1111 1234"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("digits only");
    }

    @Test
    void tooShortInputIsRejected() {
        assertThatThrownBy(() -> CardNumber.of("12345678901"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 12 and 19");
    }

    @Test
    void tooLongInputIsRejected() {
        assertThatThrownBy(() -> CardNumber.of("12345678901234567890"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 12 and 19");
    }
}
