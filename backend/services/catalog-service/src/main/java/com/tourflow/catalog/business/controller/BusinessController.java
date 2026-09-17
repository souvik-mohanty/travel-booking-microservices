package com.tourflow.catalog.business.controller;

import com.tourflow.catalog.business.dto.ActivityResponse;
import com.tourflow.catalog.business.dto.BusinessResponse;
import com.tourflow.catalog.business.dto.CreateBusinessRequest;
import com.tourflow.catalog.business.dto.UpdateBusinessRequest;
import com.tourflow.catalog.business.service.ActivityService;
import com.tourflow.catalog.business.service.BusinessService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

// REST API for business registration, lookup, and updates.
//
// DELETE, approval/verification, and payout details are deliberately not
// exposed yet -- see the roadmap this was built against.
@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessService businessService;
    private final ActivityService activityService;

    public BusinessController(
            BusinessService businessService,
            ActivityService activityService
    ) {
        this.businessService = businessService;
        this.activityService = activityService;
    }

    // Register a business for the authenticated user.
    @PostMapping
    public ResponseEntity<BusinessResponse> createBusiness(
            @Valid @RequestBody CreateBusinessRequest request,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        BusinessResponse response = businessService.createBusiness(request, ownerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Return all businesses.
    @GetMapping
    public ResponseEntity<List<BusinessResponse>> getBusinesses() {

        return ResponseEntity.ok(
                businessService.getBusinesses()
        );
    }

    // Return a single business by ID.
    @GetMapping("/{id}")
    public ResponseEntity<BusinessResponse> getBusiness(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                businessService.getBusiness(id)
        );
    }

    // Update the authenticated user's business.
    @PutMapping("/{id}")
    public ResponseEntity<BusinessResponse> updateBusiness(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBusinessRequest request,
            Authentication authentication
    ) {

        UUID ownerId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                businessService.updateBusiness(id, ownerId, request)
        );
    }

    // Return all activities offered by a business.
    @GetMapping("/{businessId}/activities")
    public ResponseEntity<List<ActivityResponse>> getBusinessActivities(
            @PathVariable UUID businessId
    ) {

        return ResponseEntity.ok(
                activityService.getActivitiesForBusiness(businessId)
        );
    }

    // Admin moderation -- see SecurityConfig for the ADMIN-role gate.
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<BusinessResponse> suspendBusiness(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                businessService.suspendBusiness(id)
        );
    }

    @PatchMapping("/{id}/reinstate")
    public ResponseEntity<BusinessResponse> reinstateBusiness(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                businessService.reinstateBusiness(id)
        );
    }
}
