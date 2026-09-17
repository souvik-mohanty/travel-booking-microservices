package com.tourflow.booking.client;

import com.tourflow.booking.dto.TourResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class TourClient {

    private final RestClient restClient;

    public TourClient() {
        this.restClient = RestClient.builder()
                .baseUrl("http://localhost:8082")
                .build();
    }

    public TourResponse getTour(UUID tourId, String token) {

        return restClient
                .get()
                .uri("/api/tours/{id}", tourId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(TourResponse.class);
    }
}
