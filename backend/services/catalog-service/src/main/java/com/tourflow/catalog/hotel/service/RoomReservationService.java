package com.tourflow.catalog.hotel.service;

import com.tourflow.catalog.hotel.domain.Hotel;
import com.tourflow.catalog.hotel.domain.ReservationStatus;
import com.tourflow.catalog.hotel.domain.Room;
import com.tourflow.catalog.hotel.domain.RoomReservation;
import com.tourflow.catalog.hotel.domain.RoomStatus;
import com.tourflow.catalog.hotel.dto.CreateRoomReservationRequest;
import com.tourflow.catalog.hotel.dto.RoomAvailabilityResponse;
import com.tourflow.catalog.hotel.dto.RoomReservationResponse;
import com.tourflow.catalog.hotel.exception.HotelNotFoundException;
import com.tourflow.catalog.hotel.exception.RoomNotAvailableException;
import com.tourflow.catalog.hotel.exception.RoomNotFoundException;
import com.tourflow.catalog.hotel.exception.RoomReservationNotFoundException;
import com.tourflow.catalog.hotel.exception.UnauthorizedHotelAccessException;
import com.tourflow.catalog.hotel.repository.HotelRepository;
import com.tourflow.catalog.hotel.repository.RoomRepository;
import com.tourflow.catalog.hotel.repository.RoomReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RoomReservationService {

    private final RoomReservationRepository roomReservationRepository;
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public RoomReservationService(
            RoomReservationRepository roomReservationRepository,
            RoomRepository roomRepository,
            HotelRepository hotelRepository
    ) {
        this.roomReservationRepository = roomReservationRepository;
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
    }

    // Reserve N rooms of this type for a date range. The room row is locked
    // for the duration of this transaction (see RoomRepository.findByIdForUpdate)
    // so two concurrent reservations against the same room can't both read the
    // same "already reserved" sum and jointly oversell it -- same pattern as
    // ride-service's seat booking.
    @Transactional
    public RoomReservationResponse reserve(
            UUID roomId,
            CreateRoomReservationRequest request,
            UUID reservedBy
    ) {
        if (!request.startDate().isBefore(request.endDate())) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        Room room = roomRepository.findByIdForUpdate(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new RoomNotAvailableException("This room is not currently active");
        }

        int alreadyReserved = roomReservationRepository.sumReservedRooms(
                roomId, request.startDate(), request.endDate()
        );
        int available = room.getTotalRooms() - alreadyReserved;

        if (available < request.roomsRequested()) {
            throw new RoomNotAvailableException(
                    "Only " + Math.max(available, 0) + " room(s) of this type available for "
                            + request.startDate() + " to " + request.endDate()
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        RoomReservation reservation = new RoomReservation(
                UUID.randomUUID(),
                roomId,
                request.tourId(),
                request.tourLegId(),
                reservedBy,
                request.startDate(),
                request.endDate(),
                request.roomsRequested(),
                ReservationStatus.ACTIVE,
                now,
                now
        );

        RoomReservation saved = roomReservationRepository.save(reservation);

        return RoomReservationResponse.fromEntity(saved);
    }

    // Release a reservation, e.g. when a tour leg is removed or a tour is
    // cancelled. Idempotent: cancelling an already-cancelled reservation is a
    // no-op, matching the status-transition convention in
    // docs/api/API-STANDARDS.md. Only the business user who made the
    // reservation may cancel it -- they already know it exists (it's theirs),
    // so a mismatch is 403, not 404.
    @Transactional
    public RoomReservationResponse cancel(UUID reservationId, UUID callerId) {

        RoomReservation reservation = roomReservationRepository.findById(reservationId)
                .orElseThrow(() -> new RoomReservationNotFoundException("Reservation not found"));

        if (!reservation.getReservedBy().equals(callerId)) {
            throw new UnauthorizedHotelAccessException(
                    "You are not authorized to cancel this reservation"
            );
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            return RoomReservationResponse.fromEntity(reservation);
        }

        // Locks the room row too, even though this reservation's own row is
        // what's mutated -- keeps every write that affects a room's computed
        // availability serialized through the same lock a concurrent reserve()
        // would also be waiting on.
        roomRepository.findByIdForUpdate(reservation.getRoomId())
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(OffsetDateTime.now());

        return RoomReservationResponse.fromEntity(roomReservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public RoomAvailabilityResponse getAvailability(UUID roomId, LocalDate startDate, LocalDate endDate) {

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        int reserved = roomReservationRepository.sumReservedRooms(roomId, startDate, endDate);
        int available = Math.max(room.getTotalRooms() - reserved, 0);

        return new RoomAvailabilityResponse(
                roomId, startDate, endDate, room.getTotalRooms(), reserved, available
        );
    }

    // Lets a hotel owner see which tours have reserved their room. Ownership-
    // gated the same way requireOwnedRoom is in RoomService.
    @Transactional(readOnly = true)
    public List<RoomReservationResponse> getReservationsForRoom(UUID roomId, UUID ownerId) {

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RoomNotFoundException("Room not found"));

        Hotel hotel = hotelRepository.findById(room.getHotelId())
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));

        if (!hotel.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedHotelAccessException(
                    "You are not authorized to view this room's reservations"
            );
        }

        return roomReservationRepository.findByRoomId(roomId)
                .stream()
                .map(RoomReservationResponse::fromEntity)
                .toList();
    }
}
