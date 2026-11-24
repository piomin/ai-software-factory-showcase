package com.example.tickets.booking;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Verifies the V2 payment columns and the four-valued status enum round-trip through the
 * real schema created by Flyway (V1 + V2) under {@code ddl-auto=validate}. Uses an isolated
 * database so it never disturbs the controller tests' id expectations, and rolls back.
 */
@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:bookingrepo;DB_CLOSE_DELAY=-1")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void allFourStatusesWithPaymentFields_persistAndLoad() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        LocalDateTime expiresAt = LocalDateTime.of(2026, 1, 1, 11, 0);
        BigDecimal amount = new BigDecimal("49.90");

        for (Booking.Status status : Booking.Status.values()) {
            Booking saved = bookingRepository.save(
                    new Booking(null, 100L, "Customer", 2, status, amount, createdAt, expiresAt));
            assertThat(saved.getId()).isNotNull();
        }
        entityManager.flush();
        entityManager.clear();

        for (Booking.Status status : Booking.Status.values()) {
            Booking loaded = bookingRepository.findAll().stream()
                    .filter(b -> b.getStatus() == status)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No booking persisted for " + status));
            assertThat(loaded.getStatus()).isEqualTo(status);
            assertThat(loaded.getAmount()).isEqualByComparingTo(amount);
            assertThat(loaded.getCreatedAt()).isEqualTo(createdAt);
            assertThat(loaded.getExpiresAt()).isEqualTo(expiresAt);
        }
    }

    @Test
    void legacyConfirmedRowWithoutAmount_loadsWithNullPaymentFields() {
        jdbcTemplate.update(
                "INSERT INTO bookings (id, event_id, customer_name, tickets, status) VALUES (?,?,?,?,?)",
                9001L, 42L, "Legacy User", 5, "CONFIRMED");
        entityManager.clear();

        Optional<Booking> loaded = bookingRepository.findById(9001L);

        assertThat(loaded).isPresent();
        Booking booking = loaded.get();
        assertThat(booking.getStatus()).isEqualTo(Booking.Status.CONFIRMED);
        assertThat(booking.getAmount()).isNull();
        assertThat(booking.getCreatedAt()).isNull();
        assertThat(booking.getExpiresAt()).isNull();
    }

    @Test
    void unknownStatusValue_cannotBeReadBackAsBookingStatus() {
        jdbcTemplate.update(
                "INSERT INTO bookings (id, event_id, customer_name, tickets, status) VALUES (?,?,?,?,?)",
                9002L, 43L, "Bad Row", 1, "NOT_A_REAL_STATUS");
        entityManager.clear();

        assertThrows(Throwable.class, () -> bookingRepository.findById(9002L).orElseThrow());
    }
}
