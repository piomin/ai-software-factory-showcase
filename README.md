# Ticket Booking System

A ticket booking system built with Spring Boot microservices and Vue 3 frontend.

## Structure

```
pom.xml                          parent / aggregator (packaging pom)
services/event-service/          Spring Boot, lists events (concerts, shows)
services/booking-service/        Spring Boot, creates and reads bookings
frontend/                        Vue 3 + Vite + TypeScript, built by Maven
```

## Build

```bash
mvn clean verify
```

This builds both services and runs all tests, including the frontend build and tests.

## CI

This repository uses CircleCI for continuous integration. The build runs on every pull request and on `master` branch, compiling the whole repository and running all automated tests (backend and frontend).

The CI workflow includes additional validation steps (all run offline, no cluster required):
- Skaffold configuration (`skaffold.yaml`) is validated for syntax and consistency with `skaffold diagnose --yaml-only`
- Kubernetes manifests in `k8s/` are validated against the Kubernetes schema with `kubeconform -strict`, with no cluster reachable
- Image names used in Skaffold artifacts must match those referenced in Kubernetes manifests

To run the same build locally:

```bash
mvn -B verify
```

## Run

Start each component in its own terminal. Each one uses its own port:

| Component | Port |
|---|---|
| event-service | 8081 |
| booking-service | 8082 |
| frontend dev server | 5173 (proxies `/api/events` to 8081 and `/api/bookings` to 8082) |

To use a different port for a service, set `SERVER_PORT`. If you move event-service, set `EVENT_SERVICE_URL` for booking-service to the new address.

### Event Service

```bash
mvn spring-boot:run -pl services/event-service
```

### Booking Service

```bash
mvn spring-boot:run -pl services/booking-service
```

### Frontend

```bash
cd frontend && npm install && npm run dev
```

Then open http://localhost:5173 in your browser.

## Database

Both services use H2 in-memory databases with Flyway migrations:

- **event-service**: `jdbc:h2:mem:events`
- **booking-service**: `jdbc:h2:mem:bookings`

Flyway automatically applies migrations on startup. To enable the H2 console for local debugging, set `spring.h2.console.enabled=true` in your `application.properties`.
