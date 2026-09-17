package com.tourflow.catalog.repository;

import com.tourflow.catalog.domain.Tour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Repository responsible for database operations on Tour entities.
public interface TourRepository extends JpaRepository<Tour, UUID> {

    // Find tours by their current status.
    List<Tour> findByStatus(String status);

    // Find tours created by a specific user.
    List<Tour> findByCreatedBy(UUID createdBy);

    // Find tours for a specific destination.
    List<Tour> findByDestinationIgnoreCase(String destination);
}