package com.tourflow.driver.domain;

// Self-toggled by the driver -- separate from DriverStatus, which tracks
// whether they're verified to drive at all.
public enum DriverAvailability {
    AVAILABLE,
    UNAVAILABLE
}
