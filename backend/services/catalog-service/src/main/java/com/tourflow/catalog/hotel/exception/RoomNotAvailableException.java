package com.tourflow.catalog.hotel.exception;

// Thrown when a reservation request asks for more rooms than are free for
// the requested date range. Mapped to HTTP 400 Bad Request by
// GlobalExceptionHandler -- matches the "seat/inventory unavailable"
// convention in docs/api/API-STANDARDS.md's status-code table.
public class RoomNotAvailableException extends RuntimeException {

    public RoomNotAvailableException(String message) {
        super(message);
    }
}
