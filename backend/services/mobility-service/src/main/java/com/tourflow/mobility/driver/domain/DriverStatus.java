package com.tourflow.driver.domain;

// Verification state of a driver. No endpoint transitions PENDING_VERIFICATION
// yet -- that needs an ops/admin authorization model that doesn't exist in
// this system yet (see identity-service's flat "role" claim, always "TOURIST").
public enum DriverStatus {
    PENDING_VERIFICATION,
    VERIFIED,
    SUSPENDED
}
