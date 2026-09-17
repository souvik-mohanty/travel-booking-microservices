package com.tourflow.catalog.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

// Request body used when creating a new tour.
public record CreateTourRequest(

        // Tour title must be provided.
        @NotBlank
        @Size(max = 255)
        String title,

        // Description is optional.
        String description,

        // Destination must be provided.
        @NotBlank
        @Size(max = 255)
        String destination,

        // Tour cannot start in the past.
        @NotNull
        @FutureOrPresent
        LocalDate startDate,

        // End date is required.
        @NotNull
        LocalDate endDate,

        // Price must be zero or greater.
        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal price,

        // At least one participant must be allowed.
        @NotNull
        @Min(1)
        Integer maxParticipants
) {
}