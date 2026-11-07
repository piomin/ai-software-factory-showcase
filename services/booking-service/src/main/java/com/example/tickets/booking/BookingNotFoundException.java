package com.example.tickets.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class BookingNotFoundException extends ResponseStatusException {

    public BookingNotFoundException(long id) {
        super(HttpStatus.NOT_FOUND, "Booking not found for id: " + id);
    }
}
