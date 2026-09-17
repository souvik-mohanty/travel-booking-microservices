# Service Communication

## What's actually in use today: synchronous REST

Every cross-service call in this codebase is a synchronous HTTP request via
Spring's `RestClient`, built with `RestClient.builder().baseUrl(...).build()`
directly (not an injected autoconfigured `RestClient.Builder` bean -- Spring
Boot doesn't autoconfigure one in this project's setup; every client that
tried injecting one failed to start until switched to the static factory
call). Each caller has its own small `client/XClient.java` component rather
than a shared library, matching the "each service copies what it needs and
evolves independently" pattern used for the JWT security trio too.

The caller's JWT is forwarded on every such call
(`.header("Authorization", "Bearer " + token)`), taken from
`authentication.getCredentials()` in the controller and threaded through the
service layer. Examples: `booking-service -> tour-service` (fetch the real
tour price), `payment-service -> booking-service` (verify a booking before
charging it), `review-service -> booking-service` + `review-service ->
business-service` (verify a booking is complete, verify an activity belongs
to a business), `trip-service -> booking-service` (verify a booking exists
before scheduling a trip).

When there's no user context (a webhook, a background job), the caller
mints its own short-lived `role: SERVICE` JWT instead -- see `SECURITY.md`.

## Kafka: two producers, three consumers (2026-09-02)

Kafka is live and in real use, not just running unused in `docker-compose`.
Two producers exist: `booking-service` publishes `BookingConfirmed` to
`booking.events` when `markBookingPaid` succeeds, and `tour-service`
publishes `TourPublished`/`TourCancelled` to `tour.events` from its
`publish`/`cancel` endpoints. Three consumers exist: `notification-service`
(`BookingConfirmed` -> in-app notification), `analytics-service`
(`BookingConfirmed` -> append-only, `eventId`-deduped booking analytics
log), and `search-service` (`TourPublished`/`TourCancelled` -> Elasticsearch
index). See `events/EVENT-CATALOG.md` and `events/EVENT-SCHEMAS.md` for the
envelope and what else is still just planned.

The synchronous `mark-paid` REST call from payment-service stays as the
source of truth for booking state (see `ADR-001-MICROSERVICES.md`) -- the
Kafka publish is a supplementary, best-effort side channel. Same for
tour-service's publish/cancel: the Postgres write is the source of truth, the
Kafka publish is a best-effort side channel to search-service. Every
publisher in this codebase catches and logs a publish failure rather than
throwing, so a Kafka outage can't break the request that triggered it.

**Host-vs-container gotcha (the reason this didn't work on the first try):**
every service in this project runs as a host JVM process, not inside Docker,
same as how they all reach Postgres via `localhost:5432` rather than the
`postgres` container's network alias. Kafka's `docker-compose.yml` entry
originally advertised only `kafka:9092`, which a host process can resolve
for the *initial* bootstrap connection (the container's port is published to
the host) but not for the actual broker address Kafka hands back in its
metadata response -- so producers/consumers could connect once, then spin
forever failing to reach the real partition leader. Fixed by giving Kafka a
second, `HOST`-named listener advertised as `localhost:9094` (see
`docker-compose.yml`) alongside the original `PLAINTEXT` listener on `9092`,
which `kafka-ui` (itself a container on `tourflow-network`) still uses. Every
service's `spring.kafka.bootstrap-servers` points at `9094`.

**`@EnableKafka` gotcha:** Spring Boot's Kafka autoconfiguration is supposed
to register `@KafkaListener` processing automatically once `spring-kafka` is
on the classpath, without needing `@EnableKafka` explicitly. In practice,
once this project defined its own `ConsumerFactory`/
`ConcurrentKafkaListenerContainerFactory` beans (done deliberately -- see
below), the listener silently never started: no error, no log output, the
consumer group simply never appeared. Adding `@EnableKafka` on the
`@Configuration` class fixed it immediately. If a future `@KafkaListener`
method seems to just never fire and nothing is logged, check this first.

**Why plain JSON strings instead of `spring-kafka`'s `JsonSerializer`/
`JsonDeserializer`:** those are built around the classic
`com.fasterxml.jackson.databind.ObjectMapper`, not the `tools.jackson`
package this project's Spring Boot 4.1/Jackson 3 stack actually uses (see
`SECURITY.md`'s Jackson 3 gotcha). Producer and consumer both use plain
`StringSerializer`/`StringDeserializer` and hand-serialize the event envelope
with the project's own `tools.jackson.databind.ObjectMapper`, matching how
`review-service`'s `BookingClient`/`BusinessClient` already parse responses
as `JsonNode` trees rather than typed DTOs.

## Elasticsearch client/server version gotcha

`search-service` is the first (and so far only) consumer of Elasticsearch in
this codebase. Spring Boot 4.1.1's BOM resolves
`spring-boot-starter-data-elasticsearch` to a `co.elastic.clients:
elasticsearch-java` 9.x client. Pointed at the Elasticsearch **8.19.6**
`docker-compose.yml` originally ran, every request failed at startup
(`SimpleElasticsearchRepository`'s `indices.exists` check throwing
`status: 400, [es/indices.exists] Expecting a response body, but none was
sent`) -- Elastic doesn't support a client newer than the server by a full
major version. Fixed by bumping both the `elasticsearch` and `kibana`
services in `docker-compose.yml` to `9.4.6`, matching the client. If a
future Spring Boot upgrade moves the managed client to a new major again,
the server (and Kibana, which must track Elasticsearch's major.minor) needs
bumping to match -- don't assume the existing container versions still work.

## gRPC, WebSocket: still not implemented

- **gRPC** for selected latency-sensitive internal calls, where justified --
  none of the current REST calls have been identified as needing it.
- **WebSocket** for live trip tracking push to the tourist's app.
  tracking-service exists today as a REST-polling substitute (clients poll
  `GET /api/tracking/{tripId}/locations/latest`) -- the data model is real,
  the real-time delivery mechanism is what's deferred. The now-working
  `TripLocationUpdated`-via-Kafka path described in `EVENT-CATALOG.md` is
  what would eventually replace the polling, once a consumer for it exists.

## Rule of thumb

> REST/gRPC -> "I need an answer now."
> Kafka -> "Something happened."

`booking-service -> booking.events` and `tour-service -> tour.events` are
the real examples of the second case. Every other cross-service interaction
in the codebase is still the first case (see above) -- new ones should be
designed with this split in mind, but don't default to Kafka just because it
now works for these two flows; most calls in this codebase (verify a
booking, fetch a price) are legitimately "I need an answer now" and belong
on REST.
