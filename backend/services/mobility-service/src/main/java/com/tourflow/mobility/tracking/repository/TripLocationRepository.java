package com.tourflow.tracking.repository;

import com.tourflow.tracking.domain.TripLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripLocationRepository extends JpaRepository<TripLocation, UUID> {

    List<TripLocation> findByTripIdOrderByRecordedAtAsc(UUID tripId);

    Optional<TripLocation> findFirstByTripIdOrderByRecordedAtDesc(UUID tripId);
}
