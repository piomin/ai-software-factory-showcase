package com.example.tickets.event;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class NotEnoughTicketsException extends ResponseStatusException {

    public NotEnoughTicketsException(int requested, int available) {
        super(HttpStatus.CONFLICT, "Not enough tickets available. Requested: " + requested + ", Available: " + available);
    }
}
