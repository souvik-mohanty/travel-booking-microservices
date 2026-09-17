package com.tourflow.mobility.driver.controller;

import com.tourflow.mobility.driver.domain.DriverAvailability;
import com.tourflow.mobility.driver.dto.CreateDriverRequest;
import com.tourflow.mobility.driver.dto.DriverResponse;
import com.tourflow.mobility.driver.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    // Register the authenticated user as a driver.
    @PostMapping
    public ResponseEntity<DriverResponse> createDriver(
            @Valid @RequestBody CreateDriverRequest request,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        DriverResponse response = driverService.createDriver(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Return the authenticated user's own driver profile.
    @GetMapping("/me")
    public ResponseEntity<DriverResponse> getMyDriverProfile(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                driverService.getMyDriverProfile(userId)
        );
    }

    // Return a driver profile by ID.
    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriver(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                driverService.getDriver(id)
        );
    }

    // Mark the authenticated driver as available for trips.
    @PatchMapping("/{id}/available")
    public ResponseEntity<DriverResponse> markAvailable(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                driverService.setAvailability(id, userId, DriverAvailability.AVAILABLE)
        );
    }

    // Mark the authenticated driver as unavailable for trips.
    @PatchMapping("/{id}/unavailable")
    public ResponseEntity<DriverResponse> markUnavailable(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                driverService.setAvailability(id, userId, DriverAvailability.UNAVAILABLE)
        );
    }
}
