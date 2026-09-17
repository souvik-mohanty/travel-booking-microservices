package com.tourflow.catalog.hotel.service;

import com.tourflow.catalog.hotel.domain.Hotel;
import com.tourflow.catalog.hotel.domain.Room;
import com.tourflow.catalog.hotel.domain.RoomStatus;
import com.tourflow.catalog.hotel.dto.CreateRoomRequest;
import com.tourflow.catalog.hotel.dto.RoomResponse;
import com.tourflow.catalog.hotel.dto.UpdateRoomRequest;
import com.tourflow.catalog.hotel.exception.HotelNotFoundException;
import com.tourflow.catalog.hotel.exception.RoomNotFoundException;
import com.tourflow.catalog.hotel.exception.UnauthorizedHotelAccessException;
import com.tourflow.catalog.hotel.repository.HotelRepository;
import com.tourflow.catalog.hotel.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomService(
            RoomRepository roomRepository,
            HotelRepository hotelRepository
    ) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    // Create a new room type under a hotel owned by the authenticated user.
    @Transactional
    public RoomResponse createRoom(
            CreateRoomRequest request,
            UUID ownerId
    ) {

        Hotel hotel = hotelRepository.findById(request.hotelId())
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));

        // A non-owner gets the same "not found" as a nonexistent hotel --
        // this must not confirm the hotel exists to someone who doesn't own it.
        if (!hotel.getOwnerId().equals(ownerId)) {
            throw new HotelNotFoundException("Hotel not found");
        }

        OffsetDateTime now = OffsetDateTime.now();

        Room room = new Room(
                UUID.randomUUID(),
                hotel.getId(),
                request.roomType(),
                request.pricePerNight(),
                request.capacity(),
                request.totalRooms(),

                // Not customer-visible until explicitly activated later.
                RoomStatus.DRAFT,

                now,
                now
        );

        Room saved = roomRepository.save(room);

        return RoomResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoom(UUID id) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        return RoomResponse.fromEntity(room);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getRooms() {

        return roomRepository.findAll()
                .stream()
                .map(RoomResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> getRoomsForHotel(UUID hotelId) {

        return roomRepository.findByHotelId(hotelId)
                .stream()
                .map(RoomResponse::fromEntity)
                .toList();
    }

    // Public search: only rooms a tourist is allowed to book.
    @Transactional(readOnly = true)
    public List<RoomResponse> getActiveRooms() {

        return roomRepository.findByStatus(RoomStatus.ACTIVE)
                .stream()
                .map(RoomResponse::fromEntity)
                .toList();
    }

    @Transactional
    public RoomResponse updateRoom(
            UUID roomId,
            UUID ownerId,
            UpdateRoomRequest request
    ) {

        Room room = requireOwnedRoom(roomId, ownerId);

        room.update(
                request.roomType(),
                request.pricePerNight(),
                request.capacity(),
                request.totalRooms()
        );

        return RoomResponse.fromEntity(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse deleteRoom(UUID roomId, UUID ownerId) {

        Room room = requireOwnedRoom(roomId, ownerId);

        if (room.getStatus() == RoomStatus.DELETED) {
            return RoomResponse.fromEntity(room);
        }

        room.setStatus(RoomStatus.DELETED);
        room.setUpdatedAt(OffsetDateTime.now());

        return RoomResponse.fromEntity(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse activateRoom(UUID roomId, UUID ownerId) {

        Room room = requireOwnedRoom(roomId, ownerId);

        if (room.getStatus() == RoomStatus.ACTIVE) {
            return RoomResponse.fromEntity(room);
        }

        room.setStatus(RoomStatus.ACTIVE);
        room.setUpdatedAt(OffsetDateTime.now());

        return RoomResponse.fromEntity(roomRepository.save(room));
    }

    @Transactional
    public RoomResponse deactivateRoom(UUID roomId, UUID ownerId) {

        Room room = requireOwnedRoom(roomId, ownerId);

        if (room.getStatus() == RoomStatus.INACTIVE) {
            return RoomResponse.fromEntity(room);
        }

        room.setStatus(RoomStatus.INACTIVE);
        room.setUpdatedAt(OffsetDateTime.now());

        return RoomResponse.fromEntity(roomRepository.save(room));
    }

    // Load a room and verify the caller owns the hotel it belongs to. The
    // caller already knows this room exists (they have its ID from a prior
    // create/list call), so an ownership mismatch is reported as 403, not
    // folded into a 404 the way hotel-existence is hidden elsewhere.
    private Room requireOwnedRoom(UUID roomId, UUID ownerId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        Hotel hotel = hotelRepository.findById(room.getHotelId())
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));

        if (!hotel.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedHotelAccessException(
                    "You are not authorized to manage this room"
            );
        }

        return room;
    }
}
