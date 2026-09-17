package com.tourflow.catalog.business.exception;

// Thrown when a requested activity does not exist.
public class ActivityNotFoundException extends RuntimeException {

    public ActivityNotFoundException(String message) {
        super(message);
    }
}
