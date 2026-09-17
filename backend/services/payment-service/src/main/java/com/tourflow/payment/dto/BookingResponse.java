package com.tourflow.payment.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Minimal booking information required by Payment Service.
public record BookingResponse(
        UUID id,
        UUID tourId,
        UUID userId,
        Integer numberOfParticipants,
        BigDecimal totalPrice,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
