package com.example.tickets.booking;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BookingRequest(@NotNull Long eventId,
                             @NotBlank String customerName,
                             @NotNull @Min(1) @Max(10) Integer tickets) {
}
