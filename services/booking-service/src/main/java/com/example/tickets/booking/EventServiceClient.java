package com.example.tickets.booking;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.net.URI;

class EventServiceClient {

    private final RestClient restClient;

    EventServiceClient(String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    EventDetails getEvent(long eventId) {
        try {
            URI uri = URI.create("/api/events/" + eventId);
            return restClient.get()
                    .uri(uri)
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new EventNotFoundException(eventId);
                    })
                    .onStatus(status -> status.value() == 503, (req, res) -> {
                        throw new ServiceUnavailableException("Event service is unavailable");
                    })
                    .body(EventDetails.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 503) {
                throw new ServiceUnavailableException("Event service is unavailable");
            }
            throw e;
        }
    }

    void releaseTickets(long eventId, int tickets) {
        try {
            URI uri = URI.create("/api/events/" + eventId + "/ticket-release");
            restClient.post()
                    .uri(uri)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(new TicketReleaseRequest(tickets))
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new EventNotFoundException(eventId);
                    })
                    .onStatus(status -> status.value() == 503, (req, res) -> {
                        throw new ServiceUnavailableException("Event service is unavailable");
                    })
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 503) {
                throw new ServiceUnavailableException("Event service is unavailable");
            }
            throw e;
        }
    }

    EventReservationResponse reserveTickets(long eventId, int tickets) {
        try {
            URI uri = URI.create("/api/events/" + eventId + "/reservations");
            return restClient.post()
                    .uri(uri)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(new EventReservationRequest(tickets))
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                        throw new EventNotFoundException(eventId);
                    })
                    .onStatus(status -> status.value() == 409, (req, res) -> {
                        throw new NotEnoughTicketsException(tickets);
                    })
                    .onStatus(status -> status.value() == 503, (req, res) -> {
                        throw new ServiceUnavailableException("Event service is unavailable");
                    })
                    .body(EventReservationResponse.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == 503) {
                throw new ServiceUnavailableException("Event service is unavailable");
            }
            throw e;
        }
    }
}
