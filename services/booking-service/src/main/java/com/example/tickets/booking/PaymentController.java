package com.example.tickets.booking;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
class PaymentController {

    private final PaymentService paymentService;

    PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{id}/payments")
    ResponseEntity<PaymentResponse> pay(@PathVariable long id, @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.pay(id, request));
    }

    @GetMapping("/{id}/payments")
    ResponseEntity<List<PaymentResponse>> history(@PathVariable long id) {
        return ResponseEntity.ok(paymentService.history(id));
    }
}
