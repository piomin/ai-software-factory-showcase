package com.example.tickets.event;

import jakarta.validation.constraints.Positive;

public record TicketReleaseRequest(@Positive int tickets) {
}
