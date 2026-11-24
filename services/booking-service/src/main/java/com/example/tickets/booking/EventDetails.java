package com.example.tickets.booking;

import java.math.BigDecimal;

public record EventDetails(long id, String name, BigDecimal price) {
}
