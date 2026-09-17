package com.tourflow.catalog.business.exception;

// Thrown when an authenticated user tries to manage a business/activity they don't own.
// Unlike BusinessNotFoundException's "hide existence" use in creation flows, this is used
// where the caller already knows the resource exists (they have its ID from a prior
// create/list call), so a 403 doesn't leak anything a 404 would have hidden.
public class UnauthorizedBusinessAccessException extends RuntimeException {

    public UnauthorizedBusinessAccessException(String message) {
        super(message);
    }
}
