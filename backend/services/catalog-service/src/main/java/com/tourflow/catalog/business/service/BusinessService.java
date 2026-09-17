package com.tourflow.catalog.business.service;

import com.tourflow.catalog.business.domain.Business;
import com.tourflow.catalog.business.domain.BusinessStatus;
import com.tourflow.catalog.business.dto.BusinessResponse;
import com.tourflow.catalog.business.dto.CreateBusinessRequest;
import com.tourflow.catalog.business.dto.UpdateBusinessRequest;
import com.tourflow.catalog.business.exception.BusinessNotFoundException;
import com.tourflow.catalog.business.exception.UnauthorizedBusinessAccessException;
import com.tourflow.catalog.business.repository.BusinessRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

// Core business-registration logic.
@Service
public class BusinessService {

    private final BusinessRepository businessRepository;

    public BusinessService(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    // Register a new business for the authenticated user.
    @Transactional
    public BusinessResponse createBusiness(
            CreateBusinessRequest request,
            UUID ownerId
    ) {

        OffsetDateTime now = OffsetDateTime.now();

        Business business = new Business(
                UUID.randomUUID(),
                ownerId,
                request.name(),
                request.description(),
                request.phone(),
                request.email(),
                request.address(),
                request.city(),
                request.state(),
                request.country(),

                // No approval workflow yet -- businesses are usable immediately.
                BusinessStatus.ACTIVE,

                now,
                now
        );

        Business saved = businessRepository.save(business);

        return BusinessResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public BusinessResponse getBusiness(UUID id) {

        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

        return BusinessResponse.fromEntity(business);
    }

    @Transactional(readOnly = true)
    public List<BusinessResponse> getBusinesses() {

        return businessRepository.findAll()
                .stream()
                .map(BusinessResponse::fromEntity)
                .toList();
    }

    // Update a business's details. Only the owner may do this.
    @Transactional
    public BusinessResponse updateBusiness(
            UUID id,
            UUID ownerId,
            UpdateBusinessRequest request
    ) {

        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

        if (!business.getOwnerId().equals(ownerId)) {
            throw new UnauthorizedBusinessAccessException(
                    "You are not authorized to manage this business"
            );
        }

        business.update(
                request.name(),
                request.description(),
                request.phone(),
                request.email(),
                request.address(),
                request.city(),
                request.state(),
                request.country()
        );

        return BusinessResponse.fromEntity(
                businessRepository.save(business)
        );
    }

    // Admin moderation: suspend a business, e.g. for a policy violation.
    // Idempotent -- suspending an already-suspended business is a no-op that
    // returns 200 unchanged, matching the status-transition convention in
    // docs/api/API-STANDARDS.md. Ownership doesn't matter here -- only the
    // ADMIN role gate on this endpoint (see SecurityConfig) does.
    @Transactional
    public BusinessResponse suspendBusiness(UUID id) {

        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

        if (business.getStatus() == BusinessStatus.SUSPENDED) {
            return BusinessResponse.fromEntity(business);
        }

        business.setStatus(BusinessStatus.SUSPENDED);
        business.setUpdatedAt(OffsetDateTime.now());

        return BusinessResponse.fromEntity(businessRepository.save(business));
    }

    // Admin moderation: lift a suspension.
    @Transactional
    public BusinessResponse reinstateBusiness(UUID id) {

        Business business = businessRepository.findById(id)
                .orElseThrow(() -> new BusinessNotFoundException("Business not found"));

        if (business.getStatus() == BusinessStatus.ACTIVE) {
            return BusinessResponse.fromEntity(business);
        }

        business.setStatus(BusinessStatus.ACTIVE);
        business.setUpdatedAt(OffsetDateTime.now());

        return BusinessResponse.fromEntity(businessRepository.save(business));
    }
}
