package com.tourflow.insights.event;

import com.tourflow.insights.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Parses the envelope as a JsonNode tree rather than a typed record, same as
// notification-service's BookingEventListener -- keeps this consumer loosely
// coupled to the producer's exact payload shape.
@Component
public class BookingEventListener {

    private static final Logger log = LoggerFactory.getLogger(BookingEventListener.class);

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    public BookingEventListener(AnalyticsService analyticsService, ObjectMapper objectMapper) {
        this.analyticsService = analyticsService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "booking.events", groupId = "${spring.kafka.consumer.group-id}")
    public void onBookingEvent(String message) {
        try {
            JsonNode envelope = objectMapper.readTree(message);
            String eventType = envelope.get("eventType").asText();

            if (!"BookingConfirmed".equals(eventType)) {
                return;
            }

            UUID eventId = UUID.fromString(envelope.get("eventId").asText());
            OffsetDateTime occurredAt = OffsetDateTime.parse(envelope.get("occurredAt").asText());
            JsonNode payload = envelope.get("payload");

            analyticsService.recordBookingConfirmed(
                    eventId,
                    UUID.fromString(payload.get("bookingId").asText()),
                    UUID.fromString(payload.get("tourId").asText()),
                    UUID.fromString(payload.get("userId").asText()),
                    decimal(payload.get("totalPrice")),
                    payload.get("currency").asText(),
                    occurredAt
            );

        } catch (Exception ex) {
            // Best-effort: no dead-letter topic wired up yet, same as
            // notification-service's BookingEventListener.
            log.warn("Failed to process booking event: {}", message, ex);
        }
    }

    // BigDecimal fields may arrive as a JSON number or a JSON string
    // depending on the producer's serializer config -- handle both rather
    // than assuming one.
    private BigDecimal decimal(JsonNode node) {
        return node.isNumber() ? node.decimalValue() : new BigDecimal(node.asText());
    }
}
