package com.tourflow.hotel.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateRoomRequest(

        @NotNull
        UUID hotelId,

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
