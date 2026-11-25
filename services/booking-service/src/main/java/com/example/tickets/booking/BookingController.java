package com.example.tickets.booking;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/bookings")
class BookingController {

    private static final Duration PAYMENT_WINDOW = Duration.ofMinutes(15);

    private final BookingRepository bookingRepository;
    private final EventServiceClient eventServiceClient;
    private final BookingService bookingService;
    private final Clock clock;

    BookingController(BookingRepository bookingRepository, EventServiceClient eventServiceClient,
                      BookingService bookingService, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.eventServiceClient = eventServiceClient;
        this.bookingService = bookingService;
        this.clock = clock;
    }

    @PostMapping
    ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        eventServiceClient.reserveTickets(request.eventId(), request.tickets());
        EventDetails event = eventServiceClient.getEvent(request.eventId());
        LocalDateTime createdAt = LocalDateTime.now(clock);
        Booking booking = bookingRepository.save(new Booking(
                null,
                request.eventId(),
                request.customerName(),
                request.tickets(),
                Booking.Status.PENDING_PAYMENT,
                event.price().multiply(BigDecimal.valueOf(request.tickets())),
                createdAt,
                createdAt.plus(PAYMENT_WINDOW)));
        return ResponseEntity.status(HttpStatus.CREATED)
                .header("Location", "/api/bookings/" + booking.getId())
                .body(BookingResponse.from(booking));
    }

    @GetMapping("/{id}")
    ResponseEntity<BookingResponse> getBooking(@PathVariable long id) {
        return bookingRepository.findById(id)
                .map(booking -> ResponseEntity.ok(BookingResponse.from(booking)))
                .orElseThrow(() -> new BookingNotFoundException(id));
    }

    @PostMapping("/{id}/cancel")
    ResponseEntity<BookingResponse> cancelBooking(@PathVariable long id) {
        return ResponseEntity.ok(BookingResponse.from(bookingService.cancel(id)));
    }
}
