package com.tourflow.mobility.tracking.dto;

import com.tourflow.mobility.tracking.domain.TripLocation;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TripLocationResponse(
        UUID id,
        UUID tripId,
        BigDecimal latitude,
        BigDecimal longitude,
        OffsetDateTime recordedAt
) {

    public static TripLocationResponse fromEntity(TripLocation location) {
        return new TripLocationResponse(
                location.getId(),
                location.getTripId(),
                location.getLatitude(),
                location.getLongitude(),
                location.getRecordedAt()
        );
    }
}
