package com.example.tickets.booking;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;
    private String customerName;
    private Integer tickets;
    @Enumerated(EnumType.STRING)
    private Status status;

    public Booking() {
    }

    public Booking(Long id, Long eventId, String customerName, Integer tickets, Status status) {
        this.id = id;
        this.eventId = eventId;
        this.customerName = customerName;
        this.tickets = tickets;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getTickets() {
        return tickets;
    }

    public void setTickets(Integer tickets) {
        this.tickets = tickets;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", customerName='" + customerName + '\'' +
                ", tickets=" + tickets +
                ", status=" + status +
                '}';
    }

    public enum Status {
        CONFIRMED
    }
}
