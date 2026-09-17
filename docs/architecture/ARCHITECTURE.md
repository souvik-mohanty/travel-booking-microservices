# TourFlow Architecture

## Overview

TourFlow is a microservices-based travel and tour operations platform.
The backend was originally scaffolded as 25 services (one per noun in the
architecture vision doc) and, once every domain that could be built without
missing infra or an external decision actually was built, consolidated down
to 9 domain services + an API gateway (2026-09-06) -- see
`docs/decisions/ADR-002-SERVICE-CONSOLIDATION.md` for why and exactly what
merged into what. This document describes the current, real state.

```
                         Web / Mobile Clients
                                  |
                            API Gateway (:8080)
                                  |
   -------------------------------------------------------------------
   |          |          |          |          |          |          |
identity   catalog    booking    payment   mobility  engagement  platform
 :8081      :8082      :8083      :8084      :8086      :8087      :8088
   |          |                              |          |
 auth       tours                          rides      reviews
 RBAC       businesses                     drivers     support
 profiles   activities                     fleet
            hotels                         trips
            rooms                          tracking
            guides

                          insights :8089        search :8099
                          audit                 Elasticsearch-backed,
                          analytics             different datastore
```

Every service owns its own PostgreSQL schema (`currentSchema=<name>_service`
on one shared `tour_planner` database in dev; nothing stops splitting these
into separate database instances in production), except search-service
(Elasticsearch, no relational schema at all). No service reads another
service's tables directly -- all cross-service reads either go through REST
calls (JWT forwarded) or, where the calling and called domain now live in
the same merged service, a direct in-process method call.

## Service status

| Service | Port | Absorbs (2026-09-06 merge) | Status |
|---|---|---|---|
| api-gateway | 8080 | -- | Real routes for every path below (`spring.cloud.gateway.server.webmvc.routes`) |
| identity-service | 8081 | identity + authorization + user | Full (auth, JWT, OAuth2, RBAC, user profiles) |
| catalog-service | 8082 | tour + business + hotel + guide | Full (tours/legs/activities, businesses, hotels/rooms/reservations, guides) |
| booking-service | 8083 | -- (kept isolated) | Full (lifecycle: PENDING/PAID/COMPLETED/CANCELLED) |
| payment-service | 8084 | -- (kept isolated) | Full (Razorpay order/verify/webhook, payout fields) |
| mobility-service | 8086 | driver + fleet + ride + trip + tracking | Full (seat concurrency via row locking; tracking scoped to REST polling, no WebSocket/Redis push) |
| engagement-service | 8087 | review + support | Full (reviews gated on booking completion; support self-service) |
| platform-service | 8088 | notification + file | Full, scoped (notifications in-app only; files on real local disk, stand-in for S3) |
| insights-service | 8089 | audit + analytics | Full (audit append-only; analytics consumes `BookingConfirmed` off Kafka) |
| search-service | 8099 | -- (kept isolated, different datastore) | Full -- Elasticsearch-backed, consumes `TourPublished`/`TourCancelled` off Kafka |
| organization-service | -- | -- | Not built separately -- catalog-service's business sub-domain covers this |
| refund-payout-service | -- | -- | Not built separately -- payment-service's `PayoutStatus` covers this |
| ai-orchestrator-service, ai-chatbot-service | -- | -- | Dropped in the 2026-09-06 consolidation -- were still empty Phase-0 scaffolds, blocked on an LLM provider decision. Revisit once that decision is made. |

## Why two REST calls became in-process calls

Merging tour-service, business-service, and hotel-service into one
catalog-service turned two previously-real HTTP calls into direct method
calls, since caller and callee now live in the same JVM:

- Attaching an activity to a tour used to call business-service over HTTP to
  verify the activity was `ACTIVE`; it now calls `ActivityService` directly.
- Reserving/releasing a room for a tour leg used to call hotel-service over
  HTTP, forwarding the caller's JWT so hotel-service could attribute the
  reservation; it now calls `RoomReservationService` directly, passing the
  caller's ID straight through (no JWT round-trip needed for an in-process
  call).

Both keep their exact prior error contract (same exceptions, same messages,
same HTTP status codes from the outside) -- see `API-REFERENCE.md`. Every
other REST call between services (trip->tour, trip->booking, payment->
booking, review->booking, review->business) stays a real network call,
since those callers and callees ended up in different merged services.

## Kafka: unaffected by the consolidation

Producer/consumer code just relocated with whichever original service
absorbed it -- topic names and event contracts didn't change.

- `catalog-service` (formerly tour-service) still publishes `TourPublished`/
  `TourCancelled` to `tour.events`.
- `search-service` still consumes `tour.events` to index/remove a
  `TourDocument`.
- `booking-service` still publishes `BookingConfirmed` to `booking.events`.
- `platform-service` (formerly notification-service) and `insights-service`
  (formerly analytics-service) both still consume `booking.events`.

See `COMMUNICATION.md` for the full flow and the two gotchas it took to get
Kafka working in the first place.

## Core principle: AI is separate from the core platform

AI enhances the platform; it never controls critical transactional
operations (booking, payment, auth, cancellation). The core platform must
keep working if every AI component is down. If/when an AI orchestrator or
chatbot gets built: the LLM calls controlled tools/APIs into microservices,
it never touches a database directly, and the same business rules apply
regardless of whether the caller is web/mobile/API/chatbot.
