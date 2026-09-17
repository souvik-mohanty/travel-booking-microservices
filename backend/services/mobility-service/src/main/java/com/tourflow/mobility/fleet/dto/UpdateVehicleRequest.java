package com.tourflow.mobility.fleet.dto;

import com.tourflow.mobility.fleet.domain.VehicleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record UpdateVehicleRequest(

        @NotNull
        VehicleType type,

        @NotNull
        @Min(1)
        Integer capacity
) {
}
