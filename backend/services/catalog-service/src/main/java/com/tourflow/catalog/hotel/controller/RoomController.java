package com.tourflow.catalog.hotel.controller;

import com.tourflow.catalog.hotel.dto.CreateRoomRequest;
import com.tourflow.catalog.hotel.dto.RoomResponse;
import com.tourflow.catalog.hotel.dto.UpdateRoomRequest;
import com.tourflow.catalog.hotel.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        RoomResponse response = roomService.createRoom(request, ownerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<RoomResponse>> getRooms() {

        return ResponseEntity.ok(
                roomService.getRooms()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoom(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                roomService.getRoom(id)
        );
    }

    // Public search: only rooms a tourist is allowed to book.
    @GetMapping("/active")
    public ResponseEntity<List<RoomResponse>> getActiveRooms() {

        return ResponseEntity.ok(
                roomService.getActiveRooms()
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoomRequest request,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                roomService.updateRoom(id, ownerId, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RoomResponse> deleteRoom(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                roomService.deleteRoom(id, ownerId)
        );
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<RoomResponse> activateRoom(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                roomService.activateRoom(id, ownerId)
        );
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<RoomResponse> deactivateRoom(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                roomService.deactivateRoom(id, ownerId)
        );
    }
}
