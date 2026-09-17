package com.tourflow.catalog.business.dto;

import com.tourflow.catalog.business.domain.Business;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BusinessResponse(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        String phone,
        String email,
        String address,
        String city,
        String state,
        String country,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static BusinessResponse fromEntity(Business business) {
        return new BusinessResponse(
                business.getId(),
                business.getOwnerId(),
                business.getName(),
                business.getDescription(),
                business.getPhone(),
                business.getEmail(),
                business.getAddress(),
                business.getCity(),
                business.getState(),
                business.getCountry(),
                business.getStatus().name(),
                business.getCreatedAt(),
                business.getUpdatedAt()
        );
    }
}
