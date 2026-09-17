# Event Catalog

**Status: three events live, the rest still planned.** As of 2026-09-02,
`booking-service` publishes `BookingConfirmed` to `booking.events`, and
`tour-service` publishes `TourPublished`/`TourCancelled` to `tour.events`.
Three real consumers exist: `notification-service` (`BookingConfirmed` ->
in-app notification), `analytics-service` (`BookingConfirmed` -> append-only
booking analytics log), and `search-service` (`TourPublished`/
`TourCancelled` -> Elasticsearch index) -- see `COMMUNICATION.md` for how
these are wired and the gotchas hit building them. Every other row in the
table below is still just a settled name/topic, not yet produced or consumed
by anything.

## Topics

One topic per producing domain, matching the service that owns the entity:

```
tour.events
booking.events
ride.events
hotel.events
activity.events
payment.events
driver.events
fleet.events
trip.events
tracking.events
review.events
notification.events
```

## Events by producer

| Producer | Event | Would replace / supplement |
|---|---|---|
| tour-service | `TourCreated` | Not published -- tours are created as `DRAFT` (`POST /api/tours`) without an event; only the publish/cancel transitions below actually publish anything today |
| tour-service | `TourPublished` | **Live.** Published from `PATCH /api/tours/{id}/publish` (`DRAFT -> PUBLISHED`, idempotent, creator-only). Consumed by search-service to index the tour. |
| tour-service | `TourCancelled` | **Live.** Published from `PATCH /api/tours/{id}/cancel` (idempotent, creator-only, valid from `DRAFT` or `PUBLISHED`). Consumed by search-service to remove the tour from its index. |
| booking-service | `BookingCreated` | -- |
| booking-service | `BookingConfirmed` | **Live.** Published to `booking.events` from `markBookingPaid`, alongside (not instead of) the synchronous `mark-paid` call from payment-service -- see `COMMUNICATION.md`. Consumed by notification-service (in-app notification) and analytics-service (booking analytics log, with real `eventId` dedup -- see `EVENT-SCHEMAS.md`'s known gap note). |
| booking-service | `BookingCancelled` | Currently: `PATCH /api/bookings/{id}/cancel`, synchronous |
| ride-service | `RideCreated` | -- |
| ride-service | `PassengerAdded` / `PassengerRemoved` | Currently: `POST /api/ride-bookings`, `PATCH .../cancel`, synchronous |
| driver-service | `DriverAssigned` | Currently: `PATCH /api/trips/{id}/assign` (trip-service), synchronous, no cross-check against driver-service |
| trip-service | `TripStarted` | Currently: `PATCH /api/trips/{id}/start`, synchronous |
| tracking-service | `TripLocationUpdated` | Currently: `POST /api/tracking/{tripId}/locations`, REST-polling-only -- this is the event that would let a real WebSocket push replace polling |
| trip-service | `TripCompleted` | Currently: `PATCH /api/trips/{id}/complete`, synchronous |
| hotel-service | `HotelBooked` | Not built -- hotel-service has no booking flow yet, only inventory (Hotel/Room) |
| business-service | `ActivityBooked` | Not built -- same gap |
| payment-service | `PaymentInitiated` / `PaymentSuccessful` / `PaymentFailed` / `PaymentRefunded` | `PaymentSuccessful` currently drives the synchronous `mark-paid` call to booking-service; the others have no consumer at all today |
| review-service | `ReviewCreated` | -- |
| support-service | `SupportTicketCreated` | -- |
| identity-service | `UserRegistered` | -- |

## Envelope

See `EVENT-SCHEMAS.md` for the structure every event should follow once
this is built.

## What's still left once the rest of this exists

- **notification-service** gets real email/SMS/push channels, consuming
  from every topic above -- today it only consumes `BookingConfirmed`, and
  only to create an in-app notification (no email/SMS/push provider is
  wired up yet).
- **search-service** stays Elasticsearch-only and tour-focused for now --
  extending it to hotels/activities/rides would mean each of those services
  publishing its own `*Published` event the same way tour-service does,
  plus a document type per resource.
- **analytics-service** only consumes `BookingConfirmed` today -- richer
  aggregation (revenue by tour, by destination, payment-funnel drop-off)
  needs `PaymentSuccessful`/`PaymentFailed` and `TourPublished` consumers
  added the same way.
- **booking-service's synchronous coupling to payment-service** (the
  `mark-paid` service-JWT call) could become an async `PaymentSuccessful`
  consumer instead -- though the current synchronous version stays even now
  that Kafka works, as a deliberate choice for immediate consistency (the
  booking-lifecycle gap was the single most load-bearing fix in this
  project's history; see `tourflow_architecture_vision` memory), not an
  oversight to fix by default. `BookingConfirmed` is published *alongside*
  it, not as a replacement.
- Every other event in the table above still needs its producer and at
  least one real consumer built, the same way `BookingConfirmed` ->
  notification-service was.
