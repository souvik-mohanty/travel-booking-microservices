package com.tourflow.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.UUID;

// Links a tour to a business-service Activity (adventure, sightseeing, or a
// vehicle-based transport offering -- see business-service's
// ActivityCategory). activityId is a cross-service reference with no FK,
// validated via BusinessClient before this row is written.
//
// scheduledDate/scheduledTime say when during the trip the activity happens
// (e.g. river rafting on the tour's second day at 9am) -- kept as separate
// date/time columns rather than one LocalDateTime so a bare date-only
// activity isn't forced to invent a time, and to match how the itinerary's
// TourLeg dates are also plain LocalDate with no time component.
@Entity
@Table(name = "tour_activities")
public class TourActivity {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "tour_id", nullable = false)
    private UUID tourId;

    @Column(name = "activity_id", nullable = false)
    private UUID activityId;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "scheduled_time")
    private LocalTime scheduledTime;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected TourActivity() {
    }

    public TourActivity(
            UUID id,
            UUID tourId,
            UUID activityId,
            LocalDate scheduledDate,
            LocalTime scheduledTime,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.tourId = tourId;
        this.activityId = activityId;
        this.scheduledDate = scheduledDate;
        this.scheduledTime = scheduledTime;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTourId() {
        return tourId;
    }

    public UUID getActivityId() {
        return activityId;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
