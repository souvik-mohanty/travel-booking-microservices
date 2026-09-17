package com.tourflow.catalog.hotel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateRoomRequest(

        @NotBlank
        String roomType,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal pricePerNight,

        @NotNull
        @Min(1)
        Integer capacity,

        @NotNull
        @Min(1)
        Integer totalRooms
) {
}
