package com.tourflow.catalog.service;

import com.tourflow.catalog.domain.Tour;
import com.tourflow.catalog.dto.CreateTourRequest;
import com.tourflow.catalog.dto.TourResponse;
import com.tourflow.catalog.event.TourEventPublisher;
import com.tourflow.catalog.repository.TourRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

// Contains the business logic for managing tours.
@Service
public class TourService {

    private final TourRepository tourRepository;
    private final TourEventPublisher tourEventPublisher;

    public TourService(TourRepository tourRepository, TourEventPublisher tourEventPublisher) {
        this.tourRepository = tourRepository;
        this.tourEventPublisher = tourEventPublisher;
    }

    // Create a new tour for the authenticated user.
    @Transactional
    public TourResponse createTour(CreateTourRequest request, UUID userId) {

        // The end date cannot be before the start date.
        if (request.endDate().isBefore(request.startDate())) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }

        // Create the domain entity.
        Tour tour = new Tour();

        tour.setTitle(request.title().trim());
        tour.setDescription(
                request.description() == null
                        ? null
                        : request.description().trim()
        );
        tour.setDestination(request.destination().trim());
        tour.setStartDate(request.startDate());
        tour.setEndDate(request.endDate());
        tour.setPrice(request.price());
        tour.setMaxParticipants(request.maxParticipants());

        // Always start newly created tours as DRAFT.
        tour.setStatus("DRAFT");

        // IMPORTANT:
        // The creator comes from the authenticated JWT.
        // The client never supplies this value.
        tour.setCreatedBy(userId);

        // Save the tour to PostgreSQL.
        Tour savedTour = tourRepository.save(tour);

        return TourResponse.from(savedTour);
    }

    // Get a single tour by its ID.
    @Transactional(readOnly = true)
    public TourResponse getTour(UUID id) {
        return TourResponse.from(findOrThrow(id));
    }

    // Publish a DRAFT tour, making it visible to search-service. Idempotent:
    // publishing an already-PUBLISHED tour is a no-op that returns 200
    // unchanged, matching the status-transition convention in
    // docs/api/API-STANDARDS.md. Only the tour's creator may publish it --
    // they already know the tour exists (they created it), so a mismatch is
    // a 403, not a 404.
    @Transactional
    public TourResponse publishTour(UUID id, UUID userId) {
        Tour tour = findOrThrow(id);
        requireOwner(tour, userId);

        if ("PUBLISHED".equals(tour.getStatus())) {
            return TourResponse.from(tour);
        }
        if (!"DRAFT".equals(tour.getStatus())) {
            throw new IllegalArgumentException(
                    "Cannot publish a tour from status " + tour.getStatus()
            );
        }

        tour.setStatus("PUBLISHED");
        Tour saved = tourRepository.save(tour);
        tourEventPublisher.publishTourPublished(saved);

        return TourResponse.from(saved);
    }

    // Cancel a tour, removing it from search-service's index. Idempotent for
    // the same reason as publishTour above. Unlike publish, cancel is valid
    // from either DRAFT or PUBLISHED.
    @Transactional
    public TourResponse cancelTour(UUID id, UUID userId) {
        Tour tour = findOrThrow(id);
        requireOwner(tour, userId);

        if ("CANCELLED".equals(tour.getStatus())) {
            return TourResponse.from(tour);
        }

        tour.setStatus("CANCELLED");
        Tour saved = tourRepository.save(tour);
        tourEventPublisher.publishTourCancelled(saved);

        return TourResponse.from(saved);
    }

    private Tour findOrThrow(UUID id) {
        return tourRepository.findById(id)
                .orElseThrow(() ->
                        new TourNotFoundException("Tour not found: " + id)
                );
    }

    private void requireOwner(Tour tour, UUID userId) {
        if (!tour.getCreatedBy().equals(userId)) {
            throw new TourOwnershipException("You do not own this tour");
        }
    }

    // Return all tours.
    @Transactional(readOnly = true)
    public List<TourResponse> getAllTours() {
        return tourRepository.findAll()
                .stream()
                .map(TourResponse::from)
                .toList();
    }

    // Find tours by destination.
    @Transactional(readOnly = true)
    public List<TourResponse> getToursByDestination(String destination) {
        return tourRepository.findByDestinationIgnoreCase(destination.trim())
                .stream()
                .map(TourResponse::from)
                .toList();
    }

    // Find tours by status.
    @Transactional(readOnly = true)
    public List<TourResponse> getToursByStatus(String status) {
        return tourRepository.findByStatus(status.toUpperCase())
                .stream()
                .map(TourResponse::from)
                .toList();
    }

    // Find all tours created by a specific user.
    @Transactional(readOnly = true)
    public List<TourResponse> getToursByCreator(UUID userId) {
        return tourRepository.findByCreatedBy(userId)
                .stream()
                .map(TourResponse::from)
                .toList();
    }
}
