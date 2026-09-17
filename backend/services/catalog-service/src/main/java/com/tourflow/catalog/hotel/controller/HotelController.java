package com.tourflow.hotel.controller;

import com.tourflow.hotel.dto.CreateHotelRequest;
import com.tourflow.hotel.dto.HotelResponse;
import com.tourflow.hotel.dto.RoomResponse;
import com.tourflow.hotel.dto.UpdateHotelRequest;
import com.tourflow.hotel.service.HotelService;
import com.tourflow.hotel.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final RoomService roomService;

    public HotelController(
            HotelService hotelService,
            RoomService roomService
    ) {
        this.hotelService = hotelService;
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<HotelResponse> createHotel(
            @Valid @RequestBody CreateHotelRequest request,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        HotelResponse response = hotelService.createHotel(request, ownerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<HotelResponse>> getHotels() {

        return ResponseEntity.ok(
                hotelService.getHotels()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<HotelResponse> getHotel(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                hotelService.getHotel(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<HotelResponse> updateHotel(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateHotelRequest request,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                hotelService.updateHotel(id, ownerId, request)
        );
    }

    // Return all rooms offered by a hotel.
    @GetMapping("/{hotelId}/rooms")
    public ResponseEntity<List<RoomResponse>> getHotelRooms(
            @PathVariable UUID hotelId
    ) {

        return ResponseEntity.ok(
                roomService.getRoomsForHotel(hotelId)
        );
    }
}
