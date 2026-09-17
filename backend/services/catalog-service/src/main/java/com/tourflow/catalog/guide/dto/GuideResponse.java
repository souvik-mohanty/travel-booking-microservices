package com.tourflow.catalog.guide.dto;

import com.tourflow.catalog.guide.domain.Guide;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GuideResponse(
        UUID id,
        UUID userId,
        String bio,
        String languagesSpoken,
        Integer yearsOfExperience,
        String phone,
        String status,
        String availability,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static GuideResponse fromEntity(Guide guide) {
        return new GuideResponse(
                guide.getId(),
                guide.getUserId(),
                guide.getBio(),
                guide.getLanguagesSpoken(),
                guide.getYearsOfExperience(),
                guide.getPhone(),
                guide.getStatus().name(),
                guide.getAvailability().name(),
                guide.getCreatedAt(),
                guide.getUpdatedAt()
        );
    }
}
