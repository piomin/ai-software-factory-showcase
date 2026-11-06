package com.example.tickets.booking;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
class BookingExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    ProblemDetail handleBookingNotFoundException(BookingNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(ex.getStatusCode().value());
        problemDetail.setTitle("Booking Not Found");
        problemDetail.setDetail(ex.getReason());
        return problemDetail;
    }

    @ExceptionHandler(InvalidBookingRequestException.class)
    ProblemDetail handleInvalidBookingRequestException(InvalidBookingRequestException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(ex.getStatusCode().value());
        problemDetail.setTitle("Invalid Booking Request");
        problemDetail.setDetail(ex.getReason());
        return problemDetail;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(400);
        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail("Invalid booking request");
        
        List<FieldError> fieldErrors = new ArrayList<>();
        ex.getConstraintViolations().forEach(violation -> {
            fieldErrors.add(new FieldError(
                    violation.getPropertyPath().toString(),
                    violation.getInvalidValue(),
                    violation.getMessage()
            ));
        });
        
        problemDetail.setProperty("fieldErrors", fieldErrors);
        return problemDetail;
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    ProblemDetail handleMethodArgumentNotValid(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(400);
        problemDetail.setTitle("Validation Error");
        problemDetail.setDetail("Invalid booking request");
        
        List<FieldError> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            fieldErrors.add(new FieldError(
                    error.getField(),
                    error.getRejectedValue(),
                    error.getDefaultMessage()
            ));
        });
        
        problemDetail.setProperty("fieldErrors", fieldErrors);
        return problemDetail;
    }

    record FieldError(String field, Object value, String message) {}
}
