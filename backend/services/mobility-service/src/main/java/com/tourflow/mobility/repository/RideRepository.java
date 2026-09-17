package com.tourflow.mobility.repository;

import com.tourflow.mobility.domain.Ride;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RideRepository extends JpaRepository<Ride, UUID> {

    List<Ride> findByCreatedBy(UUID createdBy);

    // Row-locked read for the seat-booking critical section. There's no Redis
    // distributed lock in this project yet, so this is the actual guard
    // against two concurrent bookings both reading stale availableSeats and
    // overselling -- matches the doc's own guidance that DB transactions
    // should remain the primary consistency mechanism where Redis isn't set up.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Ride r where r.id = :id")
    Optional<Ride> findByIdForUpdate(UUID id);
}
