package com.tourflow.fleet.exception;

public class VehicleAccessDeniedException extends RuntimeException {

    public VehicleAccessDeniedException(String message) {
        super(message);
    }
}
