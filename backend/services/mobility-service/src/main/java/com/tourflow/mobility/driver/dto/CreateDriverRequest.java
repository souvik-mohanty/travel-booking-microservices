package com.tourflow.mobility.driver.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// userId is deliberately not accepted here -- it comes from the JWT.
public record CreateDriverRequest(

        @NotBlank
        String licenseNumber,

        @NotNull
        @Future(message = "License must not already be expired")
        LocalDate licenseExpiryDate,

        String phone
) {
}
