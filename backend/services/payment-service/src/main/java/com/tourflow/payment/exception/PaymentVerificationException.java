package com.tourflow.payment.exception;

// Exception thrown when a Razorpay Checkout signature fails verification.
public class PaymentVerificationException extends RuntimeException {

    public PaymentVerificationException(String message) {
        super(message);
    }
}
