package com.tourflow.hotel.dto;

import com.tourflow.hotel.domain.Room;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record RoomResponse(
        UUID id,
        UUID hotelId,
        String roomType,
        BigDecimal pricePerNight,
        Integer capacity,
        Integer totalRooms,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static RoomResponse fromEntity(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getHotelId(),
                room.getRoomType(),
                room.getPricePerNight(),
                room.getCapacity(),
                room.getTotalRooms(),
                room.getStatus().name(),
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }
}
