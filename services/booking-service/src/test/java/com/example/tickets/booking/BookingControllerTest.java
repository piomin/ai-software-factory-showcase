package com.example.tickets.booking;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingRepository bookingRepository;

    @Test
    void createBooking_validRequest_returns201() throws Exception {
        Booking booking = new Booking(1L, 1L, "Jane", 2, Booking.Status.CONFIRMED);
        when(bookingRepository.save(Mockito.any(Booking.class))).thenReturn(booking);

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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.customerName").value("Jane"))
                .andExpect(jsonPath("$.tickets").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createBooking_invalidRequest_returns400ProblemDetail() throws Exception {
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
                .andExpect(status().is4xxClientError())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void getBooking_notFound_returns404() throws Exception {
        when(bookingRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/bookings/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Booking Not Found"));
    }

    @Test
    void getBooking_found_returnsBooking() throws Exception {
        String createRequestBody = """
                {
                    "eventId": 1,
                    "customerName": "Jane",
                    "tickets": 2
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestBody))
                .andReturn();
        
        mockMvc.perform(get("/api/bookings/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.customerName").value("Jane"))
                .andExpect(jsonPath("$.tickets").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
