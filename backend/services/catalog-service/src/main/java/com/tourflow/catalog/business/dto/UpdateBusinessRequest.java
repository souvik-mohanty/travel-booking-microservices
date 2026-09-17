package com.tourflow.business.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateBusinessRequest(

        @NotBlank
        String name,

        String description,

        String phone,

        @Email
        String email,

        String address,

        String city,

        String state,

        String country
) {
}
