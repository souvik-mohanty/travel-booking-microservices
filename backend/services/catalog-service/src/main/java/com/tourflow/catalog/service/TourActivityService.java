package com.tourflow.catalog.service;

import com.tourflow.catalog.business.dto.ActivityResponse;
import com.tourflow.catalog.business.exception.ActivityNotFoundException;
import com.tourflow.catalog.business.service.ActivityService;
import com.tourflow.catalog.domain.Tour;
import com.tourflow.catalog.domain.TourActivity;
import com.tourflow.catalog.dto.AddTourActivityRequest;
import com.tourflow.catalog.dto.TourActivityResponse;
import com.tourflow.catalog.repository.TourActivityRepository;
import com.tourflow.catalog.repository.TourRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Manages which catalog Activities (adventure/sightseeing/transport, owned by
// a business -- see the business sub-package) are attached to a tour.
@Service
public class TourActivityService {

    private final TourActivityRepository tourActivityRepository;
    private final TourRepository tourRepository;
    private final ActivityService activityService;

    public TourActivityService(
            TourActivityRepository tourActivityRepository,
            TourRepository tourRepository,
            ActivityService activityService
    ) {
        this.tourActivityRepository = tourActivityRepository;
        this.tourRepository = tourRepository;
        this.activityService = activityService;
    }

    // Only the tour's creator may attach an activity. Verifies the activity
    // actually exists and is customer-visible (ACTIVE) before attaching it.
    // This used to be an HTTP call to business-service; now that both domains
    // live in the same merged catalog-service, it's a direct in-process call
    // -- same validation, same IllegalArgumentException/message contract as
    // before, just no network hop.
    @Transactional
    public TourActivityResponse addActivity(UUID tourId, AddTourActivityRequest request, UUID callerId) {

        Tour tour = requireOwnedTour(tourId, callerId);

        if (request.scheduledDate().isBefore(tour.getStartDate()) || request.scheduledDate().isAfter(tour.getEndDate())) {
            throw new IllegalArgumentException(
                    "scheduledDate must fall within the tour's dates ("
                            + tour.getStartDate() + " to " + tour.getEndDate() + ")"
            );
        }

        if (tourActivityRepository.findByTourIdAndActivityId(tourId, request.activityId()).isPresent()) {
            throw new IllegalArgumentException("This activity is already attached to the tour");
        }

        verifyActivityIsActive(request.activityId());

        TourActivity tourActivity = new TourActivity(
                UUID.randomUUID(),
                tourId,
                request.activityId(),
                request.scheduledDate(),
                request.scheduledTime(),
                OffsetDateTime.now()
        );

        TourActivity saved = tourActivityRepository.save(tourActivity);

        return TourActivityResponse.from(saved);
    }

    // Only the tour's creator may detach an activity.
    @Transactional
    public void removeActivity(UUID tourId, UUID activityId, UUID callerId) {

        requireOwnedTour(tourId, callerId);

        TourActivity tourActivity = tourActivityRepository.findByTourIdAndActivityId(tourId, activityId)
                .orElseThrow(() -> new TourActivityNotFoundException("Activity not attached to this tour"));

        tourActivityRepository.delete(tourActivity);
    }

    @Transactional(readOnly = true)
    public List<TourActivityResponse> getActivitiesForTour(UUID tourId) {

        if (!tourRepository.existsById(tourId)) {
            throw new TourNotFoundException("Tour not found: " + tourId);
        }

        return tourActivityRepository.findByTourId(tourId)
                .stream()
                .map(TourActivityResponse::from)
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

    // Preserves BusinessClient's exact prior contract: IllegalArgumentException
    // (-> 400 from GlobalExceptionHandler) whether the activity doesn't exist
    // or exists but isn't ACTIVE yet.
    private void verifyActivityIsActive(UUID activityId) {

        ActivityResponse activity;

        try {
            activity = activityService.getActivity(activityId);
        } catch (ActivityNotFoundException ex) {
            throw new IllegalArgumentException("Activity not found: " + activityId);
        }

        if (!"ACTIVE".equals(activity.status())) {
            throw new IllegalArgumentException(
                    "Activity " + activityId + " is not active (status: " + activity.status() + ")"
            );
        }
    }
}
