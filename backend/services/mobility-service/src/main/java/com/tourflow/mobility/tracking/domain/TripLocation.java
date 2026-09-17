package com.tourflow.mobility.tracking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// A single recorded location point for a trip. Append-only -- a trip's
// route is the full history of these, not a row that gets overwritten.
//
// Scoped-down v1: REST polling instead of the doc's WebSocket+Redis
// real-time design (§13 Tracking Service). Storing points in Postgres and
// letting clients poll "latest"/"history" is a genuine working tracking
// model; it's the live-push delivery mechanism that's deferred, not the
// domain itself.
@Entity
@Table(name = "trip_locations")
public class TripLocation {

    @Id
    private UUID id;

    // trip-service Trip this location belongs to.
    @Column(name = "trip_id", nullable = false)
    private UUID tripId;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    protected TripLocation() {
    }

    public TripLocation(
            UUID id,
            UUID tripId,
            BigDecimal latitude,
            BigDecimal longitude,
            OffsetDateTime recordedAt
    ) {
        this.id = id;
        this.tripId = tripId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.recordedAt = recordedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTripId() {
        return tripId;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }
}
