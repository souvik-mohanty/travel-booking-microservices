package com.tourflow.business.exception;

// Thrown when a requested business does not exist.
public class BusinessNotFoundException extends RuntimeException {

    public BusinessNotFoundException(String message) {
        super(message);
    }
}
