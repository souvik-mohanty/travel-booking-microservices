package com.tourflow.mobility.controller;

import com.tourflow.mobility.dto.BookSeatsRequest;
import com.tourflow.mobility.dto.RideBookingResponse;
import com.tourflow.mobility.service.RideBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ride-bookings")
public class RideBookingController {

    private final RideBookingService rideBookingService;

    public RideBookingController(RideBookingService rideBookingService) {
        this.rideBookingService = rideBookingService;
    }

    @PostMapping
    public ResponseEntity<RideBookingResponse> bookSeats(
            @Valid @RequestBody BookSeatsRequest request,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        RideBookingResponse response = rideBookingService.bookSeats(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<RideBookingResponse>> getMyBookings(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                rideBookingService.getMyBookings(userId)
        );
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<RideBookingResponse> cancelBooking(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                rideBookingService.cancelBooking(id, userId)
        );
    }
}
