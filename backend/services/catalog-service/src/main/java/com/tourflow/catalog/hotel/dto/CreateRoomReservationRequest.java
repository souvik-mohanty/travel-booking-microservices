package com.tourflow.catalog.hotel.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record CreateRoomReservationRequest(

        @NotNull
        UUID tourId,

        // Null when the tour has no legs of its own -- the whole tour is one
        // stay.
        UUID tourLegId,

        @NotNull
        @FutureOrPresent
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @NotNull
        @Min(1)
        Integer roomsRequested
) {
}
