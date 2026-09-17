package com.tourflow.mobility.trip.controller;

import com.tourflow.mobility.trip.dto.CreateTripLogEntryRequest;
import com.tourflow.mobility.trip.dto.TripLogEntryResponse;
import com.tourflow.mobility.trip.service.TripLogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips/{tripId}/log")
public class TripLogController {

    private final TripLogService tripLogService;

    public TripLogController(TripLogService tripLogService) {
        this.tripLogService = tripLogService;
    }

    // Business-role gate is enforced in SecurityConfig; the exact-tour-owner
    // check happens in TripLogService.
    @PostMapping
    public ResponseEntity<TripLogEntryResponse> addEntry(
            @PathVariable UUID tripId,
            @Valid @RequestBody CreateTripLogEntryRequest request,
            Authentication authentication
    ) {
        UUID callerId = UUID.fromString(authentication.getName());
        String token = (String) authentication.getCredentials();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tripLogService.addEntry(tripId, request, callerId, token));
    }

    // Readable by the tourist who booked the trip, the business that owns
    // its tour, or any admin.
    @GetMapping
    public ResponseEntity<List<TripLogEntryResponse>> getLog(
            @PathVariable UUID tripId,
            Authentication authentication
    ) {
        UUID callerId = UUID.fromString(authentication.getName());
        String token = (String) authentication.getCredentials();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        return ResponseEntity.ok(
                tripLogService.getLog(tripId, callerId, isAdmin, token)
        );
    }
}
