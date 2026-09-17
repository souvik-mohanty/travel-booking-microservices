package com.tourflow.identity.profile.exception;

public class UserProfileAlreadyExistsException extends RuntimeException {

    public UserProfileAlreadyExistsException(String message) {
        super(message);
    }
}
