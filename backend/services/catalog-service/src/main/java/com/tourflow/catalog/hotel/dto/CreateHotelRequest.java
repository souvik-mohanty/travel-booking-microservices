package com.tourflow.catalog.hotel.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateHotelRequest(

        @NotBlank
        String name,

        String description,

        String address,

        String city,

        String state,

        String country
) {
}
