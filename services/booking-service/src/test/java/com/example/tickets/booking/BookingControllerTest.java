package com.example.tickets.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

@SpringBootTest
@AutoConfigureMockMvc
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookingRepository bookingRepository;

    @BeforeEach
    void setUp() {
        reset(bookingRepository);
    }

    @Test
    void createBooking_validRequest_returns201() throws Exception {
        Booking savedBooking = new Booking(1L, 1L, "Jane", 2, null);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

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

        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void getBooking_existingBooking_returns200() throws Exception {
        Booking existingBooking = new Booking(1L, 1L, "Test User", 2, null);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(existingBooking));

        mockMvc.perform(get("/api/bookings/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.customerName").value("Test User"))
                .andExpect(jsonPath("$.tickets").value(2))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        verify(bookingRepository).findById(1L);
    }

    @Test
    void getBooking_notFound_returns404() throws Exception {
        when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/bookings/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Booking Not Found"));

        verify(bookingRepository).findById(999L);
    }

    @Test
    void createBooking_ticketsBelowMin_returns400() throws Exception {
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Invalid booking request"))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_ticketsAboveMax_returns400() throws Exception {
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.detail").value("Invalid booking request"))
                .andExpect(jsonPath("$.fieldErrors").isArray());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_missingCustomerName_returns400() throws Exception {
        String requestBody = """
                {
                    "eventId": 1,
                    "tickets": 2
                }
                """;

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customerName"));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_missingEventId_returns400() throws Exception {
        String requestBody = """
                {
                    "customerName": "Jane",
                    "tickets": 2
                }
                """;

        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("eventId"));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_blankCustomerName_returns400() throws Exception {
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.title").value("Validation Error"))
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("customerName"));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_malformedJson_returns400() throws Exception {
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

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void createBooking_noBody_returns400() throws Exception {
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void getBooking_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/api/bookings/abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
