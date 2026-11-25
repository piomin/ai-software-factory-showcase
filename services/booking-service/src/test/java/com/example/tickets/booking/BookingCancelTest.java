package com.example.tickets.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties =
        "spring.datasource.url=jdbc:h2:mem:cancelctrl;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000")
class BookingCancelTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-01-01T10:00:00Z");
    private static final LocalDateTime NOW = LocalDateTime.ofInstant(FIXED_INSTANT, ZoneOffset.UTC);
    private static final long EVENT_ID = 500L;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingService bookingService;

    @MockitoBean
    private EventServiceClient eventServiceClient;

    @MockitoBean
    private java.time.Clock clock;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        org.mockito.Mockito.when(clock.instant()).thenReturn(FIXED_INSTANT);
        org.mockito.Mockito.when(clock.getZone()).thenReturn(ZoneOffset.UTC);
    }

    private Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    private Booking pending(String amount) {
        return new Booking(null, EVENT_ID, "Customer", 2, Booking.Status.PENDING_PAYMENT,
                new BigDecimal(amount), NOW, NOW.plusMinutes(15));
    }

    private Booking confirmed(String amount) {
        return new Booking(null, EVENT_ID, "Customer", 2, Booking.Status.CONFIRMED,
                new BigDecimal(amount), NOW, NOW.plusMinutes(15));
    }

    private long successPayment(long bookingId, String amount) {
        return paymentRepository.save(new Payment(null, bookingId, new BigDecimal(amount),
                Payment.Status.SUCCESS, NOW, "1234")).getId();
    }

    @Test
    void cancel_pending_releasesTicketsAndCancelsWithoutPayment() throws Exception {
        Booking booking = save(pending("30.00"));

        mockMvc.perform(post("/api/bookings/" + booking.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(booking.getId()))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(eventServiceClient).releaseTickets(EVENT_ID, 2);
        assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getStatus())
                .isEqualTo(Booking.Status.CANCELLED);
        assertThat(paymentRepository.findByBookingId(booking.getId())).isEmpty();
    }

    @Test
    void cancel_confirmed_refundsFullAmountOnce_thenSecondCancelConflict() throws Exception {
        Booking booking = save(confirmed("30.00"));
        long paymentId = successPayment(booking.getId(), "30.00");

        mockMvc.perform(post("/api/bookings/" + booking.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(eventServiceClient).releaseTickets(EVENT_ID, 2);

        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        assertThat(payment.getStatus()).isEqualTo(Payment.Status.REFUNDED);
        assertThat(payment.getAmount()).isEqualByComparingTo("30.00");

        mockMvc.perform(post("/api/bookings/" + booking.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Payment Conflict"))
                .andExpect(jsonPath("$.detail").value("reservation is already cancelled"));

        List<Payment> payments = paymentRepository.findByBookingId(booking.getId());
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getStatus()).isEqualTo(Payment.Status.REFUNDED);
        verify(eventServiceClient, times(1)).releaseTickets(EVENT_ID, 2);
    }

    @Test
    void cancel_confirmedWithAlreadyRefundedPayment_returns409() throws Exception {
        Booking booking = save(confirmed("30.00"));
        paymentRepository.save(new Payment(null, booking.getId(), new BigDecimal("30.00"),
                Payment.Status.REFUNDED, NOW, "1234"));

        mockMvc.perform(post("/api/bookings/" + booking.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Payment Conflict"))
                .andExpect(jsonPath("$.detail").value("payment already refunded"));

        assertThat(bookingRepository.findById(booking.getId()).orElseThrow().getStatus())
                .isEqualTo(Booking.Status.CONFIRMED);
    }

    @Test
    void cancel_confirmedWithoutPayment_releasesTicketsAndCancelsNoPaymentRows() throws Exception {
        Booking legacy = save(new Booking(null, EVENT_ID, "Legacy User", 5, Booking.Status.CONFIRMED));

        mockMvc.perform(post("/api/bookings/" + legacy.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(eventServiceClient).releaseTickets(EVENT_ID, 5);
        assertThat(paymentRepository.findByBookingId(legacy.getId())).isEmpty();
    }

    @Test
    void cancel_expired_returns409ReservationExpired() throws Exception {
        Booking expired = save(new Booking(null, EVENT_ID, "Customer", 2, Booking.Status.EXPIRED,
                new BigDecimal("30.00"), NOW, NOW.minusMinutes(1)));

        mockMvc.perform(post("/api/bookings/" + expired.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Payment Conflict"))
                .andExpect(jsonPath("$.detail").value("reservation is expired"));
    }

    @Test
    void cancel_alreadyCancelled_returns409AlreadyCancelled() throws Exception {
        Booking cancelled = save(new Booking(null, EVENT_ID, "Customer", 2, Booking.Status.CANCELLED,
                new BigDecimal("30.00"), NOW, NOW.plusMinutes(15)));

        mockMvc.perform(post("/api/bookings/" + cancelled.getId() + "/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Payment Conflict"))
                .andExpect(jsonPath("$.detail").value("reservation is already cancelled"));
    }

    @Test
    void cancel_unknownBooking_returns404() throws Exception {
        mockMvc.perform(post("/api/bookings/987654/cancel")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Booking not found for id: 987654"));
    }

    @Test
    void cancel_concurrentDoubleCancel_refundsAndReleasesOnce() throws Exception {
        Booking booking = save(confirmed("30.00"));
        long paymentId = successPayment(booking.getId(), "30.00");
        long id = booking.getId();

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        java.util.concurrent.Callable<Throwable> task = () -> {
            start.await();
            try {
                bookingService.cancel(id);
                return null;
            } catch (RuntimeException ex) {
                return ex;
            }
        };
        Future<Throwable> first = pool.submit(task);
        Future<Throwable> second = pool.submit(task);
        start.countDown();
        Throwable firstError = first.get(15, TimeUnit.SECONDS);
        Throwable secondError = second.get(15, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(bookingRepository.findById(id).orElseThrow().getStatus())
                .isEqualTo(Booking.Status.CANCELLED);
        List<Payment> payments = paymentRepository.findByBookingId(id);
        assertThat(payments).hasSize(1);
        assertThat(payments.get(0).getId()).isEqualTo(paymentId);
        assertThat(payments.get(0).getStatus()).isEqualTo(Payment.Status.REFUNDED);

        List<Throwable> errors = Arrays.asList(firstError, secondError);
        assertThat(errors.stream().filter(Objects::isNull).count()).isEqualTo(1);
        assertThat(errors.stream().filter(Objects::nonNull).findFirst().orElseThrow())
                .isInstanceOf(PaymentConflictException.class);
        verify(eventServiceClient, times(1)).releaseTickets(EVENT_ID, 2);
    }
}
