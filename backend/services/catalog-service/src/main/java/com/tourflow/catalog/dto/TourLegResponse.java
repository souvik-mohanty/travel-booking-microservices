package com.tourflow.catalog.dto;

import com.tourflow.catalog.domain.TourLeg;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TourLegResponse(
        UUID id,
        UUID tourId,
        Integer sequenceOrder,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        UUID hotelId,
        UUID roomId,
        Integer roomsBooked,
        OffsetDateTime createdAt
) {

    // tourDestination is the parent Tour's own destination -- used when the
    // leg didn't override it with its own.
    public static TourLegResponse from(TourLeg leg, String tourDestination) {
        return new TourLegResponse(
                leg.getId(),
                leg.getTourId(),
                leg.getSequenceOrder(),
                leg.getDestination() != null ? leg.getDestination() : tourDestination,
                leg.getStartDate(),
                leg.getEndDate(),
                leg.getHotelId(),
                leg.getRoomId(),
                leg.getRoomsBooked(),
                leg.getCreatedAt()
        );
    }
}
