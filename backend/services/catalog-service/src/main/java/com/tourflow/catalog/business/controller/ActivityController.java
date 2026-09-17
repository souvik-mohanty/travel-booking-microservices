package com.tourflow.catalog.business.controller;

import com.tourflow.catalog.business.dto.ActivityResponse;
import com.tourflow.catalog.business.dto.CreateActivityRequest;
import com.tourflow.catalog.business.dto.UpdateActivityRequest;
import com.tourflow.catalog.business.service.ActivityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// REST API for activity registration, lookup, updates, and lifecycle management.
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    // Create an activity under a business owned by the authenticated user.
    @PostMapping
    public ResponseEntity<ActivityResponse> createActivity(
            @Valid @RequestBody CreateActivityRequest request,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        ActivityResponse response = activityService.createActivity(request, ownerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Return all activities.
    @GetMapping
    public ResponseEntity<List<ActivityResponse>> getActivities() {

        return ResponseEntity.ok(
                activityService.getActivities()
        );
    }

    // Return a single activity by ID.
    @GetMapping("/{id}")
    public ResponseEntity<ActivityResponse> getActivity(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                activityService.getActivity(id)
        );
    }

    // Public search: only activities a tourist is allowed to book.
    @GetMapping("/active")
    public ResponseEntity<List<ActivityResponse>> getActiveActivities() {

        return ResponseEntity.ok(
                activityService.getActiveActivities()
        );
    }

    // Update an activity owned by the authenticated user's business.
    @PutMapping("/{id}")
    public ResponseEntity<ActivityResponse> updateActivity(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateActivityRequest request,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                activityService.updateActivity(id, ownerId, request)
        );
    }

    // Soft-delete an activity owned by the authenticated user's business.
    @DeleteMapping("/{id}")
    public ResponseEntity<ActivityResponse> deleteActivity(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                activityService.deleteActivity(id, ownerId)
        );
    }

    // Business owner activates their activity.
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ActivityResponse> activateActivity(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                activityService.activateActivity(id, ownerId)
        );
    }

    // Business owner deactivates their activity.
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ActivityResponse> deactivateActivity(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                activityService.deactivateActivity(id, ownerId)
        );
    }
}
