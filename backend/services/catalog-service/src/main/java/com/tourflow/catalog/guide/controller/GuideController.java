package com.tourflow.catalog.guide.controller;

import com.tourflow.catalog.guide.domain.GuideAvailability;
import com.tourflow.catalog.guide.dto.CreateGuideRequest;
import com.tourflow.catalog.guide.dto.GuideResponse;
import com.tourflow.catalog.guide.service.GuideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/guides")
public class GuideController {

    private final GuideService guideService;

    public GuideController(GuideService guideService) {
        this.guideService = guideService;
    }

    @PostMapping
    public ResponseEntity<GuideResponse> createGuide(
            @Valid @RequestBody CreateGuideRequest request,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        GuideResponse response = guideService.createGuide(request, userId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<GuideResponse> getMyGuideProfile(
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                guideService.getMyGuideProfile(userId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<GuideResponse> getGuide(
            @PathVariable UUID id
    ) {

        return ResponseEntity.ok(
                guideService.getGuide(id)
        );
    }

    @PatchMapping("/{id}/available")
    public ResponseEntity<GuideResponse> markAvailable(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                guideService.setAvailability(id, userId, GuideAvailability.AVAILABLE)
        );
    }

    @PatchMapping("/{id}/unavailable")
    public ResponseEntity<GuideResponse> markUnavailable(
            @PathVariable UUID id,
            Authentication authentication
    ) {

        UUID userId = UUID.fromString(authentication.getName());

        return ResponseEntity.ok(
                guideService.setAvailability(id, userId, GuideAvailability.UNAVAILABLE)
        );
    }
}
