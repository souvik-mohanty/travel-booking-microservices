package com.tourflow.platform.event;

import com.tourflow.platform.dto.CreateNotificationRequest;
import com.tourflow.platform.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

// Consumes booking-service's booking.events topic and turns a
// BookingConfirmed event into an in-app notification for the booking's
// user -- the first real Kafka consumer in the project, and the first time
// a notification gets created automatically rather than via a direct API
// call (see docs/events/EVENT-CATALOG.md's "what would actually change").
//
// Parses the envelope as a JsonNode tree rather than a typed record, same
// reasoning as review-service's BookingClient: this only needs a couple of
// fields, and staying loosely coupled to booking-service's exact payload
// shape avoids two services needing to change in lockstep.
@Component
public class BookingEventListener {

    private static final Logger log = LoggerFactory.getLogger(BookingEventListener.class);

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public BookingEventListener(NotificationService notificationService, ObjectMapper objectMapper) {
        this.notificationService = notificationService;
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

            JsonNode payload = envelope.get("payload");
            UUID userId = UUID.fromString(payload.get("userId").asText());
            UUID bookingId = UUID.fromString(payload.get("bookingId").asText());

            notificationService.createNotification(new CreateNotificationRequest(
                    userId,
                    "Booking confirmed",
                    "Your booking " + bookingId + " has been confirmed. Payment received, enjoy your trip!"
            ));

        } catch (Exception ex) {
            // Best-effort: a malformed or unprocessable event is logged and
            // skipped rather than blocking the consumer on a poison pill --
            // there's no dead-letter topic wired up yet.
            log.warn("Failed to process booking event: {}", message, ex);
        }
    }
}
