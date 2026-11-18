package com.example.tickets.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    void saveBooking_successfullyPersists() {
        Booking booking = new Booking(null, 1L, "Test User", 2, null);
        Booking saved = bookingRepository.save(booking);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEventId()).isEqualTo(1L);
        assertThat(saved.getCustomerName()).isEqualTo("Test User");
        assertThat(saved.getTickets()).isEqualTo(2);
        assertThat(saved.getStatus()).isEqualTo(Booking.Status.CONFIRMED);
    }

    @Test
    void findById_existingBooking_returnsOptional() {
        Booking booking = new Booking(null, 1L, "Test User", 2, null);
        Booking saved = bookingRepository.save(booking);

        Optional<Booking> found = bookingRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getCustomerName()).isEqualTo("Test User");
    }

    @Test
    void findById_nonExistentBooking_returnsEmpty() {
        Optional<Booking> found = bookingRepository.findById(999L);

        assertThat(found).isEmpty();
    }

    @Test
    void deleteBooking_successfullyRemoves() {
        Booking booking = new Booking(null, 1L, "Test User", 2, null);
        Booking saved = bookingRepository.save(booking);
        Long id = saved.getId();

        bookingRepository.delete(saved);

        Optional<Booking> found = bookingRepository.findById(id);
        assertFalse(found.isPresent());
    }
}
