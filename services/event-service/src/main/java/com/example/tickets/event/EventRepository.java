package com.example.tickets.event;

import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class EventRepository {

    private final ConcurrentMap<Long, Event> events = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public EventRepository() {
        seedEvents();
    }

    private void seedEvents() {
        events.put(1L, new Event(1L, "Rock Fest 2026", "Main Stadium", LocalDate.of(2026, 6, 15), 5000));
        events.put(2L, new Event(2L, "Jazz Evening", "Blue Note Club", LocalDate.of(2026, 7, 20), 200));
        events.put(3L, new Event(3L, "Classical Symphony", "Concert Hall", LocalDate.of(2026, 8, 10), 800));
    }

    public List<Event> findAll() {
        return new ArrayList<>(events.values());
    }

    public Optional<Event> findById(long id) {
        return Optional.ofNullable(events.get(id));
    }
}
