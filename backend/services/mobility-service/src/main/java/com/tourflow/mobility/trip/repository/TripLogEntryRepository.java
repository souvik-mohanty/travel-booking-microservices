package com.tourflow.mobility.trip.repository;

import com.tourflow.mobility.trip.domain.TripLogEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TripLogEntryRepository extends JpaRepository<TripLogEntry, UUID> {

    List<TripLogEntry> findByTripIdOrderByOccurredAtAsc(UUID tripId);
}
