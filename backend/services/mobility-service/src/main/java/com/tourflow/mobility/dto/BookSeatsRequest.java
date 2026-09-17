package com.tourflow.mobility.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record BookSeatsRequest(

        @NotNull
        UUID rideId,

        @NotNull
        @Min(1)
        Integer seats
) {
}
