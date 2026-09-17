package com.tourflow.ride.exception;

// Thrown for invalid state transitions and for seat-availability violations
// (not enough seats left on a ride).
public class InvalidRideStateException extends RuntimeException {

    public InvalidRideStateException(String message) {
        super(message);
    }
}
