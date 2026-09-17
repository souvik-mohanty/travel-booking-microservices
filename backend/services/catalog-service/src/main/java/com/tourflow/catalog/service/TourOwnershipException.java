package com.tourflow.catalog.service;

// Thrown when the caller tries to publish/cancel a tour they didn't create.
// Mapped to HTTP 403 Forbidden by GlobalExceptionHandler -- the caller
// already knows the tour exists (they have its ID), so 403 doesn't leak
// anything a 404 would need to hide (see docs/api/API-STANDARDS.md).
public class TourOwnershipException extends RuntimeException {

    public TourOwnershipException(String message) {
        super(message);
    }
}
