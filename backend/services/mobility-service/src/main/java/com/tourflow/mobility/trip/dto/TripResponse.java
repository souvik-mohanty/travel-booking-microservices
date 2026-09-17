package com.tourflow.trip.dto;

import com.tourflow.trip.domain.Trip;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TripResponse(
        UUID id,
        UUID bookingId,
        UUID createdBy,
        UUID driverId,
        UUID vehicleId,
        String status,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static TripResponse fromEntity(Trip trip) {
        return new TripResponse(
                trip.getId(),
                trip.getBookingId(),
                trip.getCreatedBy(),
                trip.getDriverId(),
                trip.getVehicleId(),
                trip.getStatus().name(),
                trip.getStartedAt(),
                trip.getCompletedAt(),
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }
}
