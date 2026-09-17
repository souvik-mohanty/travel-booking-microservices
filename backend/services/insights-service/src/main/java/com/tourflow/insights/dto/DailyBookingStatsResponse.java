package com.tourflow.analytics.dto;

import com.tourflow.analytics.repository.DailyBookingStatsProjection;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyBookingStatsResponse(
        LocalDate date,
        long bookingCount,
        BigDecimal revenue
) {

    public static DailyBookingStatsResponse from(DailyBookingStatsProjection projection) {
        return new DailyBookingStatsResponse(
                projection.getBookingDate(),
                projection.getBookingCount(),
                projection.getRevenue()
        );
    }
}
