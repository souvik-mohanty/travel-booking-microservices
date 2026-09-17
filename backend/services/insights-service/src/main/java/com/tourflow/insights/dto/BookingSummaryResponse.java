package com.tourflow.analytics.dto;

import java.math.BigDecimal;

public record BookingSummaryResponse(
        long totalBookings,
        BigDecimal totalRevenue,
        String currency
) {
}
