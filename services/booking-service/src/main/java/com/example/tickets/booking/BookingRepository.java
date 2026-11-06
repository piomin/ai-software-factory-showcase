package com.example.tickets.booking;

import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class BookingRepository {

    private final ConcurrentMap<Long, Booking> bookings = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Booking save(Booking booking) {
        long id = idGenerator.getAndIncrement();
        Booking saved = new Booking(id, booking.eventId(), booking.customerName(), booking.tickets(), Booking.Status.CONFIRMED);
        bookings.put(id, saved);
        return saved;
    }

    public Optional<Booking> findById(long id) {
        return Optional.ofNullable(bookings.get(id));
    }
}
