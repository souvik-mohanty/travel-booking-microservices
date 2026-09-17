package com.tourflow.catalog.service;

// Thrown when a requested tour leg ID doesn't exist (or doesn't belong to
// the tour it was requested under). Mapped to HTTP 404 Not Found by
// GlobalExceptionHandler.
public class TourLegNotFoundException extends RuntimeException {

    public TourLegNotFoundException(String message) {
        super(message);
    }
}
