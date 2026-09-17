package com.tourflow.catalog.hotel.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

// Reserves some number of a Room's totalRooms for a date range. Availability
// for any given date range is computed at read time by summing overlapping
// ACTIVE reservations against Room.totalRooms (see RoomReservationRepository)
// rather than maintaining a separately-decremented counter -- same "compute
// from source rows" choice analytics-service made for its aggregates, and it
// sidesteps having to also un-decrement a counter correctly on cancellation.
//
// tourId/tourLegId are cross-service references (tour-service) with no FK --
// every cross-service link in this codebase is a plain UUID, validated by a
// REST call, never a database foreign key (see docs/database/DATABASE-DESIGN.md).
@Entity
@Table(name = "room_reservations")
public class RoomReservation {

    @Id
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "tour_id", nullable = false)
    private UUID tourId;

    // Which leg of the tour this reservation belongs to, if the tour has legs.
    @Column(name = "tour_leg_id")
    private UUID tourLegId;

    // identity-service user who made this reservation -- the tour's creator,
    // forwarded from tour-service's own caller (see HotelClient there). Only
    // this user may cancel it.
    @Column(name = "reserved_by", nullable = false)
    private UUID reservedBy;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    // Exclusive, hotel-industry checkout convention: a stay from
    // 2026-11-10 to 2026-11-14 occupies the room for 4 nights, and a
    // reservation starting 2026-11-14 does not overlap it.
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "rooms_reserved", nullable = false)
    private Integer roomsReserved;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected RoomReservation() {
    }

    public RoomReservation(
            UUID id,
            UUID roomId,
            UUID tourId,
            UUID tourLegId,
            UUID reservedBy,
            LocalDate startDate,
            LocalDate endDate,
            Integer roomsReserved,
            ReservationStatus status,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.roomId = roomId;
        this.tourId = tourId;
        this.tourLegId = tourLegId;
        this.reservedBy = reservedBy;
        this.startDate = startDate;
        this.endDate = endDate;
        this.roomsReserved = roomsReserved;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public UUID getTourId() {
        return tourId;
    }

    public UUID getTourLegId() {
        return tourLegId;
    }

    public UUID getReservedBy() {
        return reservedBy;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public Integer getRoomsReserved() {
        return roomsReserved;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
