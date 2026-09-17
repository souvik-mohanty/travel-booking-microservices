package com.tourflow.hotel.repository;

import com.tourflow.hotel.domain.Room;
import com.tourflow.hotel.domain.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByHotelId(UUID hotelId);

    List<Room> findByStatus(RoomStatus status);
}
