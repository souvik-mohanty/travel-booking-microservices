package com.tourflow.identity.profile.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record UpdateUserProfileRequest(

        @NotBlank
        String fullName,

        String phone,

        LocalDate dateOfBirth,

        String address,

        String city,

        String state,

        String country,

        String emergencyContactName,

        String emergencyContactPhone
) {
}
