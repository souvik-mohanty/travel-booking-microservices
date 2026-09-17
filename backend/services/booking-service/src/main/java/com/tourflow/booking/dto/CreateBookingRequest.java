package com.tourflow.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Request body used when creating a new booking.
public record CreateBookingRequest(

        // ID of the tour the user wants to book.
        @NotNull(message = "Tour ID is required")
        UUID tourId,

        // Number of participants for the booking.
        @NotNull(message = "Number of participants is required")
        @Min(value = 1, message = "Number of participants must be at least 1")
        Integer numberOfParticipants
) {
}
