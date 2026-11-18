package com.example.tickets.event;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(long id) {
        super("Event not found for id: " + id);
    }
}
