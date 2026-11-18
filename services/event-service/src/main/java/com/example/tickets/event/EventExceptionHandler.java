package com.example.tickets.event;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class EventExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    ProblemDetail handleEventNotFoundException(EventNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND.value());
        problemDetail.setTitle("Event Not Found");
        problemDetail.setDetail(ex.getMessage());
        return problemDetail;
    }

    @ExceptionHandler(NotEnoughTicketsException.class)
    ProblemDetail handleNotEnoughTicketsException(NotEnoughTicketsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(ex.getStatusCode().value());
        problemDetail.setTitle("Not Enough Tickets");
        problemDetail.setDetail(ex.getReason());
        return problemDetail;
    }
}
