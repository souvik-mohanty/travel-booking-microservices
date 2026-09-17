# API Standards

Conventions actually followed across every built service: identity-service,
catalog-service, booking-service, payment-service, mobility-service,
engagement-service, platform-service, insights-service, search-service (see
`architecture/SERVICE-BOUNDARIES.md` for what each absorbed in the
2026-09-06 consolidation, `decisions/ADR-002-SERVICE-CONSOLIDATION.md` for
why). New services should match these unless there's a specific reason not
to.

## Base paths

REST resources live under `/api/<resource>` (plural nouns:
`/api/bookings`, `/api/rooms`). No `/v1` prefix yet -- versioning isn't
needed until a breaking change actually happens, per the "don't add for
hypothetical future requirements" principle. `/actuator/health` and
`/actuator/info` are exposed per service for monitoring.

## CORS

identity-service, catalog-service, booking-service, mobility-service,
insights-service, and search-service each declare a
`CorsConfigurationSource` bean in `SecurityConfig` and call
`.cors(cors -> cors.configurationSource(...))` in the filter chain, allowing
the origins in `app.cors.allowed-origins` (default: `localhost:3000`/`3001`/
`5173` -- Vite's configured port, its fallback if that port's taken, and its
own default). **This was missing entirely until the frontend's first real
integration pass** -- every backend endpoint tested fine with `curl` (which
doesn't enforce CORS), but every single request from an actual browser was
silently blocked before it reached `JwtAuthenticationFilter`. `curl`/Postman
testing a new endpoint proves the endpoint works; it does not prove a
browser client can reach it. `engagement-service` and `platform-service`
don't have one yet -- add it the day the frontend calls either directly,
copying the bean from one of the six above rather than assuming Spring
Security allows cross-origin requests by default (it doesn't). Calling
either through `api-gateway` instead sidesteps this, since only the
gateway's own origin matters then.

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
whole codebase is `platform-service`'s `CreateNotificationRequest.userId`
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

There is no one shape platform-wide -- never assume one when reading a
response; check the specific service's `GlobalExceptionHandler`. Two shapes
exist, and merging services in 2026-09-06 left three services with *both*
inside the same `GlobalExceptionHandler`, one shape per absorbed domain
(each domain's original handler methods were carried over as-is -- see
`ADR-002-SERVICE-CONSOLIDATION.md`):

| Shape | Used by |
|---|---|
| Plain string body (`ResponseEntity<String>`), just the message, no envelope | mobility-service (all domains), platform-service (all domains), identity-service's RBAC + profile domains, engagement-service's support domain, insights-service's audit domain |
| Structured envelope (`timestamp`/`status`/`error`/`message`, or `ErrorResponse` on identity-service's own auth domain) | catalog-service (all domains), identity-service's own auth domain, engagement-service's review domain, insights-service's analytics domain |

Validation failures (`MethodArgumentNotValidException`) return
`Map<String, String>` (field name -> message) directly in most services,
but nested one level under `message` in whichever shape a mixed service's
*first-registered* generic handler used -- only one
`@ExceptionHandler(MethodArgumentNotValidException.class)` method can exist
per `GlobalExceptionHandler` class, so on a mixed service the surviving one
determines the shape for every domain in that service, not just the one it
came from.

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
requests (`mobility-service`'s seat booking is the reference implementation --
verified under real concurrent load, not just written and assumed correct).
This is the fallback until Redis distributed locking exists; per the
project's own architecture doc, database transactions remain the primary
consistency mechanism where Redis isn't set up.
