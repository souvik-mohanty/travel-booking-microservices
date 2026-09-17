package com.tourflow.mobility.dto;

import com.tourflow.mobility.domain.RideType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// createdBy is deliberately not accepted here -- it comes from the JWT.
public record CreateRideRequest(

        @NotNull
        RideType type,

        @NotBlank
        String pickupLocation,

        @NotBlank
        String dropLocation,

        @NotNull
        @Min(1)
        Integer totalSeats,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal pricePerSeat
) {
}
