package com.example.tickets.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
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
    ResponseEntity<Object> createBooking(@Valid @RequestBody BookingRequest request) {
        try {
            Booking booking = bookingRepository.save(new Booking(null, request.eventId(), request.customerName(), request.tickets(), null));
            return new ResponseEntity<>(booking, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ProblemDetail.forStatus(HttpStatus.BAD_REQUEST.value()));
        }
    }

    @GetMapping("/{id}")
    Object getBooking(@PathVariable long id) {
        return bookingRepository.findById(id)
                .<Object>map(booking -> booking)
                .orElseGet(() -> ProblemDetail.forStatus(HttpStatus.NOT_FOUND.value()));
    }
}
