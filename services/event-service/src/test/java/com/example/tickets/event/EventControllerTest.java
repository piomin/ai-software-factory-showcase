package com.example.tickets.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventRepository eventRepository;

    @Test
    void listEvents_returnsThreeEvents() throws Exception {
        List<Event> events = List.of(
                new Event(1L, "Rock Fest 2025", "Main Stadium", LocalDate.of(2025, 6, 15), 5000),
                new Event(2L, "Jazz Evening", "Blue Note Club", LocalDate.of(2025, 7, 20), 200),
                new Event(3L, "Classical Symphony", "Concert Hall", LocalDate.of(2025, 8, 10), 800)
        );
        when(eventRepository.findAll()).thenReturn(events);

        mockMvc.perform(get("/api/events")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Rock Fest 2026"));
    }

    @Test
    void getEvent_notFound_returns404() throws Exception {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/events/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Event Not Found"));
    }

    @Test
    void getEvent_found_returnsEvent() throws Exception {
        mockMvc.perform(get("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Rock Fest 2026"))
                .andExpect(jsonPath("$.venue").value("Main Stadium"))
                .andExpect(jsonPath("$.date").value("2026-06-15"))
                .andExpect(jsonPath("$.availableTickets").value(5000));
    }
}
