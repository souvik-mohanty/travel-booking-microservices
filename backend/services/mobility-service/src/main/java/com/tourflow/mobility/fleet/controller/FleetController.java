package com.tourflow.mobility.fleet.controller;

import com.tourflow.mobility.fleet.domain.VehicleStatus;
import com.tourflow.mobility.fleet.dto.CreateVehicleRequest;
import com.tourflow.mobility.fleet.dto.UpdateVehicleRequest;
import com.tourflow.mobility.fleet.dto.VehicleResponse;
import com.tourflow.mobility.fleet.service.FleetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vehicles")
public class FleetController {

    private final FleetService fleetService;

    public FleetController(FleetService fleetService) {
        this.fleetService = fleetService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        VehicleResponse response = fleetService.createVehicle(request, ownerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<VehicleResponse>> getVehicles() {

        return ResponseEntity.ok(
                fleetService.getVehicles()
        );
    }

    @GetMapping("/mine")
    public ResponseEntity<List<VehicleResponse>> getMyVehicles(
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                fleetService.getMyVehicles(ownerId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleResponse> getVehicle(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                fleetService.getVehicle(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> updateVehicle(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateVehicleRequest request,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                fleetService.updateVehicle(id, ownerId, request)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<VehicleResponse> setStatus(
            @PathVariable UUID id,
            @RequestParam VehicleStatus status,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                fleetService.setStatus(id, ownerId, status)
        );
    }
}
