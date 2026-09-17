package com.tourflow.ride.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "ride_bookings")
public class RideBooking {

    @Id
    private UUID id;

    @Column(name = "ride_id", nullable = false)
    private UUID rideId;

    // identity-service user who booked seats.
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "seats_booked", nullable = false)
    private Integer seatsBooked;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideBookingStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected RideBooking() {
    }

    public RideBooking(
            UUID id,
            UUID rideId,
            UUID userId,
            Integer seatsBooked,
            RideBookingStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.rideId = rideId;
        this.userId = userId;
        this.seatsBooked = seatsBooked;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRideId() {
        return rideId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Integer getSeatsBooked() {
        return seatsBooked;
    }

    public RideBookingStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(RideBookingStatus status) {
        this.status = status;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
