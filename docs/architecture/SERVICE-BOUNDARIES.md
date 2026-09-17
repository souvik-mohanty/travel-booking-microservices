# Service Boundaries

Originally scaffolded as 25 services across 6 domains, per the project's
architecture vision. Once every domain that could be built without missing
infra or an external decision actually was built, the backend was
consolidated down to 9 domain services + an API gateway (2026-09-06). This
document maps the current 9 onto the domains they absorbed, and records the
boundary decisions made along the way -- both the original two (no
organization-service, no refund-payout-service) and the consolidation
itself.

## identity-service (`:8081`)

| Absorbed | Owns | Status |
|---|---|---|
| identity-service (kept its name/port) | Login, registration, JWT issuance, OAuth2 | Full |
| user-service | User profile (name, phone, DOB, address, emergency contact) -- separate from identity's login/credential record | Full |
| authorization-service | Role, Permission, RolePermission, UserRole -- RBAC data model | Full, unconsulted (see `SECURITY.md`) |

All three are "who is this person, what can they do" -- none called each
other over REST before the merge, so nothing about their internal logic
changed, only that they now run in one JVM instead of three.

## catalog-service (`:8082`)

| Absorbed | Owns | Status |
|---|---|---|
| tour-service (kept its port) | Tours, destinations, itineraries (legs + attached activities) | Full |
| business-service | Business (tour operator/activity provider) + Activity, two-level owner/FK model | Full |
| hotel-service | Hotel + Room + RoomReservation, same two-level model as Business+Activity | Full |
| guide-service | Guide profile -- mirrors driver-service (now in mobility-service) exactly | Full |

All four are "things a tourist books". Two REST calls between tour-service
and the others became in-process calls once merged -- see
`ARCHITECTURE.md`'s "Why two REST calls became in-process calls".

## booking-service (`:8083`) and payment-service (`:8084`)

Unchanged, and deliberately **not** merged with each other or with anything
else -- see "Deliberate boundary decisions" below.

| Service | Owns | Status |
|---|---|---|
| booking-service | Booking lifecycle: `PENDING -> PAID -> COMPLETED` / `CANCELLED` | Full |
| payment-service | Razorpay order/verify/webhook, `PaymentStatus` + separate `PayoutStatus` | Full |

## mobility-service (`:8086`)

| Absorbed | Owns | Status |
|---|---|---|
| ride-service (kept its port) | Ride (shared/private/family) + RideBooking, with real seat-concurrency locking | Full |
| driver-service | Driver profile, verification status (self-registered, `PENDING_VERIFICATION` forever -- no verifier role exists), availability | Full |
| fleet-service | Vehicle, owner-scoped, status lifecycle | Full |
| trip-service | Trip execution: `SCHEDULED -> IN_PROGRESS -> COMPLETED`/`CANCELLED`, tied to a real booking | Full |
| tracking-service | Trip location points, REST-polling (scoped down from the target WebSocket+Redis design) | Full, scoped |

All five are "getting people from A to B". Trip-service's REST calls to
tour-service (now catalog-service) and booking-service stay real network
calls -- both targets live in different merged services.

## engagement-service (`:8087`)

| Absorbed | Owns | Status |
|---|---|---|
| review-service (kept its port) | Reviews, gated on a booking actually reaching `COMPLETED` | Full |
| support-service | Support tickets, self-service status transitions | Full |

Both are post-purchase customer interaction. Review's REST calls to
booking-service and catalog-service (formerly business-service) stay real
network calls.

## platform-service (`:8088`)

| Absorbed | Owns | Status |
|---|---|---|
| notification-service (kept its port's role as primary) | In-app notifications only (scoped down from the target Email/SMS/Push/In-App design); real Kafka consumer of `booking.events` -- see `COMMUNICATION.md` | Full, scoped |
| file-service | File metadata + real local-disk storage (stand-in for S3) | Full |

Grouped as generic cross-cutting platform utilities -- there's no shared
business logic between notifications and file storage, they're just both
small, self-contained, infrastructure-flavored domains.

## insights-service (`:8089`)

| Absorbed | Owns | Status |
|---|---|---|
| analytics-service (kept its role as primary) | Business/operational analytics -- consumes `BookingConfirmed` off `booking.events` into an append-only, `eventId`-deduped `booking_event_log`; aggregates it at read time | Full |
| audit-service | Append-only audit log | Full |

Grouped as "observability data" -- both are read-heavy, event-driven-or-
event-sourced records of what happened, with no shared business logic
between them either.

## search-service (`:8099`)

Unchanged, and deliberately **not** merged with anything -- see "Deliberate
boundary decisions" below.

| Service | Owns | Status |
|---|---|---|
| search-service | Elasticsearch-backed search | Full -- consumes `TourPublished`/`TourCancelled` off `tour.events` to index/remove a `TourDocument`; `GET /api/search/tours` (`q`/`destination`/`minPrice`/`maxPrice`, paginated) queries it via a `CriteriaQuery`. No relational database of its own -- see `COMMUNICATION.md`. |

## Dropped: ai-orchestrator-service, ai-chatbot-service

Both were still empty Phase-0 scaffolds (pom.xml, application.yml, the JWT
security trio, no controllers/entities/migrations) at consolidation time,
blocked on an LLM provider decision that hasn't been made yet. Deleted
rather than merged into anything -- there was no domain logic to preserve,
and folding an empty shell into another service would just create a
same-effect-as-deleting outcome with extra confusion. Revisit as a fresh
build once the provider decision is made.

## Deliberate boundary decisions

1. **No separate organization-service.** The original vision doc's
   "Organization Service" responsibility (agencies, operators, hotels,
   memberships) was split differently in practice: catalog-service's
   business sub-domain already covers agencies/operators/activities, and
   its hotel sub-domain covers hotels. Building a third, competing
   "Organization Service" that overlaps both would create two services
   claiming the same domain. If a real multi-tenant "organization" concept
   (an agency that *has* multiple businesses/hotels under it) becomes a
   real requirement, that's worth a fresh design pass, not a same-day
   scaffold.
2. **No separate refund-payout-service.** payment-service's `Payment`
   entity already models the business-payout side of the flow
   (`PayoutStatus`: `NOT_ELIGIBLE -> ELIGIBLE -> PROCESSING -> PAID ->
   FAILED`), separately from `PaymentStatus` on the customer side. A
   standalone Refund & Payout Service would duplicate that data model. If
   payout logic grows complex enough to need its own team/deploy cadence,
   it can be split out of payment-service later -- there's no cross-service
   coupling forcing that decision now.
3. **booking-service and payment-service stay separate, even after
   consolidating everything else.** This is an explicit rule from the
   project's own architecture vision ("Payment processing must be isolated
   from the Booking Service"), not an oversight -- see
   `ADR-002-SERVICE-CONSOLIDATION.md`.
4. **search-service stays separate.** It's the only service backed by
   Elasticsearch instead of Postgres. Merging it into another service would
   mean that service straddling two totally different datastores and
   operational profiles (index management, different scaling
   characteristics) for no cohesion benefit.
5. **Which service "keeps its name/port" within a merge group** was chosen
   by whichever constituent was the largest/most complex (e.g. tour-service
   within catalog-service, ride-service within mobility-service) --
   arbitrary in the sense that any constituent could have been the
   directory that survives, but consistent within this doc and the code's
   git history (that directory's history is what the merge is built on top
   of, via `git mv`).

## Database-per-service

Every service above (except search-service, which is Elasticsearch-only)
owns its own PostgreSQL schema via `?currentSchema=<name>_service` on one
shared `tour_planner` database (dev-only simplification -- nothing stops
separating these into distinct database instances in production). No
service queries another service's tables. Cross-service reads are either a
synchronous REST call with the caller's JWT forwarded, or -- where the
2026-09-06 consolidation put both sides in the same service -- a direct
in-process call. See `DATABASE-DESIGN.md`.
