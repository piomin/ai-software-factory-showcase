package com.example.tickets.booking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findById(long id);

    /**
     * Bookings in the given status whose payment window ended before the cutoff; used by the
     * expiry job to release reservations whose PENDING_PAYMENT window has passed (req 5.1).
     */
    List<Booking> findByStatusAndExpiresAtBefore(Booking.Status status, LocalDateTime cutoff);

    /**
     * Loads a booking with a pessimistic write lock so concurrent payment submits on the same
     * booking serialize, guaranteeing at most one SUCCESS payment (idempotency, req 3.4).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findByIdForUpdate(@Param("id") long id);
}
