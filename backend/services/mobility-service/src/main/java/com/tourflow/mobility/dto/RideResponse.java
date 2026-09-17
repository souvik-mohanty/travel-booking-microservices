package com.tourflow.mobility.dto;

import com.tourflow.mobility.domain.Ride;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RideResponse(
        UUID id,
        UUID createdBy,
        String type,
        String pickupLocation,
        String dropLocation,
        Integer totalSeats,
        Integer availableSeats,
        BigDecimal pricePerSeat,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static RideResponse fromEntity(Ride ride) {
        return new RideResponse(
                ride.getId(),
                ride.getCreatedBy(),
                ride.getType().name(),
                ride.getPickupLocation(),
                ride.getDropLocation(),
                ride.getTotalSeats(),
                ride.getAvailableSeats(),
                ride.getPricePerSeat(),
                ride.getStatus().name(),
                ride.getCreatedAt(),
                ride.getUpdatedAt()
        );
    }
}
