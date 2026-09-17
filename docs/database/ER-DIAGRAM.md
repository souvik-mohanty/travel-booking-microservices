# Entity Relationships

Every relationship crossing a service boundary below is a **plain UUID
column, not a real foreign key** -- Postgres can't enforce a constraint
across schemas owned by different services, and even within one service
schema, cross-service references are deliberately just UUIDs validated (or
not) via a REST call at write time. Real FK constraints only exist for
relationships *within* a single service's schema (e.g., `Room.hotel_id ->
Hotel.id`, both in `hotel_service`).

```mermaid
erDiagram
    User ||--o| UserProfile : "has"
    User ||--o| Driver : "may be"
    User ||--o| Guide : "may be"
    User ||--o{ Business : "owns (many)"
    User ||--o{ Hotel : "owns (many)"
    User ||--o{ Vehicle : "manages (many)"
    User ||--o{ UserRole : "has"

    Business ||--o{ Activity : "offers"
    Hotel ||--o{ Room : "offers"

    Tour ||--o{ Booking : "is booked as"
    Booking ||--o| Payment : "is paid via"
    Booking ||--o| Trip : "is executed as"
    Booking ||--o| Review : "may be reviewed via"

    Activity ||--o{ Review : "is reviewed"
    Business ||--o{ Review : "is reviewed"

    Trip ||--o{ TripLocation : "reports"
    Trip }o--o| Driver : "assigned"
    Trip }o--o| Vehicle : "assigned"

    Ride ||--o{ RideBooking : "has seats booked via"

    Role ||--o{ RolePermission : "grants"
    Permission ||--o{ RolePermission : "granted by"
    Role ||--o{ UserRole : "assigned via"
```

## Cross-service reference map

| From (service) | Column | To (service) | Verified how |
|---|---|---|---|
| booking-service.Booking | `tour_id` | tour-service.Tour | `TourClient` call at booking creation (fetches real price) |
| payment-service.Payment | `booking_id` | booking-service.Booking | `BookingClient` call at payment creation (fetches real amount, verifies ownership) |
| trip-service.Trip | `booking_id` | booking-service.Booking | `BookingClient` call at trip creation (verifies booking exists + ownership) |
| review-service.Review | `booking_id` | booking-service.Booking | `BookingClient` call (verifies booking is `COMPLETED` + ownership) |
| review-service.Review | `business_id`, `activity_id` | business-service.Business/Activity | `BusinessClient` calls (verifies business exists, activity belongs to it) |
| review-service.Review | -- | booking.tour_id / activity | **Not verified.** There's no established relationship between tour-service's `Tour` and business-service's `Activity` -- they're parallel concepts from separate build sessions. Deliberately not faked; see review-service's `ReviewService.createReview` comment. |
| business-service.Activity | `business_id` | (same schema) business-service.Business | Real FK constraint (`fk_activities_business`) |
| hotel-service.Room | `hotel_id` | (same schema) hotel-service.Hotel | Real FK constraint (`fk_rooms_hotel`) |
| ride-service.RideBooking | `ride_id` | (same schema) ride-service.Ride | Real FK constraint (`fk_ride_bookings_ride`) |
| tracking-service.TripLocation | `trip_id` | trip-service.Trip | **Not verified** -- no cross-service check on write (scoped-down v1, see `SERVICE-BOUNDARIES.md`) |
| trip-service.Trip | `driver_id` | driver-service.Driver | **Not verified** -- assigned by trip ID reference only, no existence check |
| trip-service.Trip | `vehicle_id` | fleet-service.Vehicle | **Not verified** -- same as above |
| authorization-service.UserRole | `user_id` | identity-service.User | Not verified -- any UUID accepted, no existence check |
| driver-service.Driver | `user_id` | identity-service.User | Not verified at write time, but is the JWT's own subject (self-registration) |

The "not verified" rows are honest gaps, not oversights papered over --
each one is a real service-to-service integration that would need its own
design pass (an `IdentityClient`? a `DriverClient` in trip-service?) rather
than being bolted on as a side effect of an unrelated feature.

## Why no cross-schema foreign keys

Per `SERVICE-BOUNDARIES.md` and the project's core rule: a service must
never directly access another service's database, so a real FK across
schema boundaries would violate that even though Postgres technically
permits cross-schema FKs within one database. Consistency (an orphaned
`booking_id` in `payment_service.payments` after a hypothetical hard-delete
in `booking_service.bookings`) is accepted as the tradeoff for service
independence -- and mitigated in practice by nothing in this codebase ever
hard-deleting a referenced entity; see "Soft delete" in
`DATABASE-DESIGN.md`.
