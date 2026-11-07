package com.example.tickets.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class InvalidBookingRequestException extends ResponseStatusException {

    public InvalidBookingRequestException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }
}
