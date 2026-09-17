# Service Boundaries

Target: 25 services across 6 domains, per the project's architecture vision.
This document maps that target onto what actually exists, and records the
two deliberate boundary decisions made along the way.

## Identity & Platform

| Service | Owns | Status |
|---|---|---|
| identity-service | Login, registration, JWT issuance, OAuth2 | Full |
| user-service | User profile (name, phone, DOB, address, emergency contact) -- separate from identity's login/credential record | Full |
| authorization-service | Role, Permission, RolePermission, UserRole -- RBAC data model | Full, unconsulted (see `SECURITY.md`) |

## Travel & Commerce

| Service | Owns | Status |
|---|---|---|
| tour-service | Tours, destinations, itineraries | Full |
| booking-service | Booking lifecycle: `PENDING -> PAID -> COMPLETED` / `CANCELLED` | Full |
| business-service | Business (tour operator/activity provider) + Activity, two-level owner/FK model | Full |
| hotel-service | Hotel + Room, same two-level model as Business+Activity | Full |
| ride-service | Ride (shared/private/family) + RideBooking, with real seat-concurrency locking | Full |
| payment-service | Razorpay order/verify/webhook, `PaymentStatus` + separate `PayoutStatus` | Full |

## Operations

| Service | Owns | Status |
|---|---|---|
| driver-service | Driver profile, verification status (self-registered, `PENDING_VERIFICATION` forever -- no verifier role exists), availability | Full |
| fleet-service | Vehicle, owner-scoped, status lifecycle | Full |
| guide-service | Guide profile -- mirrors driver-service exactly | Full |
| trip-service | Trip execution: `SCHEDULED -> IN_PROGRESS -> COMPLETED`/`CANCELLED`, tied to a real booking | Full |
| tracking-service | Trip location points, REST-polling (scoped down from the target WebSocket+Redis design) | Full, scoped |

## Platform Services

| Service | Owns | Status |
|---|---|---|
| notification-service | In-app notifications only (scoped down from the target Email/SMS/Push/In-App design); now also a real Kafka consumer of `booking.events` -- see `COMMUNICATION.md` | Full, scoped |
| review-service | Reviews, gated on a booking actually reaching `COMPLETED` | Full |
| support-service | Support tickets, self-service status transitions | Full |
| search-service | Elasticsearch-backed search | Full -- consumes `TourPublished`/`TourCancelled` off `tour.events` to index/remove a `TourDocument`; `GET /api/search/tours` (`q`/`destination`/`minPrice`/`maxPrice`, paginated) queries it via a `CriteriaQuery`. No relational database of its own -- see `COMMUNICATION.md`. |
| analytics-service | Business/operational analytics | Full -- consumes `BookingConfirmed` off `booking.events` into an append-only, `eventId`-deduped `booking_event_log`; `GET /api/analytics/bookings/summary` and `.../daily` aggregate it at read time rather than maintaining a separate running total. See `COMMUNICATION.md`. |
| audit-service | Append-only audit log | Full |
| file-service | File metadata + real local-disk storage (stand-in for S3) | Full |

## Intelligence

| Service | Owns | Status |
|---|---|---|
| ai-orchestrator-service | Coordinates LLMs/tools/AI workflows | Scaffold only -- needs an LLM provider decision |
| ai-chatbot-service | Conversational travel assistant | Scaffold only -- needs an LLM provider decision |

## Deliberate boundary decisions (recorded per the project's own §13 rule: "record the decision" when a genuine architectural problem is found)

1. **No separate organization-service.** The original vision doc's
   "Organization Service" responsibility (agencies, operators, hotels,
   memberships) was split differently in practice: business-service already
   covers agencies/operators/activities, and hotel-service covers hotels.
   Building a third, competing "Organization Service" that overlaps both
   would create two services claiming the same domain. If a real
   multi-tenant "organization" concept (an agency that *has* multiple
   businesses/hotels under it) becomes a real requirement, that's worth a
   fresh design pass, not a same-day scaffold.
2. **No separate refund-payout-service.** payment-service's `Payment` entity
   already models the business-payout side of the flow (`PayoutStatus`:
   `NOT_ELIGIBLE -> ELIGIBLE -> PROCESSING -> PAID -> FAILED`), separately
   from `PaymentStatus` on the customer side. A standalone Refund & Payout
   Service would duplicate that data model. If payout logic grows complex
   enough to need its own team/deploy cadence, it can be split out of
   payment-service later -- there's no cross-service coupling forcing that
   decision now.

## Database-per-service

Every service above (except the 2 remaining pure scaffolds, and
search-service which is Elasticsearch-only -- see above) owns its own
PostgreSQL schema via `?currentSchema=<name>_service` on one shared
`tour_planner` database (dev-only simplification -- nothing stops separating
these into distinct database instances in production). No service queries
another service's tables. All cross-service reads are synchronous REST calls
with the caller's JWT forwarded. See `DATABASE-DESIGN.md`.
