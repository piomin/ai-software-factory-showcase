package com.example.tickets.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAll();

    @Modifying
    @Transactional
    @Query("UPDATE Event e SET e.availableTickets = e.availableTickets - :tickets WHERE e.id = :id AND e.availableTickets >= :tickets")
    int decreaseAvailableTickets(long id, int tickets);
}
