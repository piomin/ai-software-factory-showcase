package com.example.tickets.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the expiry sweep (req 5.1/5.2) with a fixed clock at 2026-01-01T10:00:00Z. The
 * repository mock mirrors the semantics of findByStatusAndExpiresAtBefore against an in-memory
 * dataset, so the negative cases prove which bookings the query must (not) return.
 */
@ExtendWith(MockitoExtension.class)
class ExpiryServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 10, 0);

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventServiceClient eventServiceClient;

    private final Clock clock = Clock.fixed(Instant.parse("2026-01-01T10:00:00Z"), ZoneOffset.UTC);

    private ExpiryService expiryService;

    @BeforeEach
    void setUp() {
        expiryService = new ExpiryService(bookingRepository, eventServiceClient, clock);
    }

    private void dataset(Booking... bookings) {
        List<Booking> rows = List.of(bookings);
        when(bookingRepository.findByStatusAndExpiresAtBefore(any(), any())).thenAnswer(invocation -> {
            Booking.Status status = invocation.getArgument(0);
            LocalDateTime cutoff = invocation.getArgument(1);
            return rows.stream()
                    .filter(b -> b.getStatus() == status)
                    .filter(b -> b.getExpiresAt() != null && b.getExpiresAt().isBefore(cutoff))
                    .toList();
        });
    }

    private Booking booking(long id, Booking.Status status, LocalDateTime expiresAt) {
        return new Booking(id, 500L, "Customer", 2, status,
                new BigDecimal("30.00"), NOW.minusMinutes(20), expiresAt);
    }

    @Test
    void pendingBookingPastWindow_releasesTicketsThenSavesExpired() {
        Booking expired = booking(1L, Booking.Status.PENDING_PAYMENT, NOW.minusMinutes(1));
        dataset(expired);

        int expiredCount = expiryService.expireOverdueBookings();

        assertThat(expiredCount).isEqualTo(1);
        verify(bookingRepository).findByStatusAndExpiresAtBefore(Booking.Status.PENDING_PAYMENT, NOW);
        InOrder inOrder = inOrder(eventServiceClient, bookingRepository);
        inOrder.verify(eventServiceClient).releaseTickets(500L, 2);
        ArgumentCaptor<Booking> saved = ArgumentCaptor.forClass(Booking.class);
        inOrder.verify(bookingRepository).save(saved.capture());
        assertThat(saved.getValue().getId()).isEqualTo(1L);
        assertThat(saved.getValue().getStatus()).isEqualTo(Booking.Status.EXPIRED);
        assertThat(expired.getStatus()).isEqualTo(Booking.Status.EXPIRED);
    }

    @Test
    void pendingBookingWithinWindow_isNotTouched() {
        Booking active = booking(2L, Booking.Status.PENDING_PAYMENT, NOW.plusMinutes(10));
        dataset(active);

        int expiredCount = expiryService.expireOverdueBookings();

        assertThat(expiredCount).isZero();
        verify(bookingRepository).findByStatusAndExpiresAtBefore(Booking.Status.PENDING_PAYMENT, NOW);
        verifyNoInteractions(eventServiceClient);
        verify(bookingRepository, never()).save(any());
        assertThat(active.getStatus()).isEqualTo(Booking.Status.PENDING_PAYMENT);
    }

    @Test
    void confirmedBookingPastWindow_isNotTouched() {
        Booking confirmed = booking(3L, Booking.Status.CONFIRMED, NOW.minusMinutes(5));
        dataset(confirmed);

        int expiredCount = expiryService.expireOverdueBookings();

        assertThat(expiredCount).isZero();
        verify(bookingRepository).findByStatusAndExpiresAtBefore(eq(Booking.Status.PENDING_PAYMENT), eq(NOW));
        verifyNoInteractions(eventServiceClient);
        verify(bookingRepository, never()).save(any());
        assertThat(confirmed.getStatus()).isEqualTo(Booking.Status.CONFIRMED);
    }

    @Test
    void releaseFailure503_leavesBookingPendingForNextRun() {
        Booking pending = booking(4L, Booking.Status.PENDING_PAYMENT, NOW.minusMinutes(3));
        dataset(pending);
        doThrow(new ServiceUnavailableException("Event service is unavailable"))
                .when(eventServiceClient).releaseTickets(500L, 2);

        int expiredCount = expiryService.expireOverdueBookings();

        assertThat(expiredCount).isZero();
        verify(eventServiceClient).releaseTickets(500L, 2);
        verify(bookingRepository, never()).save(any());
        assertThat(pending.getStatus()).isEqualTo(Booking.Status.PENDING_PAYMENT);
    }
}
