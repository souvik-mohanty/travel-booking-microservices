package com.tourflow.analytics.repository;

import com.tourflow.analytics.domain.BookingEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface BookingEventLogRepository extends JpaRepository<BookingEventLog, UUID> {

    // Used by BookingEventListener to dedup a redelivered BookingConfirmed
    // before it would otherwise double-count a booking.
    boolean existsByEventId(UUID eventId);

    @Query("SELECT COALESCE(SUM(b.totalPrice), 0) FROM BookingEventLog b")
    BigDecimal sumTotalPrice();

    // Native query: CAST(... AS date) grouping isn't portable across JPQL
    // dialects, but this service only ever runs against Postgres (same as
    // every other service in this repo -- see docs/database/DATABASE-DESIGN.md).
    @Query(value = """
            SELECT CAST(occurred_at AS date) AS bookingDate,
                   COUNT(*) AS bookingCount,
                   COALESCE(SUM(total_price), 0) AS revenue
            FROM booking_event_log
            WHERE occurred_at >= :from AND occurred_at < :to
            GROUP BY CAST(occurred_at AS date)
            ORDER BY CAST(occurred_at AS date)
            """, nativeQuery = true)
    List<DailyBookingStatsProjection> findDailyStats(
            @Param("from") OffsetDateTime from,
            @Param("to") OffsetDateTime to
    );
}
