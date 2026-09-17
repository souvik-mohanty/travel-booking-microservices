package com.tourflow.catalog.service;

// Thrown when a requested tour ID doesn't exist.
// Mapped to HTTP 404 Not Found by GlobalExceptionHandler.
public class TourNotFoundException extends RuntimeException {

    public TourNotFoundException(String message) {
        super(message);
    }
}
