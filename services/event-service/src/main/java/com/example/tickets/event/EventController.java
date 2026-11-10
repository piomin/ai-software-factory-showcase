package com.example.tickets.event;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
class EventController {

    private final EventRepository eventRepository;

    EventController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @GetMapping
    List<Event> listEvents() {
        return eventRepository.findAll();
    }

    @GetMapping("/{id}")
    ResponseEntity<Event> getEvent(@PathVariable long id) {
        Event event = eventRepository.findById(id).orElse(null);
        return event != null ? ResponseEntity.ok(event) : ResponseEntity.notFound().build();
    }
}
