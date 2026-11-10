package com.example.tickets.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bookings")
class BookingController {

    private final BookingRepository bookingRepository;

    BookingController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @PostMapping
    ResponseEntity<Booking> createBooking(@Valid @RequestBody BookingRequest request) {
        Booking booking = bookingRepository.save(new Booking(null, request.eventId(), request.customerName(), request.tickets(), null));
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/bookings/" + booking.id())
                .body(booking);
    }

    @GetMapping("/{id}")
    Booking getBooking(@PathVariable long id) {
        Booking booking = bookingRepository.findById(id).orElse(null);
        return booking != null ? booking : ProblemDetail.forStatus(HttpStatus.NOT_FOUND.value());
    }
}
