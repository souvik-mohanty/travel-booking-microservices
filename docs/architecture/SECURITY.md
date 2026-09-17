# Security

## JWT

Issued only by identity-service (`JwtService.generateToken`). HMAC-signed
with a shared secret every service is configured with (`jwt.secret`,
default value matches across all services' `application.yml` for local dev;
override via `JWT_SECRET` env var in real deployments). jjwt auto-selects
the HMAC algorithm from key length -- with the current 64-byte development
secret that's **HS384**, not HS256. This matters if a service ever needs to
validate the token through a library other than jjwt's own parser (Spring's
OAuth2 Resource Server / Nimbus defaults to expecting HS256 and will reject
these tokens unless explicitly configured otherwise) -- every service in
this codebase uses the same `JwtService` (`Jwts.parser().verifyWith(...)`)
specifically to avoid that mismatch.

Claims:
- `sub`: the user's UUID (identity-service's `User.id`)
- `email`
- `role`: a plain string, currently always `"TOURIST"` -- identity-service
  has no other role-issuing path yet. Real RBAC lives in
  authorization-service (`Role`/`Permission`/`UserRole`), which is a
  separate, fully-built data model, but nothing currently *consults* it when
  authorizing a request -- see "What's not implemented" below.
- `iat` / `exp`

## Per-request authentication

Every service has the same three files (`security/JwtService.java`,
`JwtAuthenticationFilter.java`, `SecurityConfig.java`), copied deliberately
rather than shared via a library, so each service can evolve its own
authorization rules independently. `JwtAuthenticationFilter` runs once per
request:

1. No `Authorization` header, or doesn't start with `Bearer ` -> continue
   unauthenticated (lets `permitAll()` routes work).
2. Token present -> validate + extract `sub`/`role`. On success, populate
   `SecurityContextHolder` with a `UsernamePasswordAuthenticationToken` whose
   principal name is the user UUID string and whose credentials are the raw
   token (so it can be forwarded to another service).
3. Any exception during parsing -> clear the security context. Invalid
   tokens never partially authenticate.

`SecurityConfig` always: disables CSRF (stateless REST API, no browser
session), sets `STATELESS` session policy, `permitAll()`s
`/actuator/health/**` and `/error` (the `/error` exemption specifically
matters -- without it, Spring's internal re-dispatch to render an error gets
challenged by security too, and a real `404`/`409` turns into a misleading
`401`), returns `401` (not a redirect) on missing/invalid auth, and inserts
`JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.

## Service-to-service auth

For calls with no real user context (payment-service's webhook handler
telling booking-service a payment was captured), the calling service mints
its own JWT: `role: SERVICE`, subject a fixed well-known UUID
(`00000000-0000-0000-0000-000000000001`), signed with the same shared
secret, ~1 minute TTL. This works because the secret is never exposed to
end users or attackers -- only services that already have it in their own
config can mint a token that will validate. The receiving endpoint is gated
with `hasRole("SERVICE")`, declared before the general `authenticated()`
rule. See `payment-service/BookingClient.markBookingPaid` and
`booking-service/SecurityConfig`.

## Authorization patterns

- **Ownership-gated mutation**: the resource's stored owner ID (never a
  request-supplied one) is compared against `authentication.getName()`.
  Mismatch -> `403` (if the caller already knows the resource exists) or the
  same `404` as "not found" (if they're only guessing at a parent ID). See
  `API-STANDARDS.md`.
- **Self-service status transitions**: several services (driver
  verification, guide verification, support ticket status) let the
  resource's own owner drive status changes that would, in a mature system,
  be gated behind an ops/admin/support-agent role. This is deliberate, not
  an oversight: identity-service issues no role besides `"TOURIST"`, so
  there's no actor to gate these behind yet. Each of these spots has an
  inline comment pointing back to this doc.

## What's not implemented yet

- **Authorization-service isn't consulted by anything.** It has real
  `Role`/`Permission`/`UserRole` tables and a working effective-permissions
  endpoint (`GET /api/users/{userId}/permissions`), verified end-to-end, but
  no other service calls it to make an authorization decision. Wiring that
  in is the natural next step once there's an actual admin-gated action to
  protect.
- **No OAuth2 client-credentials / mTLS for service-to-service auth** --
  the mint-your-own-JWT approach above is a deliberate lightweight
  substitute, not a production-grade service mesh.
- **No rate limiting, no audit-log auto-instrumentation** (audit-service
  exists and works, but nothing calls it automatically on sensitive
  operations yet -- every audit entry has to be explicitly created by a
  caller today).
