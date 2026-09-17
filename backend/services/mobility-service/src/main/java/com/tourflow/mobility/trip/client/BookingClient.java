package com.tourflow.trip.client;

import com.tourflow.trip.dto.BookingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

// HTTP client used by Trip Service to communicate with Booking Service.
@Component
public class BookingClient {

    private final RestClient restClient;

    public BookingClient(
            @Value("${services.booking.url}") String bookingBaseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(bookingBaseUrl)
                .build();
    }

    // Forward the caller's JWT because Booking Service requires authentication.
    public BookingResponse getBooking(UUID bookingId, String token) {

        return restClient
                .get()
                .uri("/api/bookings/{id}", bookingId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(BookingResponse.class);
    }
}
