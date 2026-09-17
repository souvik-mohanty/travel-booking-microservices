package com.tourflow.identity.auth.exception;

// Thrown when registration is attempted with an email that is already in use.
// Mapped to HTTP 409 Conflict by GlobalExceptionHandler.
public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}
