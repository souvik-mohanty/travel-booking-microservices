package com.tourflow.support.dto;

import jakarta.validation.constraints.NotBlank;

// userId is deliberately not accepted here -- it comes from the JWT.
public record CreateTicketRequest(

        @NotBlank
        String subject,

        @NotBlank
        String description
) {
}
