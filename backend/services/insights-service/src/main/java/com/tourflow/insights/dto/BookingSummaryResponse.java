package com.tourflow.insights.dto;

import java.math.BigDecimal;

public record BookingSummaryResponse(
        long totalBookings,
        BigDecimal totalRevenue,
        String currency
) {
}
