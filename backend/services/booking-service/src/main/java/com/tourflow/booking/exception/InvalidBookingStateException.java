package com.tourflow.booking.exception;

// Thrown when a requested status transition isn't valid from the booking's
// current state (e.g. completing a booking that was never paid).
public class InvalidBookingStateException extends RuntimeException {

    public InvalidBookingStateException(String message) {
        super(message);
    }
}
