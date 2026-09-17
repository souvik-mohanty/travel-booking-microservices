package com.tourflow.tracking.controller;

import com.tourflow.tracking.dto.RecordLocationRequest;
import com.tourflow.tracking.dto.TripLocationResponse;
import com.tourflow.tracking.service.TrackingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tracking/{tripId}")
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/locations")
    public ResponseEntity<TripLocationResponse> recordLocation(
            @PathVariable UUID tripId,
            @Valid @RequestBody RecordLocationRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(trackingService.recordLocation(tripId, request));
    }

    @GetMapping("/locations/latest")
    public ResponseEntity<TripLocationResponse> getLatestLocation(
            @PathVariable UUID tripId
    ) {

        return ResponseEntity.ok(
                trackingService.getLatestLocation(tripId)
        );
    }

    @GetMapping("/locations/history")
    public ResponseEntity<List<TripLocationResponse>> getLocationHistory(
            @PathVariable UUID tripId
    ) {

        return ResponseEntity.ok(
                trackingService.getLocationHistory(tripId)
        );
    }
}
