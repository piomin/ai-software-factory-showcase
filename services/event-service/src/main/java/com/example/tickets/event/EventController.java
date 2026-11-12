package com.example.tickets.event;

import org.springframework.http.HttpStatus;
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
        return eventRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
