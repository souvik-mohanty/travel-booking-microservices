package com.tourflow.catalog.controller;

import com.tourflow.catalog.dto.CreateTourLegRequest;
import com.tourflow.catalog.dto.TourLegResponse;
import com.tourflow.catalog.service.TourLegService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tours/{tourId}/legs")
public class TourLegController {

    private final TourLegService tourLegService;

    public TourLegController(TourLegService tourLegService) {
        this.tourLegService = tourLegService;
    }

    @PostMapping
    public ResponseEntity<TourLegResponse> addLeg(
            @PathVariable UUID tourId,
            @Valid @RequestBody CreateTourLegRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tourLegService.addLeg(tourId, request, userId));
    }

    @GetMapping
    public ResponseEntity<List<TourLegResponse>> getLegs(@PathVariable UUID tourId) {
        return ResponseEntity.ok(tourLegService.getLegsForTour(tourId));
    }

    @DeleteMapping("/{legId}")
    public ResponseEntity<Void> removeLeg(
            @PathVariable UUID tourId,
            @PathVariable UUID legId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        tourLegService.removeLeg(tourId, legId, userId);
        return ResponseEntity.noContent().build();
    }
}
