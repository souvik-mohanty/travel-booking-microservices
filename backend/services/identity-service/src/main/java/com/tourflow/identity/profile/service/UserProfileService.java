package com.tourflow.user.service;

import com.tourflow.user.domain.UserProfile;
import com.tourflow.user.dto.CreateUserProfileRequest;
import com.tourflow.user.dto.UpdateUserProfileRequest;
import com.tourflow.user.dto.UserProfileResponse;
import com.tourflow.user.exception.UserProfileAlreadyExistsException;
import com.tourflow.user.exception.UserProfileNotFoundException;
import com.tourflow.user.repository.UserProfileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    // Create the authenticated user's profile. One per user.
    @Transactional
    public UserProfileResponse createProfile(
            CreateUserProfileRequest request,
            UUID userId
    ) {

        if (userProfileRepository.existsByUserId(userId)) {
            throw new UserProfileAlreadyExistsException(
                    "A profile already exists for this user"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        UserProfile profile = new UserProfile(
                UUID.randomUUID(),
                userId,
                request.fullName(),
                request.phone(),
                request.dateOfBirth(),
                request.address(),
                request.city(),
                request.state(),
                request.country(),
                request.emergencyContactName(),
                request.emergencyContactPhone(),
                now,
                now
        );

        // The existsByUserId check above is a fast pre-check, not the
        // guarantee: two concurrent requests for the same user can both pass
        // it before either commits. uk_user_profiles_user is the guarantee.
        try {
            profile = userProfileRepository.saveAndFlush(profile);
        } catch (DataIntegrityViolationException ex) {
            throw new UserProfileAlreadyExistsException(
                    "A profile already exists for this user"
            );
        }

        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(UUID id) {

        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile not found"));

        return UserProfileResponse.fromEntity(profile);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(UUID userId) {

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new UserProfileNotFoundException("You do not have a profile yet")
                );

        return UserProfileResponse.fromEntity(profile);
    }

    // Update the authenticated user's own profile.
    @Transactional
    public UserProfileResponse updateMyProfile(
            UUID userId,
            UpdateUserProfileRequest request
    ) {

        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new UserProfileNotFoundException("You do not have a profile yet")
                );

        profile.update(
                request.fullName(),
                request.phone(),
                request.dateOfBirth(),
                request.address(),
                request.city(),
                request.state(),
                request.country(),
                request.emergencyContactName(),
                request.emergencyContactPhone()
        );

        return UserProfileResponse.fromEntity(userProfileRepository.save(profile));
    }
}
