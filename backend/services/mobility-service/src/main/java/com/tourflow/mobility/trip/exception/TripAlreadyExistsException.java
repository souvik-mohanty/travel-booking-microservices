package com.tourflow.mobility.trip.exception;

public class TripAlreadyExistsException extends RuntimeException {

    public TripAlreadyExistsException(String message) {
        super(message);
    }
}
