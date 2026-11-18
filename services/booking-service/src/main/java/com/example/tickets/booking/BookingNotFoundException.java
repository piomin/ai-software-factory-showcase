package com.example.tickets.booking;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(long id) {
        super("Booking not found for id: " + id);
    }
}
