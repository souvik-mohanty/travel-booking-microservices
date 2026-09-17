package com.tourflow.mobility.trip.client;

import com.tourflow.mobility.trip.dto.TourResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

// HTTP client used by Trip Service to communicate with Tour Service --
// resolves a tour's owning business for logbook access control. Mirrors
// BookingClient's exact shape.
@Component
public class TourClient {

    private final RestClient restClient;

    public TourClient(
            @Value("${services.tour.url}") String tourBaseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(tourBaseUrl)
                .build();
    }

    // Forward the caller's JWT because Tour Service requires authentication.
    public TourResponse getTour(UUID tourId, String token) {

        return restClient
                .get()
                .uri("/api/tours/{id}", tourId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(TourResponse.class);
    }
}
