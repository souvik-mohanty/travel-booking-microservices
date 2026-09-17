package com.tourflow.business.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateActivityRequest(

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
        Integer maxParticipants
) {
}
