package com.tourflow.catalog.hotel.dto;

import java.time.LocalDate;
import java.util.UUID;

public record RoomAvailabilityResponse(
        UUID roomId,
        LocalDate startDate,
        LocalDate endDate,
        int totalRooms,
        int reservedRooms,
        int availableRooms
) {
}
