package com.tourflow.catalog.event;

import com.tourflow.catalog.domain.Tour;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

// Publishes to tour.events, consumed today by search-service. A publish
// failure is caught and logged, never thrown -- a Kafka outage can't break
// the publish/cancel request itself, matching booking-service's
// BookingEventPublisher.
@Component
public class TourEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TourEventPublisher.class);
    private static final String TOPIC = "tour.events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public TourEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishTourPublished(Tour tour) {
        TourPublishedPayload payload = new TourPublishedPayload(
                tour.getId(),
                tour.getTitle(),
                tour.getDescription(),
                tour.getDestination(),
                tour.getStartDate(),
                tour.getEndDate(),
                tour.getPrice(),
                tour.getMaxParticipants(),
                tour.getCreatedBy()
        );

        publish(tour.getId(), EventEnvelope.of("TourPublished", payload));
    }

    public void publishTourCancelled(Tour tour) {
        TourCancelledPayload payload = new TourCancelledPayload(tour.getId());

        publish(tour.getId(), EventEnvelope.of("TourCancelled", payload));
    }

    private void publish(UUID key, EventEnvelope<?> envelope) {
        try {
            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(TOPIC, key.toString(), json);
        } catch (Exception ex) {
            log.warn("Failed to publish {} event for tour {}", envelope.eventType(), key, ex);
        }
    }
}
