package com.tourflow.trip.exception;

public class InvalidTripStateException extends RuntimeException {

    public InvalidTripStateException(String message) {
        super(message);
    }
}
