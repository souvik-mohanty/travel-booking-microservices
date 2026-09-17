package com.tourflow.trip.exception;

public class TripAccessDeniedException extends RuntimeException {

    public TripAccessDeniedException(String message) {
        super(message);
    }
}
