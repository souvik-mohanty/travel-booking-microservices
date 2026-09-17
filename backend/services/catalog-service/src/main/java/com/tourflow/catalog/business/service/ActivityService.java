package com.tourflow.catalog.business.service;

import com.tourflow.catalog.business.domain.Activity;
import com.tourflow.catalog.business.domain.ActivityStatus;
import com.tourflow.catalog.business.domain.Business;
import com.tourflow.catalog.business.dto.ActivityResponse;
import com.tourflow.catalog.business.dto.CreateActivityRequest;
import com.tourflow.catalog.business.dto.UpdateActivityRequest;
import com.tourflow.catalog.business.exception.ActivityNotFoundException;
import com.tourflow.catalog.business.exception.BusinessNotFoundException;
import com.tourflow.catalog.business.exception.UnauthorizedBusinessAccessException;
import com.tourflow.catalog.business.repository.ActivityRepository;
import com.tourflow.catalog.business.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Core activity-management logic.
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final BusinessRepository businessRepository;

    public ActivityService(
            ActivityRepository activityRepository,
            BusinessRepository businessRepository
    ) {
        this.activityRepository = activityRepository;
        this.businessRepository = businessRepository;
    }

    // Create a new activity under a business owned by the authenticated user.
    @Transactional
    public ActivityResponse createActivity(
            CreateActivityRequest request,
            UUID ownerId
    ) {

        Business business = businessRepository.findById(request.businessId())
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

        // A non-owner gets the same "not found" as a nonexistent business --
        // this must not confirm the business exists to someone who doesn't own it.
        if (!business.getOwnerId().equals(ownerId)) {
            throw new BusinessNotFoundException("Business not found");
        }

        OffsetDateTime now = OffsetDateTime.now();

        Activity activity = new Activity(
                UUID.randomUUID(),
                business.getId(),
                request.name(),
                request.description(),
                request.location(),
                request.price(),
                request.durationMinutes(),
                request.maxParticipants(),
                request.category(),

                // Not customer-visible until explicitly activated later.
                ActivityStatus.DRAFT,

                now,
                now
        );

        Activity saved = activityRepository.save(activity);

        return ActivityResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public ActivityResponse getActivity(UUID id) {

        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found"));

        return ActivityResponse.fromEntity(activity);
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivities() {

        return activityRepository.findAll()
                .stream()
                .map(ActivityResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivitiesForBusiness(UUID businessId) {

        return activityRepository.findByBusinessId(businessId)
                .stream()
                .map(ActivityResponse::fromEntity)
                .toList();
    }

    // Public search: only activities a tourist is allowed to book.
    @Transactional(readOnly = true)
    public List<ActivityResponse> getActiveActivities() {

        return activityRepository.findByStatus(ActivityStatus.ACTIVE)
                .stream()
                .map(ActivityResponse::fromEntity)
                .toList();
    }

    // Update an activity's details. Only the owner of the activity's business
    // may do this. Status is not editable here -- see activate/deactivate/delete.
    @Transactional
    public ActivityResponse updateActivity(
            UUID activityId,
            UUID ownerId,
            UpdateActivityRequest request
    ) {

        Activity activity = requireOwnedActivity(activityId, ownerId);

        activity.update(
                request.name(),
                request.description(),
                request.location(),
                request.price(),
                request.durationMinutes(),
                request.maxParticipants(),
                request.category()
        );

        return ActivityResponse.fromEntity(activityRepository.save(activity));
    }

    // Soft-delete an activity. Only the owner of the activity's business may
    // do this. The row is kept -- bookings/payments/reviews may reference it.
    @Transactional
    public ActivityResponse deleteActivity(UUID activityId, UUID ownerId) {

        Activity activity = requireOwnedActivity(activityId, ownerId);

        if (activity.getStatus() == ActivityStatus.DELETED) {
            return ActivityResponse.fromEntity(activity);
        }

        activity.setStatus(ActivityStatus.DELETED);
        activity.setUpdatedAt(OffsetDateTime.now());

        return ActivityResponse.fromEntity(activityRepository.save(activity));
    }

    // Make an activity customer-visible. Only the owner of the activity's
    // business may do this.
    @Transactional
    public ActivityResponse activateActivity(UUID activityId, UUID ownerId) {

        Activity activity = requireOwnedActivity(activityId, ownerId);

        if (activity.getStatus() == ActivityStatus.ACTIVE) {
            return ActivityResponse.fromEntity(activity);
        }

        activity.setStatus(ActivityStatus.ACTIVE);
        activity.setUpdatedAt(OffsetDateTime.now());

        return ActivityResponse.fromEntity(activityRepository.save(activity));
    }

    // Hide an activity from customers again. Only the owner of the activity's
    // business may do this.
    @Transactional
    public ActivityResponse deactivateActivity(UUID activityId, UUID ownerId) {

        Activity activity = requireOwnedActivity(activityId, ownerId);

        if (activity.getStatus() == ActivityStatus.INACTIVE) {
            return ActivityResponse.fromEntity(activity);
        }

        activity.setStatus(ActivityStatus.INACTIVE);
        activity.setUpdatedAt(OffsetDateTime.now());

        return ActivityResponse.fromEntity(activityRepository.save(activity));
    }

    // Load an activity and verify the caller owns the business it belongs to.
    // The caller already knows this activity exists (they have its ID from a
    // prior create/list call), so an ownership mismatch is reported as 403,
    // not folded into a 404 the way business-existence is hidden elsewhere.
    private Activity requireOwnedActivity(UUID activityId, UUID ownerId) {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ActivityNotFoundException("Activity not found"));

        Business business = businessRepository.findById(activity.getBusinessId())
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

        if (!business.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedBusinessAccessException(
                    "You are not authorized to manage this activity"
            );
        }

        return activity;
    }
}
