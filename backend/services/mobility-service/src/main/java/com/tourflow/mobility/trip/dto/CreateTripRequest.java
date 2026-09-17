package com.tourflow.mobility.trip.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTripRequest(

        @NotNull
        UUID bookingId
) {
}
