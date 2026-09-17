package com.tourflow.catalog.repository;

import com.tourflow.catalog.domain.TourActivity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TourActivityRepository extends JpaRepository<TourActivity, UUID> {

    List<TourActivity> findByTourId(UUID tourId);

    Optional<TourActivity> findByTourIdAndActivityId(UUID tourId, UUID activityId);
}
