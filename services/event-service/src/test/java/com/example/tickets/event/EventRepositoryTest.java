package com.example.tickets.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EventRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    @Test
    void findAll_returnsAllSeededEvents() {
        List<Event> events = eventRepository.findAll();

        assertThat(events).hasSize(3);
        assertThat(events).extracting(Event::getName).containsExactlyInAnyOrder(
                "Rock Fest 2025", "Jazz Evening", "Classical Symphony"
        );
    }

    @Test
    void findById_returnsExistingEvent() {
        Event event = eventRepository.findById(1L).orElse(null);

        assertThat(event).isNotNull();
        assertThat(event.getName()).isEqualTo("Rock Fest 2025");
    }

    @Test
    void findById_returnsNullForNonExistingEvent() {
        Event event = eventRepository.findById(999L).orElse(null);

        assertThat(event).isNull();
    }

    @Test
    void save_savesEventAndGeneratesId() {
        Event newEvent = new Event(null, "Test Event", "Test Venue", LocalDate.of(2025, 12, 25), 100);
        Event saved = eventRepository.save(newEvent);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Test Event");
    }
}
