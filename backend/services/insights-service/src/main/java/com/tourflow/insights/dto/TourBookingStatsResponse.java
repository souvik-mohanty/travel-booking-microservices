package com.tourflow.insights.dto;

import com.tourflow.insights.repository.TourBookingStatsProjection;

import java.math.BigDecimal;
import java.util.UUID;

public record TourBookingStatsResponse(
        UUID tourId,
        long bookingCount,
        BigDecimal revenue
) {

    public static TourBookingStatsResponse from(TourBookingStatsProjection projection) {
        return new TourBookingStatsResponse(
                projection.getTourId(),
                projection.getBookingCount(),
                projection.getRevenue()
        );
    }
}
