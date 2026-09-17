package com.tourflow.catalog.controller;

import com.tourflow.catalog.dto.CreateTourRequest;
import com.tourflow.catalog.dto.TourResponse;
import com.tourflow.catalog.service.TourService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// REST controller responsible for tour management.
@RestController
@RequestMapping("/api/tours")
public class TourController {

    private final TourService tourService;

    public TourController(TourService tourService) {
        this.tourService = tourService;
    }

    // Create a new tour.
    @PostMapping
    public ResponseEntity<TourResponse> createTour(
            @Valid @RequestBody CreateTourRequest request,
            Authentication authentication
    ) {
        // JwtAuthenticationFilter stores the user's UUID as the
        // authenticated principal.
        UUID userId = UUID.fromString(authentication.getName());

        TourResponse response = tourService.createTour(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Return a single tour by ID.
    @GetMapping("/{id}")
    public ResponseEntity<TourResponse> getTour(
            @PathVariable UUID id
    ) {
        return ResponseEntity.ok(
                tourService.getTour(id)
        );
    }

    // Return all tours.
    @GetMapping
    public ResponseEntity<List<TourResponse>> getAllTours() {
        return ResponseEntity.ok(tourService.getAllTours());
    }

    // Publish a DRAFT tour. Publishes TourPublished to Kafka so search-service
    // can index it -- see TourEventPublisher.
    @PatchMapping("/{id}/publish")
    public ResponseEntity<TourResponse> publishTour(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                tourService.publishTour(id, userId)
        );
    }

    // Cancel a tour. Publishes TourCancelled to Kafka so search-service can
    // remove it from its index.
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TourResponse> cancelTour(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                tourService.cancelTour(id, userId)
        );
    }
}