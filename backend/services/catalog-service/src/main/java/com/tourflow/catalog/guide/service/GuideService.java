package com.tourflow.guide.service;

import com.tourflow.guide.domain.Guide;
import com.tourflow.guide.domain.GuideAvailability;
import com.tourflow.guide.domain.GuideStatus;
import com.tourflow.guide.dto.CreateGuideRequest;
import com.tourflow.guide.dto.GuideResponse;
import com.tourflow.guide.exception.GuideAccessDeniedException;
import com.tourflow.guide.exception.GuideAlreadyExistsException;
import com.tourflow.guide.exception.GuideNotFoundException;
import com.tourflow.guide.repository.GuideRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class GuideService {

    private final GuideRepository guideRepository;

    public GuideService(GuideRepository guideRepository) {
        this.guideRepository = guideRepository;
    }

    // Register the authenticated user as a guide. One guide profile per user.
    @Transactional
    public GuideResponse createGuide(
            CreateGuideRequest request,
            UUID userId
    ) {

        if (guideRepository.existsByUserId(userId)) {
            throw new GuideAlreadyExistsException(
                    "A guide profile already exists for this user"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Guide guide = new Guide(
                UUID.randomUUID(),
                userId,
                request.bio(),
                request.languagesSpoken(),
                request.yearsOfExperience(),
                request.phone(),

                // No verifier role exists yet -- see GuideStatus.
                GuideStatus.PENDING_VERIFICATION,

                GuideAvailability.UNAVAILABLE,
                now,
                now
        );

        try {
            guide = guideRepository.saveAndFlush(guide);
        } catch (DataIntegrityViolationException ex) {
            throw new GuideAlreadyExistsException(
                    "A guide profile already exists for this user"
            );
        }

        return GuideResponse.fromEntity(guide);
    }

    @Transactional(readOnly = true)
    public GuideResponse getGuide(UUID id) {

        Guide guide = guideRepository.findById(id)
                .orElseThrow(() -> new GuideNotFoundException("Guide not found"));

        return GuideResponse.fromEntity(guide);
    }

    @Transactional(readOnly = true)
    public GuideResponse getMyGuideProfile(UUID userId) {

        Guide guide = guideRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new GuideNotFoundException("You do not have a guide profile yet")
                );

        return GuideResponse.fromEntity(guide);
    }

    // Toggle the authenticated guide's own availability.
    @Transactional
    public GuideResponse setAvailability(
            UUID guideId,
            UUID userId,
            GuideAvailability availability
    ) {

        Guide guide = guideRepository.findById(guideId)
                .orElseThrow(() -> new GuideNotFoundException("Guide not found"));

        if (!guide.getUserId().equals(userId)) {
            throw new GuideAccessDeniedException(
                    "You cannot change another guide's availability"
            );
        }

        if (guide.getAvailability() == availability) {
            return GuideResponse.fromEntity(guide);
        }

        guide.setAvailability(availability);
        guide.setUpdatedAt(OffsetDateTime.now());

        return GuideResponse.fromEntity(guideRepository.save(guide));
    }
}
