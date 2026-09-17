package com.tourflow.identity.user;

// Identifies how a user's identity was established.
public enum AuthProvider {

    // Registered with an email/password pair we manage ourselves.
    LOCAL,

    // Registered/authenticated by signing in with a Google account (OAuth2).
    GOOGLE
}
