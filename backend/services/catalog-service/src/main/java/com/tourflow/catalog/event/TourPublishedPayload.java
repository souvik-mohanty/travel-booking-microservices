package com.tourflow.catalog.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

// Carries everything search-service needs to index this tour without a
// follow-up call back to tour-service.
public record TourPublishedPayload(
        UUID tourId,
        String title,
        String description,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        Integer maxParticipants,
        UUID createdBy
) {
}
