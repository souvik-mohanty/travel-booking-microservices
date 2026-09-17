package com.tourflow.identity.auth.exception;

// Thrown when a login/refresh is attempted against a valid but disabled account.
// Mapped to HTTP 403 Forbidden by GlobalExceptionHandler.
public class AccountDisabledException extends RuntimeException {

    public AccountDisabledException(String message) {
        super(message);
    }
}
