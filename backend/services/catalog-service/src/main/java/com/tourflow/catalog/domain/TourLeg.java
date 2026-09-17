package com.tourflow.catalog.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

// One stay within a tour's itinerary -- a multi-city tour has several of
// these, each pointing at a specific hotel-service Room for a date range.
// hotelId/roomId are cross-service references (hotel-service) with no FK --
// every cross-service link in this codebase is a plain UUID, validated by a
// REST call, never a database foreign key.
@Entity
@Table(name = "tour_legs")
public class TourLeg {

    @Id
    @Column(nullable = false)
    private UUID id;

    @Column(name = "tour_id", nullable = false)
    private UUID tourId;

    // 1-based order this leg occurs in within the tour's itinerary.
    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    // Optional override -- falls back to the parent Tour's destination in
    // the response if not set (see TourLegResponse).
    @Column(length = 255)
    private String destination;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "hotel_id", nullable = false)
    private UUID hotelId;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "rooms_booked", nullable = false)
    private Integer roomsBooked;

    // hotel-service's RoomReservation ID -- needed to release the inventory
    // via HotelClient.cancelReservation when this leg is removed.
    @Column(name = "hotel_reservation_id", nullable = false)
    private UUID hotelReservationId;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected TourLeg() {
    }

    public TourLeg(
            UUID id,
            UUID tourId,
            Integer sequenceOrder,
            String destination,
            LocalDate startDate,
            LocalDate endDate,
            UUID hotelId,
            UUID roomId,
            Integer roomsBooked,
            UUID hotelReservationId,
            OffsetDateTime createdAt
    ) {
        this.id = id;
        this.tourId = tourId;
        this.sequenceOrder = sequenceOrder;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hotelId = hotelId;
        this.roomId = roomId;
        this.roomsBooked = roomsBooked;
        this.hotelReservationId = hotelReservationId;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTourId() {
        return tourId;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public String getDestination() {
        return destination;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public UUID getHotelId() {
        return hotelId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public Integer getRoomsBooked() {
        return roomsBooked;
    }

    public UUID getHotelReservationId() {
        return hotelReservationId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
