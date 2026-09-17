package com.tourflow.catalog.hotel.repository;

import com.tourflow.catalog.hotel.domain.Room;
import com.tourflow.catalog.hotel.domain.RoomStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {

    List<Room> findByHotelId(UUID hotelId);

    List<Room> findByStatus(RoomStatus status);

    // Row-locked read for the reservation critical section, same pattern as
    // ride-service's findByIdForUpdate: there's no Redis distributed lock in
    // this project yet, so locking this row is the actual guard against two
    // concurrent reservations both reading the same "rooms already reserved
    // for these dates" sum and jointly overselling the room type.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Room r where r.id = :id")
    Optional<Room> findByIdForUpdate(UUID id);
}
