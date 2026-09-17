package com.tourflow.trip.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTripRequest(

        @NotNull
        UUID bookingId
) {
}
