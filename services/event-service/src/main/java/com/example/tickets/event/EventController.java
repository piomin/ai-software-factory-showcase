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

    @PostMapping("/{id}/reservations")
    ResponseEntity<EventReservationResponse> reserveTickets(@PathVariable long id, @RequestBody EventReservationRequest request) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
        int updated = eventRepository.decreaseAvailableTickets(id, request.tickets());
        if (updated == 0) {
            throw new NotEnoughTicketsException(request.tickets(), event.getAvailableTickets());
        }
        event = eventRepository.findById(id).orElseThrow(() -> new EventNotFoundException(id));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new EventReservationResponse(request.tickets(), event.getAvailableTickets()));
    }

    private int getAvailableTickets(long id) {
        return eventRepository.findById(id)
                .map(Event::getAvailableTickets)
                .orElse(0);
    }
}
