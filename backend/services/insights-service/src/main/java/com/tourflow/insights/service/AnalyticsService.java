package com.tourflow.analytics.service;

import com.tourflow.analytics.domain.BookingEventLog;
import com.tourflow.analytics.dto.BookingSummaryResponse;
import com.tourflow.analytics.dto.DailyBookingStatsResponse;
import com.tourflow.analytics.repository.BookingEventLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
public class AnalyticsService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    private final BookingEventLogRepository bookingEventLogRepository;

    public AnalyticsService(BookingEventLogRepository bookingEventLogRepository) {
        this.bookingEventLogRepository = bookingEventLogRepository;
    }

    // Idempotent by eventId: a redelivered BookingConfirmed (consumer
    // restart with no committed offset, at-least-once delivery) is a no-op
    // rather than a duplicate row that would inflate every aggregate below.
    @Transactional
    public void recordBookingConfirmed(
            UUID eventId,
            UUID bookingId,
            UUID tourId,
            UUID userId,
            BigDecimal totalPrice,
            String currency,
            OffsetDateTime occurredAt
    ) {
        if (bookingEventLogRepository.existsByEventId(eventId)) {
            log.info("Skipping already-processed BookingConfirmed event {}", eventId);
            return;
        }

        BookingEventLog entry = new BookingEventLog();
        entry.setEventId(eventId);
        entry.setBookingId(bookingId);
        entry.setTourId(tourId);
        entry.setUserId(userId);
        entry.setTotalPrice(totalPrice);
        entry.setCurrency(currency);
        entry.setOccurredAt(occurredAt);

        bookingEventLogRepository.save(entry);
    }

    // Aggregated at read time from the raw event log rather than maintained
    // as a separately updated running total -- for this data volume, a plain
    // SUM/COUNT is simpler and can't drift out of sync with the source rows.
    @Transactional(readOnly = true)
    public BookingSummaryResponse getBookingSummary() {
        long totalBookings = bookingEventLogRepository.count();
        BigDecimal totalRevenue = bookingEventLogRepository.sumTotalPrice();

        // "INR" is the only currency any event in this codebase carries
        // today -- see docs/events/EVENT-SCHEMAS.md's BookingConfirmed note.
        return new BookingSummaryResponse(totalBookings, totalRevenue, "INR");
    }

    @Transactional(readOnly = true)
    public List<DailyBookingStatsResponse> getDailyBookingStats(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' cannot be after 'to'");
        }

        OffsetDateTime fromInclusive = from.atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime toExclusive = to.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC);

        return bookingEventLogRepository.findDailyStats(fromInclusive, toExclusive)
                .stream()
                .map(DailyBookingStatsResponse::from)
                .toList();
    }
}
