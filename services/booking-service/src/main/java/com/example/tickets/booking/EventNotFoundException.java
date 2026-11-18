package com.example.tickets.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class EventNotFoundException extends ResponseStatusException {

    public EventNotFoundException(long eventId) {
        super(HttpStatus.NOT_FOUND, "Event not found: " + eventId);
    }
}
