package com.tourflow.catalog.hotel.controller;

import com.tourflow.catalog.hotel.dto.CreateRoomReservationRequest;
import com.tourflow.catalog.hotel.dto.RoomAvailabilityResponse;
import com.tourflow.catalog.hotel.dto.RoomReservationResponse;
import com.tourflow.catalog.hotel.service.RoomReservationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms/{roomId}")
public class RoomReservationController {

    private final RoomReservationService roomReservationService;

    public RoomReservationController(RoomReservationService roomReservationService) {
        this.roomReservationService = roomReservationService;
    }

    // Called by tour-service (forwarding the tour creator's own JWT, not a
    // service-role token -- there's a real user behind this call) when a
    // business links a room to a tour/leg.
    @PostMapping("/reservations")
    public ResponseEntity<RoomReservationResponse> reserveRoom(
            @PathVariable UUID roomId,
            @Valid @RequestBody CreateRoomReservationRequest request,
            Authentication authentication
    ) {
        UUID reservedBy = UUID.fromString(authentication.getName());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(roomReservationService.reserve(roomId, request, reservedBy));
    }

    // Called by tour-service when a leg/tour referencing this room is removed
    // or cancelled.
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<RoomReservationResponse> cancelReservation(
            @PathVariable UUID roomId,
            @PathVariable UUID reservationId,
            Authentication authentication
    ) {
        UUID callerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                roomReservationService.cancel(reservationId, callerId)
        );
    }

    // How many of this room type are free for a date range -- checked by
    // tour-service before it commits to a leg, and usable directly by the
    // frontend to show live availability while a business builds an itinerary.
    @GetMapping("/availability")
    public ResponseEntity<RoomAvailabilityResponse> getAvailability(
            @PathVariable UUID roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(
                roomReservationService.getAvailability(roomId, startDate, endDate)
        );
    }

    // Hotel-owner visibility: which tours have reserved this room.
    @GetMapping("/reservations")
    public ResponseEntity<List<RoomReservationResponse>> getReservationsForRoom(
            @PathVariable UUID roomId,
            Authentication authentication
    ) {
        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                roomReservationService.getReservationsForRoom(roomId, ownerId)
        );
    }
}
