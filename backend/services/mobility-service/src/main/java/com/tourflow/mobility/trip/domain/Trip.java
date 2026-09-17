package com.tourflow.mobility.trip.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "trips",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_trips_booking", columnNames = "booking_id")
        }
)
public class Trip {

    @Id
    private UUID id;

    // The booking (booking-service) this trip executes. One trip per booking.
    @Column(name = "booking_id", nullable = false)
    private UUID bookingId;

    // identity-service user who created/manages this trip.
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    // driver-service driver, assigned later.
    @Column(name = "driver_id")
    private UUID driverId;

    // fleet-service vehicle, assigned later.
    @Column(name = "vehicle_id")
    private UUID vehicleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TripStatus status;

    @Column(name = "started_at")
    private OffsetDateTime startedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Trip() {
    }

    public Trip(
            UUID id,
            UUID bookingId,
            UUID createdBy,
            UUID driverId,
            UUID vehicleId,
            TripStatus status,
            OffsetDateTime startedAt,
            OffsetDateTime completedAt,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.bookingId = bookingId;
        this.createdBy = createdBy;
        this.driverId = driverId;
        this.vehicleId = vehicleId;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public UUID getVehicleId() {
        return vehicleId;
    }

    public TripStatus getStatus() {
        return status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setDriverId(UUID driverId) {
        this.driverId = driverId;
    }

    public void setVehicleId(UUID vehicleId) {
        this.vehicleId = vehicleId;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }

    public void setStartedAt(OffsetDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
