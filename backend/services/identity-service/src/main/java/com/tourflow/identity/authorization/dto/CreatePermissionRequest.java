package com.tourflow.authorization.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePermissionRequest(

        @NotBlank
        String name,

        String description
) {
}
