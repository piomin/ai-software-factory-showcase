package com.example.tickets.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=none")
class TicketReleaseEndpointTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockitoBean
    private EventRepository eventRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void releaseTickets_validRequest_returns204AndRaisesAvailability() throws Exception {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(
                new Event(1L, "Rock Fest 2025", "Main Stadium", LocalDate.of(2025, 6, 15), 4998)
        ));
        when(eventRepository.increaseAvailableTickets(1L, 2)).thenReturn(1);

        String requestBody = """
                {
                    "tickets": 2
                }
                """;
        mockMvc.perform(post("/api/events/1/ticket-release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNoContent());

        verify(eventRepository).increaseAvailableTickets(1L, 2);
    }

    @Test
    void releaseTickets_unknownEvent_returns404ProblemDetail() throws Exception {
        when(eventRepository.findById(999L)).thenReturn(Optional.empty());

        String requestBody = """
                {
                    "tickets": 2
                }
                """;
        mockMvc.perform(post("/api/events/999/ticket-release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Event Not Found"))
                .andExpect(jsonPath("$.detail").value("Event not found for id: 999"));
    }

    @Test
    void releaseTickets_zeroTickets_returns400() throws Exception {
        String requestBody = """
                {
                    "tickets": 0
                }
                """;
        mockMvc.perform(post("/api/events/1/ticket-release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void releaseTickets_negativeTickets_returns400() throws Exception {
        String requestBody = """
                {
                    "tickets": -3
                }
                """;
        mockMvc.perform(post("/api/events/1/ticket-release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
