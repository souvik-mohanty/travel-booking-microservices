package com.tourflow.catalog.controller;

import com.tourflow.catalog.dto.AddTourActivityRequest;
import com.tourflow.catalog.dto.TourActivityResponse;
import com.tourflow.catalog.service.TourActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tours/{tourId}/activities")
public class TourActivityController {

    private final TourActivityService tourActivityService;

    public TourActivityController(TourActivityService tourActivityService) {
        this.tourActivityService = tourActivityService;
    }

    @PostMapping
    public ResponseEntity<TourActivityResponse> addActivity(
            @PathVariable UUID tourId,
            @Valid @RequestBody AddTourActivityRequest request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tourActivityService.addActivity(tourId, request, userId));
    }

    @GetMapping
    public ResponseEntity<List<TourActivityResponse>> getActivities(@PathVariable UUID tourId) {
        return ResponseEntity.ok(tourActivityService.getActivitiesForTour(tourId));
    }

    @DeleteMapping("/{activityId}")
    public ResponseEntity<Void> removeActivity(
            @PathVariable UUID tourId,
            @PathVariable UUID activityId,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        tourActivityService.removeActivity(tourId, activityId, userId);
        return ResponseEntity.noContent().build();
    }
}
