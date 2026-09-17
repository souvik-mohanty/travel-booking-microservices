package com.tourflow.catalog.service;

import com.tourflow.catalog.domain.Tour;
import com.tourflow.catalog.domain.TourLeg;
import com.tourflow.catalog.dto.CreateTourLegRequest;
import com.tourflow.catalog.dto.TourLegResponse;
import com.tourflow.catalog.hotel.dto.CreateRoomReservationRequest;
import com.tourflow.catalog.hotel.exception.RoomNotAvailableException;
import com.tourflow.catalog.hotel.exception.RoomNotFoundException;
import com.tourflow.catalog.hotel.exception.RoomReservationNotFoundException;
import com.tourflow.catalog.hotel.service.RoomReservationService;
import com.tourflow.catalog.repository.TourLegRepository;
import com.tourflow.catalog.repository.TourRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Manages a tour's multi-leg itinerary -- each leg reserves a specific Room
// (see the hotel sub-package) for a date range.
@Service
public class TourLegService {

    private final TourLegRepository tourLegRepository;
    private final TourRepository tourRepository;
    private final RoomReservationService roomReservationService;

    public TourLegService(
            TourLegRepository tourLegRepository,
            TourRepository tourRepository,
            RoomReservationService roomReservationService
    ) {
        this.tourLegRepository = tourLegRepository;
        this.tourRepository = tourRepository;
        this.roomReservationService = roomReservationService;
    }

    // Only the tour's creator may add a leg. The room is reserved first -- if
    // that's rejected (not enough rooms free for these dates), nothing is
    // written here. The leg's ID is generated up front so the reservation can
    // reference it as tourLegId from the start. This used to be an HTTP call
    // to hotel-service (forwarding the caller's JWT so hotel-service could
    // attribute the reservation to them); now that both domains live in the
    // same merged catalog-service, it's a direct in-process call passing
    // callerId straight through, and room-not-found/room-not-available
    // rejections are translated to the same IllegalArgumentException contract
    // HotelClient used to produce.
    @Transactional
    public TourLegResponse addLeg(UUID tourId, CreateTourLegRequest request, UUID callerId) {

        Tour tour = requireOwnedTour(tourId, callerId);

        if (!request.startDate().isBefore(request.endDate())) {
            throw new IllegalArgumentException("startDate must be before endDate");
        }

        UUID legId = UUID.randomUUID();
        int nextOrder = tourLegRepository.countByTourId(tourId) + 1;

        UUID reservationId = reserveRoom(
                request.roomId(),
                tourId,
                legId,
                request,
                callerId
        );

        TourLeg leg = new TourLeg(
                legId,
                tourId,
                nextOrder,
                request.destination(),
                request.startDate(),
                request.endDate(),
                request.hotelId(),
                request.roomId(),
                request.roomsBooked(),
                reservationId,
                OffsetDateTime.now()
        );

        TourLeg saved = tourLegRepository.save(leg);

        return TourLegResponse.from(saved, tour.getDestination());
    }

    // Only the tour's creator may remove a leg. Releases the room reservation
    // before deleting the local row.
    @Transactional
    public void removeLeg(UUID tourId, UUID legId, UUID callerId) {

        requireOwnedTour(tourId, callerId);

        TourLeg leg = tourLegRepository.findById(legId)
                .filter(candidate -> candidate.getTourId().equals(tourId))
                .orElseThrow(() -> new TourLegNotFoundException("Tour leg not found: " + legId));

        cancelReservation(leg.getHotelReservationId(), callerId);

        tourLegRepository.delete(leg);
    }

    @Transactional(readOnly = true)
    public List<TourLegResponse> getLegsForTour(UUID tourId) {

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new TourNotFoundException("Tour not found: " + tourId));

        return tourLegRepository.findByTourIdOrderBySequenceOrderAsc(tourId)
                .stream()
                .map(leg -> TourLegResponse.from(leg, tour.getDestination()))
                .toList();
    }

    private Tour requireOwnedTour(UUID tourId, UUID callerId) {

        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new TourNotFoundException("Tour not found: " + tourId));

        if (!tour.getCreatedBy().equals(callerId)) {
            throw new TourOwnershipException("You do not own this tour");
        }

        return tour;
    }

    // Preserves HotelClient's exact prior contract: room-not-found/
    // room-not-available both surface as IllegalArgumentException (-> 400
    // from GlobalExceptionHandler), same as when this went over HTTP.
    private UUID reserveRoom(
            UUID roomId,
            UUID tourId,
            UUID tourLegId,
            CreateTourLegRequest request,
            UUID callerId
    ) {
        try {
            return roomReservationService.reserve(
                    roomId,
                    new CreateRoomReservationRequest(
                            tourId,
                            tourLegId,
                            request.startDate(),
                            request.endDate(),
                            request.roomsBooked()
                    ),
                    callerId
            ).id();
        } catch (RoomNotFoundException | RoomNotAvailableException ex) {
            throw new IllegalArgumentException(ex.getMessage());
        }
    }

    // Best-effort in the sense that a reservation already gone is not an
    // error -- there's nothing left to release. Matches HotelClient's
    // treatment of a 404 on cancel.
    private void cancelReservation(UUID reservationId, UUID callerId) {
        try {
            roomReservationService.cancel(reservationId, callerId);
        } catch (RoomReservationNotFoundException ex) {
            // Already released or never existed -- nothing to do.
        }
    }
}
