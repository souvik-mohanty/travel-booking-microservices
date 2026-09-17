package com.tourflow.user.dto;

import com.tourflow.user.domain.UserProfile;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfileResponse(
        UUID id,
        UUID userId,
        String fullName,
        String phone,
        LocalDate dateOfBirth,
        String address,
        String city,
        String state,
        String country,
        String emergencyContactName,
        String emergencyContactPhone,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static UserProfileResponse fromEntity(UserProfile profile) {
        return new UserProfileResponse(
                profile.getId(),
                profile.getUserId(),
                profile.getFullName(),
                profile.getPhone(),
                profile.getDateOfBirth(),
                profile.getAddress(),
                profile.getCity(),
                profile.getState(),
                profile.getCountry(),
                profile.getEmergencyContactName(),
                profile.getEmergencyContactPhone(),
                profile.getCreatedAt(),
                profile.getUpdatedAt()
        );
    }
}
