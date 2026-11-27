package com.example.tickets.booking;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class EventServiceClientConfiguration {

    @Value("${event-service.url:http://localhost:8081}")
    private String baseUrl;

    @Bean
    EventServiceClient eventServiceClient() {
        return new EventServiceClient(baseUrl);
    }
}
