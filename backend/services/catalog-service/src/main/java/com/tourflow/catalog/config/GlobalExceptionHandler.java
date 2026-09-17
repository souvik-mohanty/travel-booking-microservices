package com.tourflow.catalog.config;

import com.tourflow.catalog.business.exception.ActivityNotFoundException;
import com.tourflow.catalog.business.exception.BusinessNotFoundException;
import com.tourflow.catalog.business.exception.UnauthorizedBusinessAccessException;
import com.tourflow.catalog.guide.exception.GuideAccessDeniedException;
import com.tourflow.catalog.guide.exception.GuideAlreadyExistsException;
import com.tourflow.catalog.guide.exception.GuideNotFoundException;
import com.tourflow.catalog.hotel.exception.HotelNotFoundException;
import com.tourflow.catalog.hotel.exception.RoomNotAvailableException;
import com.tourflow.catalog.hotel.exception.RoomNotFoundException;
import com.tourflow.catalog.hotel.exception.RoomReservationNotFoundException;
import com.tourflow.catalog.hotel.exception.UnauthorizedHotelAccessException;
import com.tourflow.catalog.service.TourActivityNotFoundException;
import com.tourflow.catalog.service.TourLegNotFoundException;
import com.tourflow.catalog.service.TourNotFoundException;
import com.tourflow.catalog.service.TourOwnershipException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

// Central place that turns exceptions raised anywhere in catalog-service (tour +
// business + hotel + guide, merged 2026-09-06) into proper HTTP status codes.
// Without this, "Tour not found" and validation failures like "End date cannot
// be before start date" fall through as unhandled exceptions instead of clean
// 404/400 responses.
//
// business/hotel/guide each used to have their own GlobalExceptionHandler with
// their own generic IllegalArgumentException/MethodArgumentNotValidException
// catch-alls returning a bare string/Map<String,String> body; those are gone
// now that everything lives in one merged handler -- tour's richer
// {timestamp,status,error,message} shape (below) is what all four domains'
// generic validation errors return now, not just tour's own.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TourNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTourNotFound(TourNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TourOwnershipException.class)
    public ResponseEntity<Map<String, Object>> handleTourOwnership(TourOwnershipException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(TourLegNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTourLegNotFound(TourLegNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TourActivityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTourActivityNotFound(TourActivityNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BusinessNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessNotFound(BusinessNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ActivityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleActivityNotFound(ActivityNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedBusinessAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedBusinessAccess(UnauthorizedBusinessAccessException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(HotelNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleHotelNotFound(HotelNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoomNotFound(RoomNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RoomReservationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRoomReservationNotFound(RoomReservationNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RoomNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleRoomNotAvailable(RoomNotAvailableException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedHotelAccessException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedHotelAccess(UnauthorizedHotelAccessException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(GuideNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleGuideNotFound(GuideNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(GuideAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleGuideAlreadyExists(GuideAlreadyExistsException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(GuideAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleGuideAccessDenied(GuideAccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // Covers validation performed anywhere in catalog-service, e.g. "End date
    // cannot be before start date" (tour) or "startDate must be before
    // endDate" (hotel reservations).
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Triggered by @Valid on CreateTourRequest. Reports exactly which fields
    // failed instead of a bare "Bad Request".
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> body = errorBody(HttpStatus.BAD_REQUEST, "Validation failed");
        body.put("fieldErrors", fieldErrors);

        return ResponseEntity.badRequest().body(body);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(errorBody(status, message));
    }

    private Map<String, Object> errorBody(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", OffsetDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return body;
    }
}
