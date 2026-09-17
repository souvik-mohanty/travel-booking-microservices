package com.tourflow.mobility.exception;

import com.tourflow.mobility.driver.exception.DriverAccessDeniedException;
import com.tourflow.mobility.driver.exception.DriverAlreadyExistsException;
import com.tourflow.mobility.driver.exception.DriverNotFoundException;
import com.tourflow.mobility.fleet.exception.VehicleAccessDeniedException;
import com.tourflow.mobility.fleet.exception.VehicleAlreadyExistsException;
import com.tourflow.mobility.fleet.exception.VehicleNotFoundException;
import com.tourflow.mobility.tracking.exception.NoLocationDataException;
import com.tourflow.mobility.trip.exception.ExternalServiceException;
import com.tourflow.mobility.trip.exception.InvalidTripStateException;
import com.tourflow.mobility.trip.exception.TripAccessDeniedException;
import com.tourflow.mobility.trip.exception.TripAlreadyExistsException;
import com.tourflow.mobility.trip.exception.TripNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DriverNotFoundException.class)
    public ResponseEntity<String> handleDriverNotFound(
            DriverNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DriverAlreadyExistsException.class)
    public ResponseEntity<String> handleDriverAlreadyExists(
            DriverAlreadyExistsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(DriverAccessDeniedException.class)
    public ResponseEntity<String> handleDriverAccessDenied(
            DriverAccessDeniedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(exception.getMessage());
    }

    @ExceptionHandler(VehicleNotFoundException.class)
    public ResponseEntity<String> handleVehicleNotFound(
            VehicleNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(VehicleAlreadyExistsException.class)
    public ResponseEntity<String> handleVehicleAlreadyExists(
            VehicleAlreadyExistsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(VehicleAccessDeniedException.class)
    public ResponseEntity<String> handleVehicleAccessDenied(
            VehicleAccessDeniedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(exception.getMessage());
    }

    @ExceptionHandler(TripNotFoundException.class)
    public ResponseEntity<String> handleTripNotFound(
            TripNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(TripAlreadyExistsException.class)
    public ResponseEntity<String> handleTripAlreadyExists(
            TripAlreadyExistsException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(exception.getMessage());
    }

    @ExceptionHandler(TripAccessDeniedException.class)
    public ResponseEntity<String> handleTripAccessDenied(
            TripAccessDeniedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InvalidTripStateException.class)
    public ResponseEntity<String> handleInvalidTripState(
            InvalidTripStateException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    // A downstream service (booking-service, catalog-service) couldn't be
    // reached at all -- distinct from an access-denied rejection, which
    // comes back as TripAccessDeniedException (403) instead.
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<String> handleExternalService(
            ExternalServiceException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(exception.getMessage());
    }

    @ExceptionHandler(NoLocationDataException.class)
    public ResponseEntity<String> handleNoLocationData(
            NoLocationDataException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<String> handleRideNotFound(
            RideNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(RideBookingNotFoundException.class)
    public ResponseEntity<String> handleRideBookingNotFound(
            RideBookingNotFoundException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }

    @ExceptionHandler(RideAccessDeniedException.class)
    public ResponseEntity<String> handleAccessDenied(
            RideAccessDeniedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(exception.getMessage());
    }

    @ExceptionHandler(InvalidRideStateException.class)
    public ResponseEntity<String> handleInvalidState(
            InvalidRideStateException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity
                .badRequest()
                .body(errors);
    }
}
