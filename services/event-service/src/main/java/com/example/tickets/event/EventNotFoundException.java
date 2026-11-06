package com.example.tickets.event;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class EventNotFoundException extends ResponseStatusException {

    public EventNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "Event not found for id: " + id);
    }
}
