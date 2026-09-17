package com.tourflow.insights.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

// Spring Data interface projection for BookingEventLogRepository's native
// daily-stats query -- column aliases are matched to these getters
// case-insensitively.
public interface DailyBookingStatsProjection {
    LocalDate getBookingDate();
    Long getBookingCount();
    BigDecimal getRevenue();
}
