package com.tourflow.catalog.hotel.dto;

import com.tourflow.catalog.hotel.domain.RoomReservation;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RoomReservationResponse(
        UUID id,
        UUID roomId,
        UUID tourId,
        UUID tourLegId,
        UUID reservedBy,
        LocalDate startDate,
        LocalDate endDate,
        Integer roomsReserved,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static RoomReservationResponse fromEntity(RoomReservation reservation) {
        return new RoomReservationResponse(
                reservation.getId(),
                reservation.getRoomId(),
                reservation.getTourId(),
                reservation.getTourLegId(),
                reservation.getReservedBy(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getRoomsReserved(),
                reservation.getStatus().name(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
