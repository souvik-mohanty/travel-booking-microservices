package com.tourflow.mobility.trip.dto;

import com.tourflow.mobility.trip.domain.TripLogEntry;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TripLogEntryResponse(
        UUID id,
        UUID tripId,
        String type,
        String description,
        String location,
        OffsetDateTime occurredAt,
        UUID loggedBy,
        OffsetDateTime createdAt
) {

    public static TripLogEntryResponse fromEntity(TripLogEntry entry) {
        return new TripLogEntryResponse(
                entry.getId(),
                entry.getTripId(),
                entry.getType().name(),
                entry.getDescription(),
                entry.getLocation(),
                entry.getOccurredAt(),
                entry.getLoggedBy(),
                entry.getCreatedAt()
        );
    }
}
