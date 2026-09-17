# Database Design

## Database-per-service, one physical instance

Every service (except pure scaffolds) has its own Flyway-managed PostgreSQL
schema: `?currentSchema=<service>_service` in the JDBC URL, plus
`spring.flyway.schemas: <service>_service` and `create-schemas: true` so the
schema is created automatically on first boot. All of these currently point
at one shared `tour_planner` database/container in local dev
(`docker-compose.yml`'s `postgres` service) purely as a development
convenience -- nothing in the code assumes they share an instance, and
production can split them onto separate database servers without any
application changes.

No service is configured with credentials or a connection string for any
other service's schema. Cross-service data access is always a REST call,
never a direct query.

## Entity conventions

- `UUID` primary keys, assigned by the application (`UUID.randomUUID()` in
  the service layer), not database-generated (`@GeneratedValue`). Every
  entity's constructor takes the full set of fields including `id`,
  `status`, and both timestamps -- there's no partially-constructed entity
  state.
- `OffsetDateTime` / `TIMESTAMP WITH TIME ZONE` (or `TIMESTAMPTZ`, the same
  type) for all timestamps -- never a bare `TIMESTAMP`.
- `created_at` + `updated_at` on almost everything. The one deliberate
  exception is `audit_logs`, which has only `occurred_at` -- it's
  append-only, so there's no `updated_at` to have.
- Enums stored as `VARCHAR` via `@Enumerated(EnumType.STRING)`, with a
  matching `CHECK` constraint in the migration listing the same values. Two
  places to update when a state is added -- the Java enum and the SQL
  `CHECK` -- by design, so a bad value can't sneak into the column from
  outside the application either.
- Entities are effectively immutable after construction except for a small
  set of explicit mutator methods (`setStatus`, `setUpdatedAt`, or a bulk
  `update(...)` covering all owner-editable fields at once). No generic
  setters-for-everything.

## Soft delete

Resources that other services may reference historically (an `Activity` a
past `Booking` points to, a `Room` a past reservation used) are never
physically deleted. A `DELETED` status value is added to the entity's status
enum instead, set via the same idempotent status-transition pattern as
`activate`/`deactivate`. See `business-service`'s `Activity`/`ActivityStatus`
and `hotel-service`'s `Room`/`RoomStatus` for the reference implementation.

`audit_logs` are never deleted or updated at all -- there's no delete
endpoint, full stop.

## Concurrency: pessimistic locking, not optimistic

Where a resource has a limited, decrementable count that concurrent requests
race over (`ride-service`'s `Ride.availableSeats`), the read-then-check-then-
write happens inside one `@Transactional` method against a row locked with
`@Lock(LockModeType.PESSIMISTIC_WRITE)`:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select r from Ride r where r.id = :id")
Optional<Ride> findByIdForUpdate(UUID id);
```

This was verified under real concurrent load (10 parallel requests booking
seats on a 5-seat ride: exactly 5 succeeded, 5 correctly rejected,
`availableSeats` never went negative), not just assumed correct from reading
the code. This is the pattern to reuse for the next capacity-limited
resource (hotel room inventory, activity capacity) until Redis distributed
locking exists -- per the project's own architecture doc, database
transactions are the primary consistency mechanism where Redis isn't set up.

## Race-safe uniqueness

A duplicate-prevention pattern shows up repeatedly: an application-level
pre-check (`existsByX`, `findByX`) for a fast, friendly error, backed by a
real DB `UNIQUE` constraint as the actual guarantee, with the pre-check's
window closed via `saveAndFlush` inside a `try/catch
(DataIntegrityViolationException)` that re-throws the same client-facing
error. Examples: one payment per booking, one user profile per user, one
driver/guide profile per user, one business-owner-name pair, unique vehicle
registration numbers.

## Migrations

Flyway, `V<n>__description.sql` under `src/main/resources/db/migration`,
`ddl-auto: validate` (Hibernate validates the schema Flyway created; it
never generates DDL itself). Early in a service's life, while nothing
depends on the exact migration history yet, `V1` gets edited in place rather
than accumulating `V2`, `V3` corrections for the same not-yet-shipped table
-- each such edit in this project's history required dropping and
recreating the local dev schema (`DROP SCHEMA <name>_service CASCADE`),
always confirmed with the user first since it's a destructive action. Once a
service has real data, this stops being appropriate and new migrations
should be added instead of editing history.
