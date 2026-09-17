package com.tourflow.hotel.dto;

import com.tourflow.hotel.domain.Hotel;

import java.time.OffsetDateTime;
import java.util.UUID;

public record HotelResponse(
        UUID id,
        UUID ownerId,
        String name,
        String description,
        String address,
        String city,
        String state,
        String country,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static HotelResponse fromEntity(Hotel hotel) {
        return new HotelResponse(
                hotel.getId(),
                hotel.getOwnerId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getAddress(),
                hotel.getCity(),
                hotel.getState(),
                hotel.getCountry(),
                hotel.getStatus().name(),
                hotel.getCreatedAt(),
                hotel.getUpdatedAt()
        );
    }
}
