FROM maven:3.9-eclipse-temurin-25-alpine AS build
WORKDIR /build
COPY pom.xml ./
COPY services/event-service/pom.xml ./services/event-service/
COPY services/booking-service/pom.xml ./services/booking-service/
RUN mvn -q -T1C dependency:go-offline -DskipTests
COPY services/event-service/src ./services/event-service/src
COPY services/booking-service/src ./services/booking-service/src
RUN mvn -q -DskipTests package
RUN mvn -pl services/event-service -am spring-boot:build-image -Dspring-boot.build-image.imageName=event-service
RUN mvn -pl services/booking-service -am spring-boot:build-image -Dspring-boot.build-image.imageName=booking-service

FROM maven:3.9-eclipse-temurin-25-alpine AS copy-events
WORKDIR /build
COPY services/event-service/target/*.jar /build/event-service.jar

FROM maven:3.9-eclipse-temurin-25-alpine AS copy-bookings
WORKDIR /build
COPY services/booking-service/target/*.jar /build/booking-service.jar
