package com.tourflow.mobility.trip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

// A single event in a trip's logbook -- check-in/check-out at a stay,
// an activity completed, arrival at the destination, or a free-form note.
// Append-only: no update/delete operation exists, same as audit-service's
// AuditLog -- a logbook that could be edited after the fact wouldn't be
// trustworthy as a record of what actually happened.
@Entity
@Table(name = "trip_log_entries")
public class TripLogEntry {

    @Id
    private UUID id;

    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TripLogEntryType type;

    @Column(length = 1000)
    private String description;

    @Column(length = 255)
    private String location;

    // When the event actually happened -- defaults to now() at write time if
    // the caller doesn't supply one (e.g. logging a check-in as it happens),
    // but can be backdated (e.g. logging yesterday's checkout after the fact).
    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt;

    // identity-service user who wrote this entry -- the tour's business
    // owner, per TripLogService's access check.
    @Column(name = "logged_by", nullable = false)
    private UUID loggedBy;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected TripLogEntry() {
    }

    public TripLogEntry(
            UUID id,
            UUID tripId,
            TripLogEntryType type,
            String description,
            String location,
            OffsetDateTime occurredAt,
            UUID loggedBy,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.tripId = tripId;
        this.type = type;
        this.description = description;
        this.location = location;
        this.occurredAt = occurredAt;
        this.loggedBy = loggedBy;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTripId() {
        return tripId;
    }

    public TripLogEntryType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public UUID getLoggedBy() {
        return loggedBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
