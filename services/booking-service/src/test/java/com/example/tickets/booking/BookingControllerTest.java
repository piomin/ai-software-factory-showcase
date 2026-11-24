package com.example.tickets.booking;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.matchesRegex;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class BookingControllerTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-01-01T10:15:30Z");
    private static final String FIXED_CREATED_AT = "2026-01-01T10:15:30";
    private static final String FIXED_EXPIRES_AT = "2026-01-01T10:30:30";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BookingRepository bookingRepository;

    @MockitoBean
    private EventServiceClient eventServiceClient;

    @MockitoBean
    private java.time.Clock clock;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private void stubEvent(long eventId, String name, String price) {
        when(eventServiceClient.getEvent(eventId))
                .thenReturn(new EventDetails(eventId, name, new BigDecimal(price)));
    }

    @Test
    void createBooking_validRequest_returns201WithPendingPaymentAndAmount() throws Exception {
        stubEvent(1L, "Rock Concert", "49.90");
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
                .andExpect(header().string("Location", matchesRegex("/api/bookings/\\d+")))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.customerName").value("Jane"))
                .andExpect(jsonPath("$.tickets").value(2))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.amount").value(closeTo(99.80, 0.001)))
                .andExpect(jsonPath("$.expiresAt").value(FIXED_EXPIRES_AT));
        verify(eventServiceClient).getEvent(1L);
    }

    @Test
    void createBooking_reservesTicketsOnEventService() throws Exception {
        stubEvent(7L, "Jazz Night", "20.00");
        String requestBody = """
                {
                    "eventId": 7,
                    "customerName": "John",
                    "tickets": 3
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
        verify(eventServiceClient).reserveTickets(7L, 3);
    }

    @Test
    void createBooking_eventNotFound_returns404AndSavesNothing() throws Exception {
        when(eventServiceClient.getEvent(99L)).thenThrow(new EventNotFoundException(99L));
        long bookingsBefore = bookingRepository.count();
        String requestBody = """
                {
                    "eventId": 99,
                    "customerName": "Jane",
                    "tickets": 2
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Event Not Found"))
                .andExpect(jsonPath("$.detail").value("Event not found: 99"));
        assertThat(bookingRepository.count()).isEqualTo(bookingsBefore);
    }

    @Test
    void createBooking_eventServiceUnavailable_returns503AndSavesNothing() throws Exception {
        when(eventServiceClient.getEvent(98L))
                .thenThrow(new ServiceUnavailableException("Event service is unavailable"));
        long bookingsBefore = bookingRepository.count();
        String requestBody = """
                {
                    "eventId": 98,
                    "customerName": "Jane",
                    "tickets": 2
                }
                """;
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Service Unavailable"));
        assertThat(bookingRepository.count()).isEqualTo(bookingsBefore);
    }

    @Test
    void createAndGetMapping_validBooking_returns200() throws Exception {
        stubEvent(1L, "Rock Concert", "49.90");
        String createRequestBody = """
                {
                    "eventId": 1,
                    "customerName": "Test User",
                    "tickets": 2
                }
                """;
        MvcResult result = mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.customerName").value("Test User"))
                .andExpect(jsonPath("$.tickets").value(2))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.amount").value(closeTo(99.80, 0.001)))
                .andExpect(jsonPath("$.expiresAt").value(FIXED_EXPIRES_AT))
                .andReturn();

        long id = ((Number) JsonPath.read(
                result.getResponse().getContentAsString(), "$.id")).longValue();

        mockMvc.perform(get("/api/bookings/" + id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.eventId").value(1))
                .andExpect(jsonPath("$.customerName").value("Test User"))
                .andExpect(jsonPath("$.tickets").value(2))
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.amount").value(closeTo(99.80, 0.001)))
                .andExpect(jsonPath("$.expiresAt").value(FIXED_EXPIRES_AT));
    }

    @Test
    void getBooking_legacyConfirmedWithoutAmount_returns200() throws Exception {
        Booking legacy = bookingRepository.save(
                new Booking(null, 42L, "Legacy User", 5, Booking.Status.CONFIRMED));

        mockMvc.perform(get("/api/bookings/" + legacy.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(legacy.getId()))
                .andExpect(jsonPath("$.eventId").value(42))
                .andExpect(jsonPath("$.customerName").value("Legacy User"))
                .andExpect(jsonPath("$.tickets").value(5))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.amount").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.expiresAt").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void getBooking_notFound_returns404() throws Exception {
        mockMvc.perform(get("/api/bookings/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Booking Not Found"))
                .andExpect(jsonPath("$.detail").value("Booking not found for id: 999"));
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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
    }

    @Test
    void createBooking_missingTickets_returns400() throws Exception {
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
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBooking_nonNumericId_returns400() throws Exception {
        mockMvc.perform(get("/api/bookings/abc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
