package com.tourflow.tour.event;

import java.time.OffsetDateTime;
import java.util.UUID;

// Envelope every event on tour.events carries, per docs/events/EVENT-SCHEMAS.md.
// Kept in sync with booking-service's copy (each service owns its own copy
// rather than sharing a library, same convention as the JWT security trio).
public record EventEnvelope<T>(
        UUID eventId,
        String eventType,
        int version,
        OffsetDateTime occurredAt,
        String source,
        UUID correlationId,
        T payload
) {
    public static <T> EventEnvelope<T> of(String eventType, T payload) {
        return new EventEnvelope<>(
                UUID.randomUUID(),
                eventType,
                1,
                OffsetDateTime.now(),
                "tour-service",
                UUID.randomUUID(),
                payload
        );
    }
}
