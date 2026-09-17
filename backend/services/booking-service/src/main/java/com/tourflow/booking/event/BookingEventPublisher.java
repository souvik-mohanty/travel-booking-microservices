package com.tourflow.booking.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import com.tourflow.booking.domain.Booking;

// Publishes booking-service's domain events to the booking.events topic,
// per docs/events/EVENT-CATALOG.md. Best-effort: a publish failure is
// logged, not thrown, so a Kafka outage never breaks the booking flow
// itself (the mark-paid REST call to the caller is still the source of
// truth -- this is a supplementary notification channel, not a second
// consistency mechanism; see EVENT-CATALOG.md's note on why the
// synchronous mark-paid call stays even after this exists).
@Component
public class BookingEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BookingEventPublisher.class);
    private static final String TOPIC = "booking.events";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public BookingEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishBookingConfirmed(Booking booking) {

        BookingConfirmedPayload payload = new BookingConfirmedPayload(
                booking.getId(),
                booking.getTourId(),
                booking.getUserId(),
                booking.getTotalPrice(),
                "INR"
        );

        EventEnvelope<BookingConfirmedPayload> envelope = EventEnvelope.of("BookingConfirmed", payload);

        try {
            String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(TOPIC, booking.getId().toString(), json);
        } catch (Exception ex) {
            log.warn("Failed to publish BookingConfirmed event for booking {}", booking.getId(), ex);
        }
    }
}
