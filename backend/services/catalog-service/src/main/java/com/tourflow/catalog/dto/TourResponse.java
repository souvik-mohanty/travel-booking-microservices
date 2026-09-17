package com.tourflow.catalog.dto;

import com.tourflow.catalog.domain.Tour;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

// Response returned by the Tour API.
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
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    // Convert a Tour entity into an API response.
    public static TourResponse from(Tour tour) {
        return new TourResponse(
                tour.getId(),
                tour.getTitle(),
                tour.getDescription(),
                tour.getDestination(),
                tour.getStartDate(),
                tour.getEndDate(),
                tour.getPrice(),
                tour.getMaxParticipants(),
                tour.getStatus(),
                tour.getCreatedBy(),
                tour.getCreatedAt(),
                tour.getUpdatedAt()
        );
    }
}