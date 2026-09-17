package com.tourflow.ride.dto;

import com.tourflow.ride.domain.RideBooking;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RideBookingResponse(
        UUID id,
        UUID rideId,
        UUID userId,
        Integer seatsBooked,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static RideBookingResponse fromEntity(RideBooking booking) {
        return new RideBookingResponse(
                booking.getId(),
                booking.getRideId(),
                booking.getUserId(),
                booking.getSeatsBooked(),
                booking.getStatus().name(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
