package com.example.tickets.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class NotEnoughTicketsException extends ResponseStatusException {

    public NotEnoughTicketsException(int tickets) {
        super(HttpStatus.CONFLICT, "Not enough tickets available");
    }
}
