package com.example.tickets.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@Import(BookingRepository.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createBooking_validRequest_returns201() throws Exception {
        String requestBody = """
                {
                    "eventId": 1,
                    "customerName": "Jane",
                    "tickets": 2
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.customerName").value("Jane"))
                .andExpect(jsonPath("$.tickets").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createBooking_invalidRequest_returns400() throws Exception {
        String requestBody = """
                {
                    "eventId": null,
                    "customerName": "",
                    "tickets": 15
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getBooking_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/bookings/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}
