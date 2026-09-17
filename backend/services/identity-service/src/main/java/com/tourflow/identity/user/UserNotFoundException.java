package com.tourflow.identity.user;

// Thrown when an admin looks up a user ID that doesn't exist.
// Mapped to HTTP 404 Not Found by GlobalExceptionHandler.
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
