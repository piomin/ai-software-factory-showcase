package com.example.tickets.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByBookingId(long bookingId);

    boolean existsByBookingIdAndStatus(long bookingId, Payment.Status status);

    default boolean existsSuccessByBookingId(long bookingId) {
        return existsByBookingIdAndStatus(bookingId, Payment.Status.SUCCESS);
    }
}
