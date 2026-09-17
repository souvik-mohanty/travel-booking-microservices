package com.tourflow.catalog.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateTourLegRequest(

        // Falls back to the parent tour's destination if omitted.
        String destination,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotNull
        UUID hotelId,

        @NotNull
        UUID roomId,

        @NotNull
        @Min(1)
        Integer roomsBooked
) {
}
