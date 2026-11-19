package com.example.tickets.gateway;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingTest {

    private static final HttpServer EVENT_UPSTREAM;
    private static final HttpServer BOOKING_UPSTREAM;

    static {
        try {
            EVENT_UPSTREAM = startUpstream("event");
            BOOKING_UPSTREAM = startUpstream("booking");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to start test upstreams", e);
        }
    }

    @LocalServerPort
    private int port;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @AfterAll
    static void stopUpstreams() {
        EVENT_UPSTREAM.stop(0);
        BOOKING_UPSTREAM.stop(0);
    }

    @DynamicPropertySource
    static void upstreamUrls(DynamicPropertyRegistry registry) {
        registry.add("EVENT_SERVICE_URL", () -> "http://localhost:" + EVENT_UPSTREAM.getAddress().getPort());
        registry.add("BOOKING_SERVICE_URL", () -> "http://localhost:" + BOOKING_UPSTREAM.getAddress().getPort());
    }

    @Test
    void routesEventRequestsToEventService() {
        client.get().uri("/api/events")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("event:/api/events");
    }

    @Test
    void routesBookingRequestsToBookingService() {
        client.get().uri("/api/bookings/42")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("booking:/api/bookings/42");
    }

    @Test
    void returnsNotFoundWhenNoRouteMatches() {
        client.get().uri("/api/unknown")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void exposesHealthEndpoint() {
        client.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("UP");
    }

    private static HttpServer startUpstream(String prefix) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            String marker = prefix + ":" + exchange.getRequestURI().getPath();
            byte[] bytes = marker.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream body = exchange.getResponseBody()) {
                body.write(bytes);
            }
        });
        server.start();
        return server;
    }
}
