package com.tourflow.payment.controller;

import com.tourflow.payment.dto.CreatePaymentRequest;
import com.tourflow.payment.dto.CreatePaymentResponse;
import com.tourflow.payment.dto.PaymentResponse;
import com.tourflow.payment.dto.VerifyPaymentRequest;
import com.tourflow.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// REST API for customer payments.
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Create a Razorpay order for the authenticated customer's booking.
    @PostMapping
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication
    ) {

        UUID customerId =
                UUID.fromString(authentication.getName());

        // JwtAuthenticationFilter stores the raw JWT in credentials.
        String token =
                (String) authentication.getCredentials();

        CreatePaymentResponse payment =
                paymentService.createPayment(
                        request,
                        customerId,
                        token
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(payment);
    }

    // Verify the signature Razorpay Checkout returns to the frontend on success.
    @PostMapping("/{paymentId}/verify")
    public ResponseEntity<PaymentResponse> verifyPayment(
            @PathVariable UUID paymentId,
            @Valid @RequestBody VerifyPaymentRequest request,
            Authentication authentication
    ) {

        UUID customerId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                paymentService.verifyPayment(paymentId, request, customerId)
        );
    }

    // Razorpay's server-to-server webhook. Authenticated via the
    // X-Razorpay-Signature header instead of a JWT -- see SecurityConfig.
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) {

        paymentService.handleWebhook(rawBody, signature);

        return ResponseEntity.ok().build();
    }

    // Get one payment.
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable UUID paymentId
    ) {

        return ResponseEntity.ok(
                paymentService.getPayment(paymentId)
        );
    }

    // Return payments belonging to the authenticated customer.
    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getPayments(
            Authentication authentication
    ) {

        UUID customerId =
                UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                paymentService.getCustomerPayments(customerId)
        );
    }
}
