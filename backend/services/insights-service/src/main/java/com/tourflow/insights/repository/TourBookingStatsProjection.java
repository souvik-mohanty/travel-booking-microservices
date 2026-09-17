package com.tourflow.insights.repository;

import java.math.BigDecimal;
import java.util.UUID;

// Spring Data interface projection for BookingEventLogRepository's
// group-by-tour query.
public interface TourBookingStatsProjection {
    UUID getTourId();
    Long getBookingCount();
    BigDecimal getRevenue();
}
