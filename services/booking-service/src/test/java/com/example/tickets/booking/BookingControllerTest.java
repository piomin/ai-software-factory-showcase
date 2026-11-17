package com.example.tickets.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class BookingControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Test
    void createBooking_validRequest_returns201() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
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
                .andExpect(header().string("Location", "/api/bookings/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.customerName").value("Jane"))
                .andExpect(jsonPath("$.tickets").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createAndGetMapping_validBooking_returns200() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String createRequestBody = """
                {
                    "eventId": 1,
                    "customerName": "Test User",
                    "tickets": 2
                }
                """;
        var result = mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.customerName").value("Test User"))
                .andExpect(jsonPath("$.tickets").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
        
        // Extract the ID from the response and get the booking
        String content = result.andReturn().getResponse().getContentAsString();
        int idStart = content.indexOf("\"id\":") + 5;
        int idEnd = content.indexOf(",", idStart);
        String idStr = content.substring(idStart, idEnd).trim();
        long id = Long.parseLong(idStr);
        
        mockMvc.perform(get("/api/bookings/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.customerName").value("Test User"))
                .andExpect(jsonPath("$.tickets").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void getBooking_notFound_returns404() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(get("/api/bookings/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createBooking_ticketsBelowMin_returns400() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String requestBody = """
                {
                    "eventId": 1,
                    "customerName": "Jane",
                    "tickets": 0
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_ticketsAboveMax_returns400() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String requestBody = """
                {
                    "eventId": 1,
                    "customerName": "Jane",
                    "tickets": 11
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_missingTickets_returns400() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String requestBody = """
                {
                    "eventId": 1,
                    "customerName": "Jane"
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_blankCustomerName_returns400() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String requestBody = """
                {
                    "eventId": 1,
                    "customerName": "   ",
                    "tickets": 2
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_emptyCustomerName_returns400() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String requestBody = """
                {
                    "eventId": 1,
                    "customerName": "",
                    "tickets": 2
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_invalidRequest_returns400() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String requestBody = """
                {
                    "eventId": 1,
                    "customerName": "",
                    "tickets": 15
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_missingCustomerName_returns400() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String requestBody = """
                {
                    "eventId": 1,
                    "tickets": 2
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_missingEventId_returns400() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String requestBody = """
                {
                    "customerName": "Jane",
                    "tickets": 2
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_malformedJson_returns400() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        String requestBody = """
                {
                    "eventId": 1,
                    "customerName": "Jane",
                    "tickets": 2,
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_noBody_returns400() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking_nonNumericId_returns400() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mockMvc.perform(get("/api/bookings/abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
