package com.tourflow.booking.event;

import java.time.OffsetDateTime;
import java.util.UUID;

// Common envelope every event published to Kafka carries, per
// docs/events/EVENT-SCHEMAS.md -- kept in sync with notification-service's
// copy (each service owns its own copy rather than sharing a library, same
// convention as the JWT security trio; see ADR-001).
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
                "booking-service",
                UUID.randomUUID(),
                payload
        );
    }
}
