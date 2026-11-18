package com.example.tickets.event;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
        when(repository.findById(999L)).thenReturn(Optional.empty());

        var result = controller.getEvent(999);

        assertThat(result.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void reserveTickets_success_returns201() {
        Event eventBefore = new Event(1L, "Rock Fest 2025", "Main Stadium", LocalDate.of(2025, 6, 15), 5000);
        Event eventAfter = new Event(1L, "Rock Fest 2025", "Main Stadium", LocalDate.of(2025, 6, 15), 4998);
        
        when(repository.findById(1L)).thenReturn(Optional.of(eventBefore));
        when(repository.decreaseAvailableTickets(1L, 2)).thenReturn(1);
        when(repository.findById(1L)).thenReturn(Optional.of(eventAfter));

        var result = controller.reserveTickets(1L, new EventReservationRequest(2));

        assertThat(result.getStatusCode().value()).isEqualTo(201);
        assertThat(result.getBody().ticketsReserved()).isEqualTo(2);
        assertThat(result.getBody().availableTickets()).isEqualTo(4998);
    }

    @Test
    void reserveTickets_eventNotFound_throws404() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        try {
            controller.reserveTickets(999L, new EventReservationRequest(2));
        } catch (EventNotFoundException ex) {
            assertThat(ex.getStatusCode().value()).isEqualTo(404);
        }
    }

    @Test
    void reserveTickets_notEnoughTickets_throws409() {
        Event event = new Event(1L, "Rock Fest 2025", "Main Stadium", LocalDate.of(2025, 6, 15), 5);
        
        when(repository.findById(1L)).thenReturn(Optional.of(event));
        when(repository.decreaseAvailableTickets(1L, 10)).thenReturn(0);

        try {
            controller.reserveTickets(1L, new EventReservationRequest(10));
        } catch (NotEnoughTicketsException ex) {
            assertThat(ex.getStatusCode().value()).isEqualTo(409);
        }
    }
}