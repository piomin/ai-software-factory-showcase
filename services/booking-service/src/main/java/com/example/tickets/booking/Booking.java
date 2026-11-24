package com.example.tickets.booking;

import jakarta.persistence.*;
import java.math.BigDecimal;
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

    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public Booking() {
    }

    public Booking(Long id, Long eventId, String customerName, Integer tickets, Status status) {
        this(id, eventId, customerName, tickets, status, null, null, null);
    }

    public Booking(Long id, Long eventId, String customerName, Integer tickets, Status status,
                   BigDecimal amount, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.id = id;
        this.eventId = eventId;
        this.customerName = customerName;
        this.tickets = tickets;
        this.status = status != null ? status : Status.CONFIRMED;
        this.amount = amount;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", customerName='" + customerName + '\'' +
                ", tickets=" + tickets +
                ", status=" + status +
                ", amount=" + amount +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
    }

    public enum Status {
        PENDING_PAYMENT,
        CONFIRMED,
        CANCELLED,
        EXPIRED
    }
}
