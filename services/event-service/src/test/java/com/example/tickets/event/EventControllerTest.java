package com.example.tickets.event;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class EventControllerTest {

    @Test
    void listEvents_returnsThreeEvents() {
        EventRepository repository = Mockito.mock(EventRepository.class);
        when(repository.findAll()).thenReturn(List.of(
                new Event(1L, "Rock Fest 2025", "Main Stadium", LocalDate.of(2025, 6, 15), 5000),
                new Event(2L, "Jazz Evening", "Blue Note Club", LocalDate.of(2025, 7, 20), 200),
                new Event(3L, "Classical Symphony", "Concert Hall", LocalDate.of(2025, 8, 10), 800)
        ));
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(new Event(1L, "Rock Fest 2025", "Main Stadium", LocalDate.of(2025, 6, 15), 5000)));
        when(repository.findById(2L)).thenReturn(java.util.Optional.of(new Event(2L, "Jazz Evening", "Blue Note Club", LocalDate.of(2025, 7, 20), 200)));
        when(repository.findById(3L)).thenReturn(java.util.Optional.of(new Event(3L, "Classical Symphony", "Concert Hall", LocalDate.of(2025, 8, 10), 800)));
        when(repository.findById(999L)).thenReturn(java.util.Optional.empty());

        EventController controller = new EventController(repository);

        List<Event> events = controller.listEvents();

        assertThat(events).hasSize(3);
        assertThat(events.get(0).getId()).isEqualTo(1L);
        assertThat(events.get(0).getName()).isEqualTo("Rock Fest 2025");
    }

    @Test
    void getEvent_notFound_returns404() {
        EventRepository repository = Mockito.mock(EventRepository.class);
        when(repository.findById(999L)).thenReturn(java.util.Optional.empty());

        EventController controller = new EventController(repository);

        var result = controller.getEvent(999);

        assertThat(result.getStatusCode().value()).isEqualTo(404);
    }
}
