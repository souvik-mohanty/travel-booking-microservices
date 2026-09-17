package com.tourflow.catalog.hotel.service;

import com.tourflow.catalog.hotel.domain.Hotel;
import com.tourflow.catalog.hotel.domain.HotelStatus;
import com.tourflow.catalog.hotel.dto.CreateHotelRequest;
import com.tourflow.catalog.hotel.dto.HotelResponse;
import com.tourflow.catalog.hotel.dto.UpdateHotelRequest;
import com.tourflow.catalog.hotel.exception.HotelNotFoundException;
import com.tourflow.catalog.hotel.exception.UnauthorizedHotelAccessException;
import com.tourflow.catalog.hotel.repository.HotelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @Transactional
    public HotelResponse createHotel(
            CreateHotelRequest request,
            UUID ownerId
    ) {

        OffsetDateTime now = OffsetDateTime.now();

        Hotel hotel = new Hotel(
                UUID.randomUUID(),
                ownerId,
                request.name(),
                request.description(),
                request.address(),
                request.city(),
                request.state(),
                request.country(),

                // No approval workflow yet -- hotels are usable immediately.
                HotelStatus.ACTIVE,

                now,
                now
        );

        Hotel saved = hotelRepository.save(hotel);

        return HotelResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public HotelResponse getHotel(UUID id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));

        return HotelResponse.fromEntity(hotel);
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> getHotels() {

        return hotelRepository.findAll()
                .stream()
                .map(HotelResponse::fromEntity)
                .toList();
    }

    // Update a hotel's details. Only the owner may do this.
    @Transactional
    public HotelResponse updateHotel(
            UUID id,
            UUID ownerId,
            UpdateHotelRequest request
    ) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));

        if (!hotel.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedHotelAccessException(
                    "You are not authorized to manage this hotel"
            );
        }

        hotel.update(
                request.name(),
                request.description(),
                request.address(),
                request.city(),
                request.state(),
                request.country()
        );

        return HotelResponse.fromEntity(hotelRepository.save(hotel));
    }

    // Admin moderation: suspend a hotel, e.g. for a policy violation.
    // Idempotent -- suspending an already-suspended hotel is a no-op that
    // returns 200 unchanged, matching the status-transition convention in
    // docs/api/API-STANDARDS.md. Ownership doesn't matter here -- only the
    // ADMIN role gate on this endpoint (see SecurityConfig) does.
    @Transactional
    public HotelResponse suspendHotel(UUID id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));

        if (hotel.getStatus() == HotelStatus.SUSPENDED) {
            return HotelResponse.fromEntity(hotel);
        }

        hotel.setStatus(HotelStatus.SUSPENDED);
        hotel.setUpdatedAt(OffsetDateTime.now());

        return HotelResponse.fromEntity(hotelRepository.save(hotel));
    }

    // Admin moderation: lift a suspension.
    @Transactional
    public HotelResponse reinstateHotel(UUID id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException("Hotel not found"));

        if (hotel.getStatus() == HotelStatus.ACTIVE) {
            return HotelResponse.fromEntity(hotel);
        }

        hotel.setStatus(HotelStatus.ACTIVE);
        hotel.setUpdatedAt(OffsetDateTime.now());

        return HotelResponse.fromEntity(hotelRepository.save(hotel));
    }
}
