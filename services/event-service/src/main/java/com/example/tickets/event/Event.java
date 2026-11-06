package com.example.tickets.event;

import java.time.LocalDate;

public record Event(long id, String name, String venue, LocalDate date, int availableTickets) {
}
