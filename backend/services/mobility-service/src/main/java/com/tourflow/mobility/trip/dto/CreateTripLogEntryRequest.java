package com.tourflow.mobility.trip.dto;

import com.tourflow.mobility.trip.domain.TripLogEntryType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CreateTripLogEntryRequest(

        @NotNull
        TripLogEntryType type,

        @Size(max = 1000)
        String description,

        @Size(max = 255)
        String location,

        // Defaults to now() in TripLogService if omitted -- lets a backdated
        // entry (e.g. logging yesterday's checkout after the fact) specify
        // when it actually happened.
        OffsetDateTime occurredAt
) {
}
