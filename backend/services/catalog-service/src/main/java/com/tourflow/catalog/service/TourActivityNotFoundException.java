package com.tourflow.catalog.service;

// Thrown when trying to remove an activity that isn't attached to the tour.
// Mapped to HTTP 404 Not Found by GlobalExceptionHandler.
public class TourActivityNotFoundException extends RuntimeException {

    public TourActivityNotFoundException(String message) {
        super(message);
    }
}
