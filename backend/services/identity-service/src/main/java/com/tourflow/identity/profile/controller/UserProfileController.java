package com.tourflow.identity.profile.controller;

import com.tourflow.identity.profile.dto.CreateUserProfileRequest;
import com.tourflow.identity.profile.dto.UpdateUserProfileRequest;
import com.tourflow.identity.profile.dto.UserProfileResponse;
import com.tourflow.identity.profile.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    // Create the authenticated user's profile.
    @PostMapping("/profile")
    public ResponseEntity<UserProfileResponse> createProfile(
            @Valid @RequestBody CreateUserProfileRequest request,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        UserProfileResponse response = userProfileService.createProfile(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Return the authenticated user's own profile.
    @GetMapping("/profile/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                userProfileService.getMyProfile(userId)
        );
    }

    // Update the authenticated user's own profile.
    @PutMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateUserProfileRequest request,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                userProfileService.updateMyProfile(userId, request)
        );
    }

    // Return a profile by its ID.
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getProfile(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                userProfileService.getProfile(id)
        );
    }
}
