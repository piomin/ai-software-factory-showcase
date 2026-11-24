package com.example.tickets.booking;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the payments table created by Flyway (V3) round-trips every {@link Payment} field
 * under {@code ddl-auto=validate}. Uses an isolated database so it never disturbs the other
 * tests' id expectations, and rolls back.
 */
@SpringBootTest
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:paymentrepo;DB_CLOSE_DELAY=-1")
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void allFieldsWithEveryStatus_persistAndLoad() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 2, 1, 9, 30);
        BigDecimal amount = new BigDecimal("120.00");

        for (Payment.Status status : Payment.Status.values()) {
            Payment saved = paymentRepository.save(
                    new Payment(null, 500L, amount, status, createdAt, "1234"));
            assertThat(saved.getId()).isNotNull();
        }
        entityManager.flush();
        entityManager.clear();

        for (Payment.Status status : Payment.Status.values()) {
            Payment loaded = paymentRepository.findByBookingId(500L).stream()
                    .filter(p -> p.getStatus() == status)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("No payment persisted for " + status));
            assertThat(loaded.getBookingId()).isEqualTo(500L);
            assertThat(loaded.getAmount()).isEqualByComparingTo(amount);
            assertThat(loaded.getStatus()).isEqualTo(status);
            assertThat(loaded.getCreatedAt()).isEqualTo(createdAt);
            assertThat(loaded.getCardLast4()).isEqualTo("1234");
        }
    }

    @Test
    void findByBookingId_returnsOnlyMatchingRows() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 2, 1, 10, 0);
        paymentRepository.save(new Payment(null, 600L, new BigDecimal("10.00"),
                Payment.Status.SUCCESS, createdAt, "1111"));
        paymentRepository.save(new Payment(null, 600L, new BigDecimal("20.00"),
                Payment.Status.DECLINED, createdAt, "2222"));
        paymentRepository.save(new Payment(null, 601L, new BigDecimal("30.00"),
                Payment.Status.REFUNDED, createdAt, "3333"));
        entityManager.flush();
        entityManager.clear();

        List<Payment> forBooking600 = paymentRepository.findByBookingId(600L);

        assertThat(forBooking600).hasSize(2)
                .allSatisfy(p -> assertThat(p.getBookingId()).isEqualTo(600L));
        assertThat(paymentRepository.findByBookingId(999L)).isEmpty();
    }

    @Test
    void toStringExposesOnlyLast4_neverFullCardNumber() {
        String fullNumber = "4111111111111234";
        CardNumber card = CardNumber.of(fullNumber);
        Payment payment = paymentRepository.save(new Payment(null, 700L, new BigDecimal("5.00"),
                Payment.Status.SUCCESS, LocalDateTime.of(2026, 2, 1, 12, 0), card.last4()));
        entityManager.flush();
        entityManager.clear();
        Payment loaded = paymentRepository.findById(payment.getId()).orElseThrow();

        assertThat(loaded.toString()).contains("1234").doesNotContain(fullNumber);
    }
}
