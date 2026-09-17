package com.tourflow.booking.dto;

import com.tourflow.booking.domain.Booking;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// Response returned by the booking API.
public record BookingResponse(
        UUID id,
        UUID tourId,
        UUID userId,
        Integer numberOfParticipants,
        BigDecimal totalPrice,
        String status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    // Convert a Booking entity into an API response.
    public static BookingResponse fromEntity(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getTourId(),
                booking.getUserId(),
                booking.getNumberOfParticipants(),
                booking.getTotalPrice(),
                booking.getStatus().name(),
                booking.getCreatedAt(),
                booking.getUpdatedAt()
        );
    }
}
