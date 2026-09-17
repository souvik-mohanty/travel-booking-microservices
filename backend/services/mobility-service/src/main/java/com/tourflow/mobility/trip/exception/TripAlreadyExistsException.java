package com.tourflow.trip.exception;

public class TripAlreadyExistsException extends RuntimeException {

    public TripAlreadyExistsException(String message) {
        super(message);
    }
}
