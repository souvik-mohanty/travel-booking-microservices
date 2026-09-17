package com.tourflow.identity.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min = 8, max = 100)
        String password,

        @NotBlank
        @Size(max = 100)
        String firstName,

        @Size(max = 100)
        String lastName,

        // Chosen at signup: a regular customer or a business/tour-operator
        // account. Restricted to a fixed allowlist so a client can't send
        // arbitrary values (e.g. "ADMIN" or "SERVICE") to self-elevate --
        // this is the only role a client ever gets to choose for itself.
        @NotBlank
        @Pattern(regexp = "TOURIST|BUSINESS", message = "role must be TOURIST or BUSINESS")
        String role
) {
}