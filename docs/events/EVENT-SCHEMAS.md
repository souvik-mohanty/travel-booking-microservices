# Event Schemas

**Status: the envelope, `BookingConfirmed`, `TourPublished`, and
`TourCancelled` are real and live; everything below the draft divider is
still a draft.** See `EVENT-CATALOG.md` for what's built vs. planned, and
`COMMUNICATION.md` for how the real ones are wired.

## Envelope

Every event, regardless of topic, should carry the same envelope:

```json
{
  "eventId": "uuid",
  "eventType": "BookingConfirmed",
  "version": 1,
  "occurredAt": "2026-09-02T13:14:26.212715Z",
  "source": "booking-service",
  "correlationId": "uuid",
  "payload": { }
}
```

- `eventId`: unique per event, generated at publish time -- lets a consumer
  deduplicate a redelivered message the same way `ride-service`'s seat
  booking and `payment-service`'s webhook handler are already idempotent by
  construction (a repeat of the same operation is a safe no-op, not an
  error). See `API-STANDARDS.md`'s idempotency section for the pattern this
  should follow once events exist.
- `eventType`: matches the table in `EVENT-CATALOG.md`.
- `version`: events are versioned independently of the API
  (`BookingCreated.v1`, `BookingCreated.v2` if the payload shape ever needs
  to change) -- never silently change what an existing event type means.
- `occurredAt`: when the underlying domain event happened, not when it was
  published (those can differ under retry/backpressure).
- `source`: the producing service's name, matching its `spring.application.name`.
- `correlationId`: threads a single logical operation (e.g., one checkout)
  across multiple events/services for tracing, once distributed tracing
  (OpenTelemetry, per the architecture doc) exists. Not implemented yet --
  there's no `traceId`/`correlationId` propagation anywhere in the codebase
  today, including the REST calls that exist now.
- `payload`: event-specific fields, one record type per `eventType`.

## Example payloads

### `BookingConfirmed` (booking-service) -- live, exactly matches production code

```json
{
  "bookingId": "uuid",
  "tourId": "uuid",
  "userId": "uuid",
  "totalPrice": "24000.00",
  "currency": "INR"
}
```

Mirrors `BookingResponse` in shape (see the `dto/BookingResponse` record
duplicated across `payment-service`, `trip-service`, and `review-service`
today, each a hand-copied subset of `booking-service`'s own response).
`currency` is always the literal `"INR"` (see `BookingEventPublisher`),
matching how payment-service hardcodes the same literal today -- there's no
multi-currency support anywhere in the project yet.

**Known gap:** despite the envelope's `eventId` being described above as
enabling consumer-side deduplication, `notification-service`'s
`BookingEventListener` doesn't actually do this yet -- it creates a new
notification on every message it processes, full stop. A redelivered or
reprocessed `BookingConfirmed` (e.g. after a consumer restart with no
committed offset) would currently produce a duplicate notification. Real
dedup would mean tracking processed `eventId`s (or making notification
creation itself idempotent per booking).

**analytics-service does this dedup for real** -- its `BookingEventListener`
checks `existsByEventId` before inserting into `booking_event_log`, which
also has a DB-level `UNIQUE` constraint on `event_id` as a backstop. A
redelivered `BookingConfirmed` is a logged no-op there, not a duplicate row
that would inflate `GET /api/analytics/bookings/summary`.

### `TourPublished` (tour-service) -- live, exactly matches production code

```json
{
  "tourId": "uuid",
  "title": "Backwaters of Kerala",
  "description": "A 4-day houseboat tour through the Kerala backwaters.",
  "destination": "Kerala",
  "startDate": "2026-11-10",
  "endDate": "2026-11-14",
  "price": "18500.00",
  "maxParticipants": 12,
  "createdBy": "uuid"
}
```

Published from `PATCH /api/tours/{id}/publish` once a `DRAFT` tour
transitions to `PUBLISHED` (idempotent -- publishing an already-`PUBLISHED`
tour is a no-op and does not re-publish the event). Carries everything
search-service needs to build a `TourDocument`, so it never calls back to
tour-service to fetch anything.

### `TourCancelled` (tour-service) -- live, exactly matches production code

```json
{
  "tourId": "uuid"
}
```

Published from `PATCH /api/tours/{id}/cancel`. Deliberately minimal --
search-service only needs the ID to remove the document from its index.

### Everything below this line is still a draft -- not yet built, subject to
change on first implementation.

### `PaymentSuccessful` (payment-service)

```json
{
  "paymentId": "uuid",
  "bookingId": "uuid",
  "customerId": "uuid",
  "amount": "24000.00",
  "currency": "INR",
  "platformFee": "2400.00",
  "businessAmount": "21600.00",
  "gatewayPaymentId": "pay_xxx"
}
```

### `TripLocationUpdated` (tracking-service)

```json
{
  "tripId": "uuid",
  "latitude": "12.971600",
  "longitude": "77.594600",
  "recordedAt": "2026-09-02T13:14:26.212715Z"
}
```

Would let notification/tracking consumers push over WebSocket instead of
the tourist's app polling `GET /api/tracking/{tripId}/locations/latest`
(tracking-service's current, deliberately scoped-down delivery mechanism --
see `SERVICE-BOUNDARIES.md`).

## Not decided yet

- Serialization format (JSON, as sketched above, vs. Avro/Protobuf with a
  schema registry) -- JSON matches everything else in this codebase (every
  REST API is JSON) but doesn't give the compile-time/schema-evolution
  guarantees Avro would.
- Outbox implementation details -- the project's architecture doc calls for
  the Transactional Outbox pattern (write the domain change and an outbox
  row in the same DB transaction, publish from the outbox asynchronously) so
  a service never publishes an event for a database write that didn't
  actually commit. No service has an outbox table today.
