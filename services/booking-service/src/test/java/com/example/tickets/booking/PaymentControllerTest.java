package com.example.tickets.booking;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties =
        "spring.datasource.url=jdbc:h2:mem:paymentctrl;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000")
class PaymentControllerTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-01-01T10:00:00Z");
    private static final LocalDateTime ACTIVE_EXPIRES_AT = LocalDateTime.of(2026, 1, 1, 10, 30);
    private static final LocalDateTime EXPIRED_EXPIRES_AT = LocalDateTime.of(2026, 1, 1, 9, 0);

    private static final String SUCCESS_CARD = "4111111111111234"; // last4 1234
    private static final String DECLINE_CARD = "4000000000000002";  // last4 0002 -> test-decline
    private static final String RETRY_CARD = "5555555555554444";    // last4 4444

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private java.time.Clock clock;

    private MockMvc mockMvc;
    private ListAppender<ILoggingEvent> logAppender;
    private ch.qos.logback.classic.Logger serviceLogbackLogger;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        when(clock.instant()).thenReturn(FIXED_INSTANT);
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);

        serviceLogbackLogger =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(PaymentService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        serviceLogbackLogger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        serviceLogbackLogger.detachAppender(logAppender);
    }

    private long savePendingBooking(String amount, LocalDateTime expiresAt) {
        return bookingRepository.save(bookingEntity(amount, expiresAt)).getId();
    }

    private Booking bookingEntity(String amount, LocalDateTime expiresAt) {
        return new Booking(null, 500L, "Customer", 2, Booking.Status.PENDING_PAYMENT,
                new BigDecimal(amount), LocalDateTime.ofInstant(FIXED_INSTANT, ZoneOffset.UTC), expiresAt);
    }

    private String paymentJson(String cardNumber) {
        return """
                {
                    "cardNumber": "%s",
                    "holderName": "Ada Lovelace",
                    "expiry": "12/28",
                    "cvv": "123"
                }
                """.formatted(cardNumber);
    }

    @Test
    void pay_withValidCard_returns200SuccessAndConfirmsBooking() throws Exception {
        long id = savePendingBooking("30.00", ACTIVE_EXPIRES_AT);
        mockMvc.perform(post("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(SUCCESS_CARD)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.paymentId").isNumber())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(closeTo(30.00, 0.001)))
                .andExpect(jsonPath("$.cardLast4").value("1234"))
                .andExpect(content().string(not(containsString(SUCCESS_CARD))))
                .andExpect(content().string(not(containsString("card declined"))));

        mockMvc.perform(get("/api/bookings/" + id).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void pay_withDeclineCard_returns200DeclinedThenRetrySucceeds() throws Exception {
        long id = savePendingBooking("45.00", ACTIVE_EXPIRES_AT);
        mockMvc.perform(post("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(DECLINE_CARD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"))
                .andExpect(jsonPath("$.reason").value("card declined"))
                .andExpect(jsonPath("$.amount").value(closeTo(45.00, 0.001)))
                .andExpect(jsonPath("$.cardLast4").value("0002"))
                .andExpect(content().string(not(containsString(DECLINE_CARD))));

        mockMvc.perform(get("/api/bookings/" + id).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING_PAYMENT"));

        mockMvc.perform(post("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(RETRY_CARD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.cardLast4").value("4444"));

        mockMvc.perform(get("/api/bookings/" + id).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void pay_concurrentSubmits_chargesOnce() throws Exception {
        Booking booking = bookingRepository.save(bookingEntity("30.00", ACTIVE_EXPIRES_AT));
        long id = booking.getId();
        PaymentRequest request = new PaymentRequest(SUCCESS_CARD, "Ada Lovelace", "12/28", "123");

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        java.util.concurrent.Callable<Throwable> task = () -> {
            start.await();
            try {
                paymentService.pay(id, request);
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

        long successCount = paymentRepository.findByBookingId(id).stream()
                .filter(p -> p.getStatus() == Payment.Status.SUCCESS)
                .count();
        assertThat(successCount).isEqualTo(1);
        List<Throwable> errors = Arrays.asList(firstError, secondError);
        assertThat(errors.stream().filter(Objects::isNull).count()).isEqualTo(1);
        assertThat(errors.stream().filter(Objects::nonNull).findFirst().orElseThrow())
                .isInstanceOf(PaymentConflictException.class);
    }

    @Test
    void pay_bookingAlreadyPaid_returns409AlreadyPaid() throws Exception {
        long id = savePendingBooking("20.00", ACTIVE_EXPIRES_AT);
        paymentRepository.save(new Payment(null, id, new BigDecimal("20.00"),
                Payment.Status.SUCCESS, LocalDateTime.ofInstant(FIXED_INSTANT, ZoneOffset.UTC), "9999"));

        mockMvc.perform(post("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(SUCCESS_CARD)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.title").value("Payment Conflict"))
                .andExpect(jsonPath("$.detail").value("reservation already paid"));
    }

    @Test
    void pay_bookingAlreadyConfirmed_returns409() throws Exception {
        Booking confirmed = bookingRepository.save(new Booking(
                null, 500L, "Customer", 2, Booking.Status.CONFIRMED, new BigDecimal("30.00"),
                LocalDateTime.ofInstant(FIXED_INSTANT, ZoneOffset.UTC), ACTIVE_EXPIRES_AT));

        mockMvc.perform(post("/api/bookings/" + confirmed.getId() + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(SUCCESS_CARD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Payment Conflict"))
                .andExpect(jsonPath("$.detail").value("reservation is already confirmed"));
    }

    @Test
    void pay_expiredPendingBooking_returns409ReservationExpired() throws Exception {
        long id = savePendingBooking("30.00", EXPIRED_EXPIRES_AT);

        mockMvc.perform(post("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(SUCCESS_CARD)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Payment Conflict"))
                .andExpect(jsonPath("$.detail").value("reservation expired"));
    }

    @Test
    void pay_unknownBooking_returns404() throws Exception {
        mockMvc.perform(post("/api/bookings/987654/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(SUCCESS_CARD)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.detail").value("Booking not found for id: 987654"));
    }

    @Test
    void history_unknownBooking_returns404() throws Exception {
        mockMvc.perform(get("/api/bookings/987655/payments").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Booking not found for id: 987655"));
    }

    @Test
    void history_returnsMaskedAttempts() throws Exception {
        long id = savePendingBooking("45.00", ACTIVE_EXPIRES_AT);
        mockMvc.perform(post("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(DECLINE_CARD)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(SUCCESS_CARD)))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(content().string(not(containsString(DECLINE_CARD))))
                .andExpect(content().string(not(containsString(SUCCESS_CARD))))
                .andReturn();

        List<String> statuses = JsonPath.read(result.getResponse().getContentAsString(), "$[*].status");
        List<String> last4 = JsonPath.read(result.getResponse().getContentAsString(), "$[*].cardLast4");
        assertThat(statuses).containsExactlyInAnyOrder("DECLINED", "SUCCESS");
        assertThat(last4).containsExactlyInAnyOrder("0002", "1234");
    }

    @Test
    void pay_blankCardNumber_returns400() throws Exception {
        long id = savePendingBooking("30.00", ACTIVE_EXPIRES_AT);
        mockMvc.perform(post("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson("   ")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pay_invalidCardNumber_returns400WithoutLeakingCard() throws Exception {
        long id = savePendingBooking("30.00", ACTIVE_EXPIRES_AT);
        String tooShort = "1234";
        mockMvc.perform(post("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(tooShort)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.title").value("Invalid Card"))
                .andExpect(content().string(not(containsString(tooShort))));
    }

    @Test
    void pay_missingFields_returns400() throws Exception {
        long id = savePendingBooking("30.00", ACTIVE_EXPIRES_AT);
        String body = """
                {
                    "cardNumber": "4111111111111234"
                }
                """;
        mockMvc.perform(post("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void paymentLogsAndResponses_neverContainMoreThanLast4Digits() throws Exception {
        long id = savePendingBooking("30.00", ACTIVE_EXPIRES_AT);
        mockMvc.perform(post("/api/bookings/" + id + "/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentJson(SUCCESS_CARD)))
                .andExpect(status().isOk());

        assertThat(logAppender.list).isNotEmpty();
        assertThat(logAppender.list)
                .allSatisfy(event -> assertThat(event.getFormattedMessage()).doesNotContain(SUCCESS_CARD));
        assertThat(logAppender.list)
                .anySatisfy(event -> assertThat(event.getFormattedMessage()).contains("1234"));
    }
}
