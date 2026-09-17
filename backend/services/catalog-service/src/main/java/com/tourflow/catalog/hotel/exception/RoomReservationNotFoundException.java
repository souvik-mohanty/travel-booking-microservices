package com.tourflow.catalog.hotel.exception;

// Thrown when a requested reservation ID doesn't exist.
// Mapped to HTTP 404 Not Found by GlobalExceptionHandler.
public class RoomReservationNotFoundException extends RuntimeException {

    public RoomReservationNotFoundException(String message) {
        super(message);
    }
}
