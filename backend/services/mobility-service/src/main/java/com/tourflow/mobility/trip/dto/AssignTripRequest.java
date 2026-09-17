package com.tourflow.mobility.trip.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignTripRequest(

        @NotNull
        UUID driverId,

        @NotNull
        UUID vehicleId
) {
}
