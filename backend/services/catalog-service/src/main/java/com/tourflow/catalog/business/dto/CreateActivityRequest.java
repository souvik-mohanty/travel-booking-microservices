package com.tourflow.catalog.business.dto;

import com.tourflow.catalog.business.domain.ActivityCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateActivityRequest(

        @NotNull
        UUID businessId,

        @NotBlank
        String name,

        String description,

        String location,

        @NotNull
        @DecimalMin("0.00")
        BigDecimal price,

        @NotNull
        @Min(1)
        Integer durationMinutes,

        @NotNull
        @Min(1)
        Integer maxParticipants,

        @NotNull
        ActivityCategory category
) {
}
