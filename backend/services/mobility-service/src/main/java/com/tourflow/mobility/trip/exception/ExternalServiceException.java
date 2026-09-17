package com.tourflow.mobility.trip.exception;

// A downstream service (booking-service, tour-service) couldn't be reached
// at all while resolving logbook access. Mapped to HTTP 503 by
// GlobalExceptionHandler, matching tour-service's identical exception.
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
