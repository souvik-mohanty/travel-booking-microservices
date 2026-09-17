package com.tourflow.identity.authorization.exception;

// Covers duplicate role/permission names and duplicate role<->permission or
// user<->role assignments -- all are "this already exists" in shape.
public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(String message) {
        super(message);
    }
}
