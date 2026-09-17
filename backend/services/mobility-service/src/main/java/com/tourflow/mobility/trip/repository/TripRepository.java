package com.tourflow.mobility.trip.repository;

import com.tourflow.mobility.trip.domain.Trip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {

    List<Trip> findByCreatedBy(UUID createdBy);

    Optional<Trip> findByBookingId(UUID bookingId);
}
