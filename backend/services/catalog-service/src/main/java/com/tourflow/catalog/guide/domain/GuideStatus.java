package com.tourflow.catalog.guide.domain;

// Verification state of a guide. No endpoint transitions PENDING_VERIFICATION
// yet -- that needs an ops/admin authorization model that doesn't exist in
// this system yet (see identity-service's flat "role" claim, always "TOURIST").
public enum GuideStatus {
    PENDING_VERIFICATION,
    VERIFIED,
    SUSPENDED
}
