package com.tourflow.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// userId here is the recipient, not the caller -- notifications are sent to
// someone else by nature (a service or another user notifying a user), so
// this deliberately isn't taken from the JWT the way most other requests are.
public record CreateNotificationRequest(

        @NotNull
        UUID userId,

        @NotBlank
        String title,

        @NotBlank
        String message
) {
}
