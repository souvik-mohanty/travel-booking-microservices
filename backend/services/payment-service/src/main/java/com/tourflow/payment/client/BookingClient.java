package com.tourflow.payment.client;

import com.tourflow.payment.dto.BookingResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

// HTTP client used by Payment Service to communicate with Booking Service.
@Component
public class BookingClient {

    // Well-known subject for tokens this service mints for itself -- not a
    // real user, so it can't collide with any identity-service-issued UUID.
    private static final UUID SERVICE_ACTOR_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000001");

    private static final Duration SERVICE_TOKEN_TTL = Duration.ofMinutes(1);

    private final RestClient restClient;
    private final SecretKey signingKey;

    public BookingClient(
            @Value("${services.booking.base-url}") String bookingBaseUrl,
            @Value("${jwt.secret}") String jwtSecret
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(bookingBaseUrl)
                .build();
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    // Forward the caller's JWT because Booking Service requires authentication.
    public BookingResponse getBooking(UUID bookingId, String token) {

        return restClient
                .get()
                .uri("/api/bookings/{id}", bookingId)
                .header("Authorization", "Bearer " + token)
                .retrieve()
                .body(BookingResponse.class);
    }

    // Tell Booking Service a payment was captured. Called after a successful
    // checkout verify or webhook capture -- neither has a real user's JWT
    // available (the webhook call has none at all), so this authenticates
    // with a short-lived service-role token instead, signed with the same
    // shared secret every service already trusts.
    public void markBookingPaid(UUID bookingId) {

        restClient
                .patch()
                .uri("/api/bookings/{id}/mark-paid", bookingId)
                .header("Authorization", "Bearer " + mintServiceToken())
                .retrieve()
                .toBodilessEntity();
    }

    private String mintServiceToken() {

        Date now = new Date();
        Date expiration = new Date(now.getTime() + SERVICE_TOKEN_TTL.toMillis());

        return Jwts.builder()
                .subject(SERVICE_ACTOR_ID.toString())
                .claim("role", "SERVICE")
                .issuedAt(now)
                .expiration(expiration)
                .signWith(signingKey)
                .compact();
    }
}
