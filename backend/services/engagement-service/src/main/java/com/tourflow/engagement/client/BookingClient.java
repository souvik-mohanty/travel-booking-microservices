package com.tourflow.review.client;

import com.tourflow.review.exception.ExternalServiceException;
import com.tourflow.review.exception.ReviewValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

// HTTP client used by Review Service to communicate with Booking Service.
// Uses the tree model (JsonNode) rather than a typed response DTO -- Booking
// Service's shape isn't locked in yet on the tourId/activityId relationship
// side, and this only needs a few fields.
@Component
public class BookingClient {

    private final RestClient restClient;
    private final String bookingServiceUrl;

    public BookingClient(
            RestClient restClient,
            @Value("${services.booking.url}") String bookingServiceUrl
    ) {
        this.restClient = restClient;
        this.bookingServiceUrl = bookingServiceUrl;
    }

    public BookingInfo getBooking(UUID bookingId) {

        try {

            JsonNode booking = restClient.get()
                    .uri(bookingServiceUrl + "/api/bookings/" + bookingId)
                    .retrieve()
                    .body(JsonNode.class);

            if (booking == null || booking.isNull()) {
                throw new ReviewValidationException(
                        "Booking not found: " + bookingId
                );
            }

            UUID userId = UUID.fromString(
                    booking.get("userId").asText()
            );

            UUID tourId = UUID.fromString(
                    booking.get("tourId").asText()
            );

            String status = booking.get("status").asText();

            return new BookingInfo(
                    bookingId,
                    userId,
                    tourId,
                    status
            );

        } catch (ReviewValidationException ex) {
            throw ex;

        } catch (Exception ex) {
            throw new ExternalServiceException(
                    "Unable to communicate with Booking Service",
                    ex
            );
        }
    }

    public record BookingInfo(
            UUID id,
            UUID userId,
            UUID tourId,
            String status
    ) {
    }
}
