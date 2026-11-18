package com.example.tickets.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ProblemDetail;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
class BookingController {

    private final BookingRepository bookingRepository;
    private final EventServiceClient eventServiceClient;

    BookingController(BookingRepository bookingRepository, EventServiceClient eventServiceClient) {
        this.bookingRepository = bookingRepository;
        this.eventServiceClient = eventServiceClient;
    }

    @PostMapping
    ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest request) {
        eventServiceClient.reserveTickets(request.eventId(), request.tickets());
        Booking booking = bookingRepository.save(new Booking(null, request.eventId(), request.customerName(), request.tickets(), Booking.Status.CONFIRMED));
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/bookings/" + booking.getId())
                .body(booking);
    }

    @GetMapping("/{id}")
    ResponseEntity<Booking> getBooking(@PathVariable long id) {
        return bookingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }
}
