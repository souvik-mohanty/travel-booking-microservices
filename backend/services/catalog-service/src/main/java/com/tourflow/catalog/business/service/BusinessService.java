package com.tourflow.business.service;

import com.tourflow.business.domain.Business;
import com.tourflow.business.domain.BusinessStatus;
import com.tourflow.business.dto.BusinessResponse;
import com.tourflow.business.dto.CreateBusinessRequest;
import com.tourflow.business.dto.UpdateBusinessRequest;
import com.tourflow.business.exception.BusinessNotFoundException;
import com.tourflow.business.exception.UnauthorizedBusinessAccessException;
import com.tourflow.business.repository.BusinessRepository;
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
}
