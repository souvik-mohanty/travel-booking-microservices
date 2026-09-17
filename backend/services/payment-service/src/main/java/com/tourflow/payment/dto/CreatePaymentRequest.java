package com.tourflow.payment.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Client only supplies the booking.
// Amount is fetched securely from Booking Service.
public record CreatePaymentRequest(

        @NotNull(message = "Booking ID is required")
        UUID bookingId

) {
}
