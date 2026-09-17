package com.tourflow.payment.exception;

// Exception thrown when a requested payment does not exist.
public class PaymentNotFoundException extends RuntimeException {

    // Create a payment-not-found exception with a message.
    public PaymentNotFoundException(String message) {
        super(message);
    }
}
