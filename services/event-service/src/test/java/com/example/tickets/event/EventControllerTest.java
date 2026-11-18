package com.example.tickets.event;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

class EventControllerTest {

    private final EventRepository repository = Mockito.mock(EventRepository.class);
    private final EventController controller = new EventController(repository);

    @Test
    void listEvents_returnsThreeEvents() {
        when(repository.findAll()).thenReturn(List.of(
                new Event(1L, "Rock Fest 2025", "Main Stadium", LocalDate.of(2025, 6, 15), 5000),
                new Event(2L, "Jazz Evening", "Blue Note Club", LocalDate.of(2025, 7, 20), 200),
                new Event(3L, "Classical Symphony", "Concert Hall", LocalDate.of(2025, 8, 10), 800)
        ));

        List<Event> events = controller.listEvents();

        assertThat(events).hasSize(3);
        assertThat(events.get(0).getId()).isEqualTo(1L);
        assertThat(events.get(0).getName()).isEqualTo("Rock Fest 2025");
    }

    @Test
    void getEvent_notFound_returns404() {
        when(repository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> controller.getEvent(999))
                .isInstanceOf(EventNotFoundException.class)
                .hasMessage("Event not found for id: 999");
    }

    @Test
    void getEvent_found_returns200() {
        when(repository.findById(1L)).thenReturn(java.util.Optional.of(
                new Event(1L, "Rock Fest 2025", "Main Stadium", LocalDate.of(2025, 6, 15), 5000)
        ));

        var result = controller.getEvent(1);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
    }
}
