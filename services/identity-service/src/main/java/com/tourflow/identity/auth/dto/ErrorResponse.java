package com.tourflow.identity.auth.dto;

import java.time.OffsetDateTime;
import java.util.Map;

// Uniform error body returned by every failure response from this service.
// fieldErrors is only populated for bean-validation (400) failures, otherwise it's null.
public record ErrorResponse(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, null);
    }

    public static ErrorResponse ofValidation(int status, String error, String message, Map<String, String> fieldErrors) {
        return new ErrorResponse(OffsetDateTime.now(), status, error, message, fieldErrors);
    }
}
