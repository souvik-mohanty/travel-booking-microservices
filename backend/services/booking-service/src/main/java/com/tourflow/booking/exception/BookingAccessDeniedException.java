package com.tourflow.booking.exception;

// Thrown when a user tries to manage a booking they don't own (or, for
// completion, a tour they didn't create).
public class BookingAccessDeniedException extends RuntimeException {

    public BookingAccessDeniedException(String message) {
        super(message);
    }
}
