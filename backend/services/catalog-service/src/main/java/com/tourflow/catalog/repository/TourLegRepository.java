package com.tourflow.catalog.repository;

import com.tourflow.catalog.domain.TourLeg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TourLegRepository extends JpaRepository<TourLeg, UUID> {

    List<TourLeg> findByTourIdOrderBySequenceOrderAsc(UUID tourId);

    int countByTourId(UUID tourId);
}
