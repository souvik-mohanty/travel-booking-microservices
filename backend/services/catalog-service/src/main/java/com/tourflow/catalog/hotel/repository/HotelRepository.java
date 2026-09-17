package com.tourflow.catalog.hotel.repository;

import com.tourflow.catalog.hotel.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface HotelRepository extends JpaRepository<Hotel, UUID> {

    List<Hotel> findByOwnerId(UUID ownerId);
}
