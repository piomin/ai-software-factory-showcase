package com.example.tickets.event;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EventReservationRequest(@NotNull @Min(1) @Max(100) int tickets) {
}
