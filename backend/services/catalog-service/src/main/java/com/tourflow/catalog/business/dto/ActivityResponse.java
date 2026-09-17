package com.tourflow.business.dto;

import com.tourflow.business.domain.Activity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        UUID businessId,
        String name,
        String description,
        String location,
        BigDecimal price,
        Integer durationMinutes,
        Integer maxParticipants,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static ActivityResponse fromEntity(Activity activity) {
        return new ActivityResponse(
                activity.getId(),
                activity.getBusinessId(),
                activity.getName(),
                activity.getDescription(),
                activity.getLocation(),
                activity.getPrice(),
                activity.getDurationMinutes(),
                activity.getMaxParticipants(),
                activity.getStatus().name(),
                activity.getCreatedAt(),
                activity.getUpdatedAt()
        );
    }
}
