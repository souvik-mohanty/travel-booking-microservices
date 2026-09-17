# ADR-001: Microservices Architecture

## Status

Accepted. In effect since the project's first service (identity-service)
and consistently applied through all 21 services built as of 2026-09-02.

## Context

TourFlow needed to demonstrate production-style distributed-systems
engineering (per the project's stated goals: microservices, database per
service, event-driven communication, distributed-system concepts, secure
auth, real-time tracking, search, caching, observability) rather than a
monolith with a few extra layers. The domain itself also naturally
decomposes into largely-independent bounded contexts (identity, tours,
bookings, payments, driver/fleet operations, reviews, support, ...), each
with its own data lifecycle and its own reasons to change independently.

## Decision

Build TourFlow as a set of independently deployable Spring Boot services,
one per bounded context, each:

- owning its own PostgreSQL schema, never reading another service's tables
  directly (see `DATABASE-DESIGN.md`);
- exposing a REST API secured by a shared JWT scheme (see `SECURITY.md`);
- communicating with other services synchronously via REST for "I need an
  answer now" reads, with Kafka reserved for "something happened" events
  once it exists (see `COMMUNICATION.md`);
- following the same conventions for entities, DTOs, exceptions, and
  security wiring (see `API-STANDARDS.md`) -- copied per-service rather
  than shared via a library, so each service can diverge when it needs to
  without a central dependency forcing lockstep changes.

Design the full 25-service target up front, but implement incrementally in
vertical slices, each integrated with what already exists before moving to
the next (per the project's own §13 development philosophy) -- this is why
services were built in dependency order (Identity -> Tour -> Booking ->
Payment -> Business -> Review -> the remaining Travel/Operations/Platform
services) rather than all scaffolded-then-filled-in at once.

## Consequences

**Accepted costs:**
- No cross-service transactions -- consistency across services is eventual,
  enforced by explicit REST verification calls (`BookingClient`,
  `TourClient`, etc.) rather than a database-level guarantee. See
  `ER-DIAGRAM.md`'s "not verified" rows for the honest gaps this leaves.
- Real duplicated boilerplate: the JWT security trio and the
  `GlobalExceptionHandler`/DTO/entity conventions are copy-pasted into every
  service rather than shared. This is a deliberate tradeoff for
  independence, not an oversight -- see `API-STANDARDS.md`.
- No distributed transaction/Saga pattern implemented yet, despite being a
  stated goal. Every multi-step flow built so far (create payment -> mark
  booking paid; create booking -> nothing rolls back tour capacity on
  failure) is either single-service or a best-effort side effect, not a
  compensating-transaction saga.
- Operational overhead: 21+ separately running services in local dev (one
  Postgres instance, `docker-compose` for infra, a VS Code task per
  service).

**What this bought:**
- Every service so far has been buildable, testable, and verifiable in
  isolation -- each was built, booted against a real database, and exercised
  with real HTTP calls (including deliberately adversarial ones: cross-user
  access attempts, concurrent requests, invalid state transitions) before
  being considered done.
- Two genuine architectural bugs were caught specifically *because* of this
  isolation discipline: a Spring Security rule-ordering bug that would have
  let any authenticated user hit a service-only endpoint, and a
  `@Transactional` rollback swallowing a status update that needed to
  survive the exception it was thrown alongside.
- The service boundary questions that came up mid-build (should
  Organization Service exist separately from Business Service? should
  Refund & Payout Service exist separately from Payment Service?) were
  resolved by *not* building a redundant service, rather than by rigidly
  following the original 25-service list -- see `SERVICE-BOUNDARIES.md`'s
  "deliberate boundary decisions" section.

## Related

- `SERVICE-BOUNDARIES.md` -- what each service owns, and the two boundary
  decisions made
- `DATABASE-DESIGN.md` -- schema-per-service conventions
- `SECURITY.md` -- the shared JWT scheme
- `COMMUNICATION.md` -- REST is still the default; Kafka now has one real
  producer/consumer pair, gRPC/WebSocket not yet built
