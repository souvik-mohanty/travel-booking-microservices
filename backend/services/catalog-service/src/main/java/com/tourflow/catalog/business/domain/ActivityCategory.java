package com.tourflow.catalog.business.domain;

// What kind of offering this activity is -- lets a tour's itinerary
// distinguish "things to do" from "how you get around" when a business
// picks activities for a leg (see tour-service's TourActivity).
public enum ActivityCategory {
    ADVENTURE,
    SIGHTSEEING,

    // A vehicle-based transport offering (the business "organises the tour"
    // with their own vehicle) -- modeled as an Activity rather than a
    // separate entity so it stays in the same Business-owned catalog as
    // every other bookable offering, instead of bridging into fleet-service's
    // unrelated fleet-manager ownership model.
    TRANSPORT,

    OTHER
}
