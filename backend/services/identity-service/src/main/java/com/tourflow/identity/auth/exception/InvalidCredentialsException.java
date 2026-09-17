package com.tourflow.identity.auth.exception;

// Thrown for any login failure: unknown email, wrong password, or a token that doesn't
// resolve to an active session. Deliberately generic so we never reveal to a caller
// whether the email or the password was the part that was wrong.
// Mapped to HTTP 401 Unauthorized by GlobalExceptionHandler.
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException(String message) {
        super(message);
    }
}
