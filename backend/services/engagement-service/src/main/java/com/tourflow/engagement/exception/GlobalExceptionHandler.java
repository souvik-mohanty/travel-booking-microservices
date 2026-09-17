package com.tourflow.engagement.exception;

import com.tourflow.engagement.support.exception.InvalidTicketStateException;
import com.tourflow.engagement.support.exception.TicketAccessDeniedException;
import com.tourflow.engagement.support.exception.TicketNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<String> handleTicketNotFound(
            TicketNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(TicketAccessDeniedException.class)
    public ResponseEntity<String> handleTicketAccessDenied(
            TicketAccessDeniedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InvalidTicketStateException.class)
    public ResponseEntity<String> handleInvalidTicketState(
            InvalidTicketStateException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(ReviewValidationException.class)
    public ResponseEntity<?> handleValidation(
            ReviewValidationException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", 400,
                        "error", "Review validation failed",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<?> handleExternalService(
            ExternalServiceException ex
    ) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", 503,
                        "error", "Service unavailable",
                        "message", ex.getMessage()
                ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleFieldValidation(
            MethodArgumentNotValidException ex
    ) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        fieldErrors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "status", 400,
                        "error", "Request validation failed",
                        "message", fieldErrors
                ));
    }
}
