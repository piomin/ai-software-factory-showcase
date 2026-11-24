package com.example.tickets.event;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EventPricePersistenceTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void priceRoundTrip_isReadBackWithTwoDecimals() {
        Event event = new Event(null, "Tech Meetup", "Expo Hall", LocalDate.of(2026, 3, 1), 100, new BigDecimal("12.5"));

        Event saved = eventRepository.saveAndFlush(event);
        entityManager.clear();

        Event found = eventRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getPrice()).isEqualByComparingTo("12.50");
        assertThat(found.getPrice().scale()).isEqualTo(2);
        assertThat(found.getPrice()).hasToString("12.50");
    }

    @Test
    @Transactional
    void invalidPriceScale_isRoundedToColumnScale() {
        Event event = new Event(null, "Comedy Night", "Tiny Club", LocalDate.of(2026, 4, 1), 50, new BigDecimal("19.999"));

        Event saved = eventRepository.saveAndFlush(event);
        entityManager.clear();

        Event found = eventRepository.findById(saved.getId()).orElseThrow();

        assertThat(found.getPrice()).isEqualByComparingTo("20.00");
        assertThat(found.getPrice().scale()).isEqualTo(2);
    }

    @Test
    @Transactional
    void migrationBackfillsExistingEventsWithDefaultPrice() {
        List<Event> events = eventRepository.findAll();

        assertThat(events).isNotEmpty();
        assertThat(events).allSatisfy(event -> {
            assertThat(event.getPrice()).isEqualByComparingTo("50.00");
            assertThat(event.getPrice().scale()).isEqualTo(2);
        });
    }
}
