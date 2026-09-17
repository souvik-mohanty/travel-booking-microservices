package com.tourflow.search.event;

import com.tourflow.search.document.TourDocument;
import com.tourflow.search.repository.TourSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.UUID;

// Keeps the "tours" Elasticsearch index in sync with tour-service. Parses
// the envelope as a JsonNode tree rather than a typed record, same as
// notification-service's BookingEventListener -- keeps this consumer loosely
// coupled to the producer's exact payload shape.
@Component
public class TourEventListener {

    private static final Logger log = LoggerFactory.getLogger(TourEventListener.class);

    private final TourSearchRepository tourSearchRepository;
    private final ObjectMapper objectMapper;

    public TourEventListener(TourSearchRepository tourSearchRepository, ObjectMapper objectMapper) {
        this.tourSearchRepository = tourSearchRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "tour.events", groupId = "${spring.kafka.consumer.group-id}")
    public void onTourEvent(String message) {
        try {
            JsonNode envelope = objectMapper.readTree(message);
            String eventType = envelope.get("eventType").asText();
            JsonNode payload = envelope.get("payload");

            switch (eventType) {
                case "TourPublished" -> index(payload);
                case "TourCancelled" -> remove(payload);
                default -> { /* not something search-service cares about */ }
            }
        } catch (Exception ex) {
            // Best-effort: no dead-letter topic wired up yet, same as
            // notification-service's BookingEventListener.
            log.warn("Failed to process tour event: {}", message, ex);
        }
    }

    private void index(JsonNode payload) {
        TourDocument document = new TourDocument(
                UUID.fromString(payload.get("tourId").asText()),
                payload.get("title").asText(),
                payload.hasNonNull("description") ? payload.get("description").asText() : null,
                payload.get("destination").asText(),
                LocalDate.parse(payload.get("startDate").asText()),
                LocalDate.parse(payload.get("endDate").asText()),
                payload.get("price").asDouble(),
                payload.get("maxParticipants").asInt(),
                UUID.fromString(payload.get("createdBy").asText())
        );

        tourSearchRepository.save(document);
    }

    private void remove(JsonNode payload) {
        tourSearchRepository.deleteById(payload.get("tourId").asText());
    }
}
