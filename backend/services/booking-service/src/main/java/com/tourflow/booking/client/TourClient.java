package com.tourflow.booking.client;

import com.tourflow.booking.dto.TourResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Component
public class TourClient {

    private final RestClient restClient;

    public TourClient(@Value("${services.catalog.base-url:http://localhost:8082}") String catalogBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(catalogBaseUrl)
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
