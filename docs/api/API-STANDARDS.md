# API Standards

Conventions actually followed across every built service (identity, tour,
booking, payment, business, review, user, driver, fleet, guide, trip,
tracking, hotel, notification, support, authorization, audit, file). New
services should match these unless there's a specific reason not to.

## Base paths

REST resources live under `/api/<resource>` (plural nouns:
`/api/bookings`, `/api/rooms`). No `/v1` prefix yet -- versioning isn't
needed until a breaking change actually happens, per the "don't add for
hypothetical future requirements" principle. `/actuator/health` and
`/actuator/info` are exposed per service for monitoring.

## CORS (2026-09-02)

identity-service, tour-service, search-service, and booking-service each
declare a `CorsConfigurationSource` bean in `SecurityConfig` and call
`.cors(cors -> cors.configurationSource(...))` in the filter chain, allowing
the origins in `app.cors.allowed-origins` (default: `localhost:3000`/`3001`/
`5173` -- Vite's configured port, its fallback if that port's taken, and its
own default). **This was missing entirely until the frontend's first real
integration pass** -- every backend endpoint tested fine with `curl` (which
doesn't enforce CORS), but every single request from an actual browser was
silently blocked before it reached `JwtAuthenticationFilter`. `curl`/Postman
testing a new endpoint proves the endpoint works; it does not prove a
browser client can reach it. Any new service the frontend calls directly
needs the same bean -- copy it from one of the four above rather than
assuming Spring Security allows cross-origin requests by default (it
doesn't).

## Authentication

Every service validates the same JWT, signed with one shared secret
(`jwt.secret`, matching identity-service). `JwtAuthenticationFilter` reads
the `Authorization: Bearer <token>` header, and on a valid token sets
`authentication.getName()` to the JWT's `sub` (the user's UUID) and
`authentication.getCredentials()` to the raw token string (so controllers
can forward it to another service). An invalid/expired token just clears
the security context -- it does not fail the request outright, so
`SecurityConfig`'s `permitAll()` routes still work. See `SECURITY.md`.

## Ownership: JWT, not request body

The owning ID (`userId`, `ownerId`, `createdBy`) always comes from
`authentication.getName()`, never from the request body:

```java
UUID userId = UUID.fromString(authentication.getName());
```

Request DTOs deliberately omit an ownership field. The one exception in the
whole codebase is `notification-service`'s `CreateNotificationRequest.userId`
-- that's the *recipient*, not the caller, since a notification is
legitimately sent by one party to another.

## Request/response shape

- DTOs are Java records (`CreateXRequest`, `UpdateXRequest`, `XResponse`).
- `XResponse.fromEntity(entity)` is a static factory on the response record.
- Validation via `jakarta.validation` annotations (`@NotBlank`, `@NotNull`,
  `@Min`, `@DecimalMin`, etc.), triggered by `@Valid @RequestBody`.

## Status codes

| Situation | Status |
|---|---|
| Resource created | `201 Created` |
| Resource read/updated | `200 OK` |
| Action with no response body (delete, mark-all-read) | `204 No Content` |
| Resource not found | `404 Not Found` |
| Caller isn't the owner of a resource they know exists | `403 Forbidden` |
| Caller isn't the owner, but existence itself is sensitive | `404 Not Found` (see below) |
| Duplicate / already exists | `409 Conflict` |
| Invalid state transition, seat/inventory unavailable | `400 Bad Request` |
| `@Valid` field validation failure | `400 Bad Request`, `Map<field, message>` body |
| No/invalid JWT | `401 Unauthorized` |

## Error response bodies

Most services return a plain string body (`ResponseEntity<String>`) for
single-exception handlers -- just the message, no envelope. Validation
failures return `Map<String, String>` (field name -> message).
`review-service` is the one exception: it uses a structured envelope
(`timestamp`/`status`/`error`/`message`) because it distinguishes client
validation errors from upstream-service-unavailable errors and wanted that
visible in the shape of the response. Don't assume one shape platform-wide
when reading a response -- check the specific service's
`GlobalExceptionHandler`.

## 404-vs-403: hiding existence

Two different rules are used deliberately, depending on whether the caller
could already know the resource exists:

- **Creating something under a parent you don't own** (e.g., an activity
  under someone else's business, a room under someone else's hotel): the
  parent lookup returns the *same* `404 <Parent> not found` whether the
  parent doesn't exist or belongs to someone else. The caller only supplied
  an ID -- confirming "it exists, you just don't own it" would leak
  information they shouldn't get from a create call.
- **Managing a resource you already have the ID for** (e.g.,
  activate/deactivate/delete on an activity you created, a driver changing
  their own availability): ownership mismatches return `403`. The caller
  already knows the resource exists (they created it or listed it), so `403`
  doesn't leak anything new.

## Idempotency on status-transition endpoints

`PATCH .../activate`, `.../deactivate`, `.../mark-paid`, `.../complete`, etc.
are idempotent: calling them again when already in the target state is a
no-op that returns `200` with the unchanged resource, not an error. Invalid
*transitions* (completing an unpaid booking, cancelling a completed trip)
still throw a `400`.

## Service-to-service calls

A service calling another service's authenticated API forwards the caller's
JWT (`Authorization: Bearer <token>` from `authentication.getCredentials()`)
via a `RestClient`-based `*Client` component in a `client/` package. When
there's no user context at all (a webhook, a background reconciliation), the
calling service mints its own short-lived JWT signed with the same shared
secret, `role: SERVICE`, subject a well-known constant UUID
(`00000000-0000-0000-0000-000000000001`), ~1 minute TTL. The receiving
service gates that specific endpoint to `hasRole("SERVICE")` in
`SecurityConfig`, declared *before* the general `authenticated()` rule
(Spring Security matches rules in order -- get this backwards and the
service-only endpoint becomes reachable by any real user). See
`payment-service`'s `BookingClient.markBookingPaid` /
`booking-service`'s `SecurityConfig` for the reference implementation.

## Concurrency

Row-level pessimistic locking (`@Lock(LockModeType.PESSIMISTIC_WRITE)` via a
`@Query`-annotated repository method), not application-level checks, guards
any resource with a limited count that can be decremented by concurrent
requests (`ride-service`'s seat booking is the reference implementation --
verified under real concurrent load, not just written and assumed correct).
This is the fallback until Redis distributed locking exists; per the
project's own architecture doc, database transactions remain the primary
consistency mechanism where Redis isn't set up.
