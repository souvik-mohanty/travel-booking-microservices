package com.tourflow.catalog.hotel.repository;

import com.tourflow.catalog.hotel.domain.RoomReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RoomReservationRepository extends JpaRepository<RoomReservation, UUID> {

    List<RoomReservation> findByRoomId(UUID roomId);

    List<RoomReservation> findByTourId(UUID tourId);

    // Half-open interval overlap test (start inclusive, end exclusive) --
    // matches the hotel-industry checkout convention documented on
    // RoomReservation.endDate. Only ACTIVE reservations count against
    // capacity; a CANCELLED one already gave its rooms back.
    @Query("""
            SELECT COALESCE(SUM(rr.roomsReserved), 0)
            FROM RoomReservation rr
            WHERE rr.roomId = :roomId
              AND rr.status = com.tourflow.catalog.hotel.domain.ReservationStatus.ACTIVE
              AND rr.startDate < :endDate
              AND rr.endDate > :startDate
            """)
    int sumReservedRooms(
            @Param("roomId") UUID roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
