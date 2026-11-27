# Ticket Booking System

A ticket booking system built with Spring Boot microservices and Vue 3 frontend.

## Structure

```
pom.xml                          parent / aggregator (packaging pom)
services/event-service/          Spring Boot, lists events (concerts, shows)
services/booking-service/        Spring Boot, creates and reads bookings
gateway/                         Spring Cloud Gateway, single entry point for the frontend
frontend/                        Vue 3 + Vite + TypeScript, built by Maven
```

## Prerequisites

- **JDK 25** is required. The root `pom.xml` sets `java.version` to `25`, which is passed to the compiler as `<release>`. With an older JDK the build fails with `release version 25 not supported`.
- **Maven 3.9+**, running on JDK 25: `JAVA_HOME` must point to a JDK 25 installation.
- **Node.js** does not need to be installed globally for the Maven build, because `frontend-maven-plugin` downloads it. It is needed only to run the frontend dev server (`npm run dev`).

Check your setup. Both commands must report Java 25:

```bash
java -version
mvn -v
```

If you need to install JDK 25, for example:

```bash
sdk install java 25-tem      # SDKMAN
brew install openjdk@25      # Homebrew (macOS)
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

Run each in its own terminal:

```bash
mvn spring-boot:run -pl services/event-service     # port 8081
mvn spring-boot:run -pl services/booking-service   # port 8082
mvn spring-boot:run -pl gateway                    # port 8080
cd frontend && npm install && npm run dev          # port 5173
```

Then open http://localhost:5173 in your browser. The frontend calls only the gateway.

## Database

Both services use H2 in-memory databases with Flyway migrations:

- **event-service**: `jdbc:h2:mem:events`
- **booking-service**: `jdbc:h2:mem:bookings`

Flyway automatically applies migrations on startup. To enable the H2 console for local debugging, set `spring.h2.console.enabled=true` in your `application.properties`.
