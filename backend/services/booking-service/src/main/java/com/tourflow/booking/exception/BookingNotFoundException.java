package com.tourflow.booking.exception;

// Exception thrown when a requested booking does not exist.
public class BookingNotFoundException extends RuntimeException {

    // Create a booking-not-found exception with a message.
    public BookingNotFoundException(String message) {
        super(message);
    }
}
