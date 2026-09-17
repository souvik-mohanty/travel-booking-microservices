package com.tourflow.guide.dto;

import jakarta.validation.constraints.Min;

// userId is deliberately not accepted here -- it comes from the JWT.
public record CreateGuideRequest(

        String bio,

        String languagesSpoken,

        @Min(0)
        Integer yearsOfExperience,

        String phone
) {
}
