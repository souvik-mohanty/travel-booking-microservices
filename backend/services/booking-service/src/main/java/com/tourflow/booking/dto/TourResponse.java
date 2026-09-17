package com.tourflow.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TourResponse(
        UUID id,
        String title,
        String description,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        Integer maxParticipants,
        String status,
        UUID createdBy,
        String createdAt,
        String updatedAt
) {
}
