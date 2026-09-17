package com.tourflow.payment.exception;

// Exception thrown when a user tries to act on a payment that isn't theirs.
public class PaymentAccessDeniedException extends RuntimeException {

    public PaymentAccessDeniedException(String message) {
        super(message);
    }
}
