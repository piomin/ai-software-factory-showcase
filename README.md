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

To run the same build locally:

```bash
mvn -B verify
```

## Run

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
cd frontend && npm run dev
```

Then open http://localhost:5173 in your browser.
