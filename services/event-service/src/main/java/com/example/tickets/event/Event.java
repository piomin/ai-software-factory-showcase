package com.example.tickets.event;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String venue;
    private LocalDate date;

    @Version
    private int version;

    private int availableTickets;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    public Event() {
    }

    public Event(Long id, String name, String venue, LocalDate date, int availableTickets) {
        this(id, name, venue, date, availableTickets, null);
    }

    public Event(Long id, String name, String venue, LocalDate date, int availableTickets, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.venue = venue;
        this.date = date;
        this.availableTickets = availableTickets;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public void setAvailableTickets(int availableTickets) {
        this.availableTickets = availableTickets;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", venue='" + venue + '\'' +
                ", date=" + date +
                ", version=" + version +
                ", availableTickets=" + availableTickets +
                ", price=" + price +
                '}';
    }
}
