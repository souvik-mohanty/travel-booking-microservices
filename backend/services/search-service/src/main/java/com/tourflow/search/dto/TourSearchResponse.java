package com.tourflow.search.dto;

import com.tourflow.search.document.TourDocument;

import java.time.LocalDate;
import java.util.UUID;

public record TourSearchResponse(
        UUID id,
        String title,
        String description,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        Double price,
        Integer maxParticipants,
        UUID createdBy
) {

    public static TourSearchResponse from(TourDocument document) {
        return new TourSearchResponse(
                UUID.fromString(document.getId()),
                document.getTitle(),
                document.getDescription(),
                document.getDestination(),
                document.getStartDate(),
                document.getEndDate(),
                document.getPrice(),
                document.getMaxParticipants(),
                UUID.fromString(document.getCreatedBy())
        );
    }
}
