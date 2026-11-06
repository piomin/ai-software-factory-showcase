package com.example.tickets.event;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@Import(EventRepository.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listEvents_returnsThreeEvents() throws Exception {
        mockMvc.perform(get("/api/events")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Rock Fest 2026"));
    }

    @Test
    void getEvent_notFound_returns404ProblemDetail() throws Exception {
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
