package com.tourflow.fleet.dto;

import com.tourflow.fleet.domain.Vehicle;

import java.time.OffsetDateTime;
import java.util.UUID;

public record VehicleResponse(
        UUID id,
        UUID ownerId,
        String registrationNumber,
        String type,
        Integer capacity,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getOwnerId(),
                vehicle.getRegistrationNumber(),
                vehicle.getType().name(),
                vehicle.getCapacity(),
                vehicle.getStatus().name(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
