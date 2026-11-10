package com.example.tickets.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import jakarta.persistence.EntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void save_savesBookingAndGeneratesId() {
        Booking booking = new Booking(null, 1L, "John Doe", 2, Booking.Status.CONFIRMED);
        Booking saved = bookingRepository.save(booking);
        entityManager.flush();

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEventId()).isEqualTo(1L);
        assertThat(saved.getCustomerName()).isEqualTo("John Doe");
        assertThat(saved.getTickets()).isEqualTo(2);
        assertThat(saved.getStatus()).isEqualTo(Booking.Status.CONFIRMED);
    }

    @Test
    void findById_returnsExistingBooking() {
        Booking booking = new Booking(null, 1L, "Jane Doe", 3, Booking.Status.CONFIRMED);
        Booking saved = bookingRepository.save(booking);
        entityManager.flush();

        Booking found = bookingRepository.findById(saved.getId()).orElse(null);

        assertThat(found).isNotNull();
        assertThat(found.getCustomerName()).isEqualTo("Jane Doe");
    }

    @Test
    void findById_returnsNullForNonExistingBooking() {
        Booking booking = bookingRepository.findById(999L).orElse(null);

        assertThat(booking).isNull();
    }
}
