package com.tourflow.trip.controller;

import com.tourflow.trip.dto.AssignTripRequest;
import com.tourflow.trip.dto.CreateTripRequest;
import com.tourflow.trip.dto.TripResponse;
import com.tourflow.trip.service.TripService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping
    public ResponseEntity<TripResponse> createTrip(
            @Valid @RequestBody CreateTripRequest request,
            Authentication authentication
    ) {

        UUID callerId = UUID.fromString(authentication.getName());
        String token = (String) authentication.getCredentials();

        TripResponse response = tripService.createTrip(request, callerId, token);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<TripResponse>> getMyTrips(
            Authentication authentication
    ) {

        UUID callerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                tripService.getMyTrips(callerId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<TripResponse> getTrip(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                tripService.getTrip(id)
        );
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TripResponse> assignTrip(
            @PathVariable UUID id,
            @Valid @RequestBody AssignTripRequest request,
            Authentication authentication
    ) {

        UUID callerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                tripService.assignTrip(id, callerId, request)
        );
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<TripResponse> startTrip(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID callerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                tripService.startTrip(id, callerId)
        );
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<TripResponse> completeTrip(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID callerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                tripService.completeTrip(id, callerId)
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<TripResponse> cancelTrip(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID callerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                tripService.cancelTrip(id, callerId)
        );
    }
}
