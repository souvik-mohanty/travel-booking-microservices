package com.tourflow.hotel.exception;

public class UnauthorizedHotelAccessException extends RuntimeException {

    public UnauthorizedHotelAccessException(String message) {
        super(message);
    }
}
