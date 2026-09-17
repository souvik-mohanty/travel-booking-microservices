package com.tourflow.catalog.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AddTourActivityRequest(

        @NotNull
        UUID activityId,

        // Must fall within the tour's own startDate/endDate.
        @NotNull
        LocalDate scheduledDate,

        // Optional -- not every activity needs a fixed time slot.
        LocalTime scheduledTime
) {
}
