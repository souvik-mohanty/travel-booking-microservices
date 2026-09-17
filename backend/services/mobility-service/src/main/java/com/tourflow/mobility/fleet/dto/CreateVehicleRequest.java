package com.tourflow.fleet.dto;

import com.tourflow.fleet.domain.VehicleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

// ownerId is deliberately not accepted here -- it comes from the JWT.
public record CreateVehicleRequest(

        @NotBlank
        String registrationNumber,

        @NotNull
        VehicleType type,

        @NotNull
        @Min(1)
        Integer capacity
) {
}
