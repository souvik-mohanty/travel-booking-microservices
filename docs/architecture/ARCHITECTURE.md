# TourFlow Architecture

## Overview

TourFlow is a microservices-based travel and tour operations platform. The
full target architecture is 25 services across 6 domains (Identity/Platform,
Travel/Commerce, Operations, Platform Services, Intelligence). This document
describes what's actually built as of 2026-09-02, not just the target.

```
                         Web / Mobile Clients
                                  |
                            API Gateway (:8080)
                                  |
       -----------------------------------------------------------
       |                    |                    |                |
  Identity Domain      Travel/Commerce      Operations        Platform
       |                    |                    |                |
  identity :8081        tour :8082          driver :8091      notification :8097
  user :8088             booking :8083        fleet :8092      support :8098
  authorization :8089    business :8086       guide :8093      audit :8101
                          hotel :8096          trip :8094       file :8102
                          payment :8084        tracking :8095   review :8087
                          ride :8090
```

Every service owns its own PostgreSQL schema (`currentSchema=<name>_service`
on one shared `tour_planner` database in dev; nothing stops splitting these
into separate database instances in production). No service reads another
service's tables directly -- all cross-service reads go through REST calls.

## Service status

| Service | Port | Status |
|---|---|---|
| api-gateway | 8080 | Routing only |
| identity-service | 8081 | Full (auth, JWT, OAuth2) |
| tour-service | 8082 | Full |
| booking-service | 8083 | Full (lifecycle: PENDING/PAID/COMPLETED/CANCELLED) |
| payment-service | 8084 | Full (Razorpay order/verify/webhook, payout fields) |
| business-service | 8086 | Full (Business + Activity) |
| review-service | 8087 | Full (gated on booking completion) |
| user-service | 8088 | Full |
| authorization-service | 8089 | Full (RBAC: Role/Permission/UserRole) |
| ride-service | 8090 | Full (seat concurrency via row locking) |
| driver-service | 8091 | Full |
| fleet-service | 8092 | Full |
| guide-service | 8093 | Full |
| trip-service | 8094 | Full |
| tracking-service | 8095 | Full, scoped (REST polling, no WebSocket/Redis push) |
| hotel-service | 8096 | Full (Hotel + Room) |
| notification-service | 8097 | Full, scoped (in-app only, no email/SMS/push; consumes `BookingConfirmed` from Kafka -- see `COMMUNICATION.md`) |
| support-service | 8098 | Full |
| audit-service | 8101 | Full (append-only) |
| file-service | 8102 | Full (real local-disk storage) |
| search-service | 8099 | Scaffold only -- needs Elasticsearch |
| analytics-service | 8100 | Scaffold only -- Kafka now works (see below), still needs its own consumer(s) built |
| ai-orchestrator-service | 8103 | Scaffold only -- needs an LLM provider decision |
| ai-chatbot-service | 8104 | Scaffold only -- needs an LLM provider decision |
| organization-service | -- | Not built separately -- business-service covers this domain |
| refund-payout-service | -- | Not built separately -- payment-service's PayoutStatus covers this |

"Scaffold only" means: pom.xml, application.yml, JWT security trio
(JwtService/JwtAuthenticationFilter/SecurityConfig), package structure. No
entities, no APIs, no migrations.

## Why Search/Analytics aren't built as REST-polling substitutes

The intended design has Search and Analytics as Kafka event consumers
(`TourPublished -> Kafka -> Search Service -> Elasticsearch`). Building them
as services that synchronously call every other service's API to aggregate
data on demand would mean building the *wrong* architecture -- not a scoped
step toward the real one.

As of 2026-09-02, Kafka itself is live and working -- see
`COMMUNICATION.md` for the real `booking-service -> booking.events ->
notification-service` flow and the two gotchas it took to get there. Search
and analytics are no longer blocked on Kafka *existing*; they're blocked on
their own consumer/aggregation design not having been built yet (a `TourPublished`
producer in tour-service, an Elasticsearch index in search-service, an
aggregation model in analytics-service) -- a real scoping task, not an
infra gap.

Tracking and Notification, by contrast, were legitimately scoped down: their
core data model (a location point; an in-app message) doesn't require the
missing infra (WebSocket+Redis push; a real email/SMS/push provider) to be
real and useful. Only the *delivery mechanism* was deferred.

## Core principle: AI is separate from the core platform

AI enhances the platform; it never controls critical transactional
operations (booking, payment, auth, cancellation). The core platform must
keep working if every AI component is down. When AI Orchestrator/Chatbot are
eventually built: the LLM calls controlled tools/APIs into microservices, it
never touches a database directly, and the same business rules apply
regardless of whether the caller is web/mobile/API/chatbot.
