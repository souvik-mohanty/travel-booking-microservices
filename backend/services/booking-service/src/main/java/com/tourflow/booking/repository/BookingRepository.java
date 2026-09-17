package com.tourflow.booking.repository;

import com.tourflow.booking.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

// Repository responsible for database operations on bookings.
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // Find all bookings created by a particular user.
    List<Booking> findByUserId(UUID userId);

    // Find all bookings belonging to a particular tour.
    List<Booking> findByTourId(UUID tourId);
}
