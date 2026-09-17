
package com.tourflow.booking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

// JPA entity representing a booking stored in the bookings table.
@Entity
@Table(name = "bookings")
public class Booking {

    // Unique identifier of the booking.
    @Id
    private UUID id;

    // ID of the tour being booked.
    @Column(name = "tour_id", nullable = false)
    private UUID tourId;

    // ID of the user who created the booking.
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    // Number of participants included in this booking.
    @Column(name = "number_of_participants", nullable = false)
    private Integer numberOfParticipants;

    // Total amount that the user must pay for this booking.
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // Current lifecycle state of the booking.
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    // Time when the booking was created.
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    // Time when the booking was last updated.
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    // Required by JPA.
    protected Booking() {
    }

    // Create a new booking with its initial values.
    public Booking(
            UUID id,
            UUID tourId,
            UUID userId,
            Integer numberOfParticipants,
            BigDecimal totalPrice,
            BookingStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.tourId = tourId;
        this.userId = userId;
        this.numberOfParticipants = numberOfParticipants;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTourId() {
        return tourId;
    }

    public UUID getUserId() {
        return userId;
    }

    public Integer getNumberOfParticipants() {
        return numberOfParticipants;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
