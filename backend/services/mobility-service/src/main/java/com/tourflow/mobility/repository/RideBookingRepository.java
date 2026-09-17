package com.tourflow.mobility.repository;

import com.tourflow.mobility.domain.RideBooking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RideBookingRepository extends JpaRepository<RideBooking, UUID> {

    List<RideBooking> findByUserId(UUID userId);

    List<RideBooking> findByRideId(UUID rideId);
}
