package com.tourflow.catalog.dto;

import com.tourflow.catalog.domain.TourActivity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TourActivityResponse(
        UUID id,
        UUID tourId,
        UUID activityId,
        LocalDate scheduledDate,
        LocalTime scheduledTime,
        OffsetDateTime createdAt
) {

    public static TourActivityResponse from(TourActivity tourActivity) {
        return new TourActivityResponse(
                tourActivity.getId(),
                tourActivity.getTourId(),
                tourActivity.getActivityId(),
                tourActivity.getScheduledDate(),
                tourActivity.getScheduledTime(),
                tourActivity.getCreatedAt()
        );
    }
}
