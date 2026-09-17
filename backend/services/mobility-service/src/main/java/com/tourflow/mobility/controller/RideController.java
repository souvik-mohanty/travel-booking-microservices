package com.tourflow.mobility.controller;

import com.tourflow.mobility.dto.CreateRideRequest;
import com.tourflow.mobility.dto.RideBookingResponse;
import com.tourflow.mobility.dto.RideResponse;
import com.tourflow.mobility.service.RideBookingService;
import com.tourflow.mobility.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;
    private final RideBookingService rideBookingService;

    public RideController(
            RideService rideService,
            RideBookingService rideBookingService
    ) {
        this.rideService = rideService;
        this.rideBookingService = rideBookingService;
    }

    @PostMapping
    public ResponseEntity<RideResponse> createRide(
            @Valid @RequestBody CreateRideRequest request,
            Authentication authentication
    ) {

        UUID createdBy = UUID.fromString(authentication.getName());

        RideResponse response = rideService.createRide(request, createdBy);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<RideResponse>> getRides() {

        return ResponseEntity.ok(
                rideService.getRides()
        );
    }

    // Public search: only rides a tourist can still book a seat on.
    @GetMapping("/available")
    public ResponseEntity<List<RideResponse>> getAvailableRides() {

        return ResponseEntity.ok(
                rideService.getAvailableRides()
        );
    }

    @GetMapping("/mine")
    public ResponseEntity<List<RideResponse>> getMyRides(
            Authentication authentication
    ) {

        UUID createdBy = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                rideService.getMyRides(createdBy)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideResponse> getRide(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                rideService.getRide(id)
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RideResponse> cancelRide(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID createdBy = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                rideService.cancelRide(id, createdBy)
        );
    }

    // Return all seat bookings for a ride.
    @GetMapping("/{rideId}/bookings")
    public ResponseEntity<List<RideBookingResponse>> getBookingsForRide(
            @PathVariable UUID rideId
    ) {

        return ResponseEntity.ok(
                rideBookingService.getBookingsForRide(rideId)
        );
    }
}
