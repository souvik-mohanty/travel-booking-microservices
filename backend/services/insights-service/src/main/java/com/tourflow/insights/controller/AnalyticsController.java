package com.tourflow.insights.controller;

import com.tourflow.insights.dto.BookingSummaryResponse;
import com.tourflow.insights.dto.DailyBookingStatsResponse;
import com.tourflow.insights.dto.TourBookingStatsResponse;
import com.tourflow.insights.service.AnalyticsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics/bookings")
public class AnalyticsController {

    private static final int DEFAULT_RANGE_DAYS = 29;

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ResponseEntity<BookingSummaryResponse> getBookingSummary() {
        return ResponseEntity.ok(analyticsService.getBookingSummary());
    }

    // Defaults to the trailing 30 days when from/to are omitted.
    @GetMapping("/daily")
    public ResponseEntity<List<DailyBookingStatsResponse>> getDailyBookingStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate effectiveTo = to != null ? to : LocalDate.now();
        LocalDate effectiveFrom = from != null ? from : effectiveTo.minusDays(DEFAULT_RANGE_DAYS);

        return ResponseEntity.ok(analyticsService.getDailyBookingStats(effectiveFrom, effectiveTo));
    }

    @GetMapping("/by-tour")
    public ResponseEntity<List<TourBookingStatsResponse>> getStatsByTour() {
        return ResponseEntity.ok(analyticsService.getStatsByTour());
    }
}
