# ADR-002: Consolidate 25 Services Down to 9 + API Gateway

## Status

Accepted and implemented, 2026-09-06. Amends ADR-001 -- the one-service-per-
bounded-context principle from ADR-001 still holds, but "bounded context"
was redrawn at a coarser grain than "one per noun in the vision doc."

## Context

By 2026-09-05, every domain from the original 25-service target that could
be built without missing infra (Elasticsearch, Kafka) or an external
decision (an LLM provider for the AI services) had been built, tested, and
verified live. That left 23 real services, 2 empty AI scaffolds, and one
never-used placeholder (`finance-service`) running as 25 separate local JVM
processes, each with its own port, its own copy-pasted JWT security stack
(per ADR-001's accepted tradeoff), and its own Postgres schema.

The user asked to consolidate this down to 10 total (9 domain services +
the API gateway), trading fine-grained service-per-noun boundaries for
fewer, more cohesive deployable units.

## Decision

Group the 23 real services into 6 merge groups by business cohesion, plus 3
services kept deliberately isolated, plus the gateway:

| New service | Absorbs |
|---|---|
| identity-service | identity + authorization + user |
| catalog-service | tour + business + hotel + guide |
| mobility-service | driver + fleet + ride + trip + tracking |
| engagement-service | review + support |
| platform-service | notification + file |
| insights-service | audit + analytics |
| booking-service | (unchanged) |
| payment-service | (unchanged) |
| search-service | (unchanged) |
| api-gateway | (unchanged) |

Two services were deliberately **not** merged with anything despite being
in the "commerce" neighborhood, for reasons that predate this ADR:

- **booking-service and payment-service stay separate from each other and
  from everything else.** This is an explicit rule in the project's own
  architecture vision doc ("Payment processing must be isolated from the
  Booking Service"), not a coincidence of the merge groups above.
- **search-service stays separate.** It's the only service backed by
  Elasticsearch instead of Postgres -- merging it into another service
  would mean that service straddling two datastores with different
  operational profiles for no cohesion benefit.

`finance-service` (a directory with zero files -- never scaffolded) and the
two AI scaffolds (`ai-orchestrator-service`, `ai-chatbot-service` -- Phase-0
only, no domain code, blocked on an LLM provider decision) were deleted
outright rather than merged into anything, since there was no logic to
preserve.

### Mechanics applied uniformly to every merge group

- **One package per merged service**, named after it (e.g.
  `com.tourflow.catalog`), with each absorbed domain as a sub-package
  (`com.tourflow.catalog.business`, `.hotel`, `.guide`) -- the constituent
  whose directory survived (chosen as whichever was largest/most complex)
  keeps its code at the base package level.
- **One security stack per merged service** -- one `SecurityConfig`, one
  `JwtService`, one `JwtAuthenticationFilter`, collapsed from N near-
  identical copies (the ADR-001-accepted per-service duplication). Every
  constituent's specific rules (role gates, CORS beans, order-sensitive
  matchers) were preserved verbatim in the union.
- **One `GlobalExceptionHandler` per merged service**, same union
  treatment. Where two constituents both had a generic catch-all for the
  same standard Spring exception (`IllegalArgumentException`,
  `MethodArgumentNotValidException`), only one survives -- this is the one
  deliberate, minor response-*shape* change from this ADR: a few endpoints'
  generic validation-error bodies now come back in a different (but still
  correct, still documented) JSON shape than before. No endpoint's success
  path, status code, or business logic changed.
- **One Postgres schema per merged service**, continuing the
  `?currentSchema=<name>_service` convention from `DATABASE-DESIGN.md`.
  Flyway migrations from each constituent were concatenated into one
  `db/migration` folder and renumbered into a single continuous sequence
  (safe -- confirmed zero table-name collisions across any two services
  before merging). This is the one exception: identity-service used to be
  the sole service left on Postgres's default `public` schema; it now uses
  `identity_service` like everything else, fixing a pre-existing
  inconsistency as a side effect.
- **Two REST calls became in-process calls**, purely as a consequence of
  where the merge boundaries fell, not a design goal of the ADR: within
  catalog-service, tour's calls to business (activity verification) and to
  hotel (room reservation) now call the target service's own Spring bean
  directly instead of going over HTTP. Every other cross-service REST call
  in the system (trip->tour, trip->booking, payment->booking, review->
  booking, review->business) stays a real network call, since those
  callers and callees ended up in different merged services. See
  `ARCHITECTURE.md` for the full list.
- **Kafka topics and contracts are unaffected** -- producer/consumer code
  just relocated with whichever service absorbed it.

## Consequences

**What this bought:**
- 10 running processes in local dev instead of 25 -- one Postgres pool
  budget, one port per domain to remember, one `.vscode/tasks.json` build/
  run pair per domain.
- Fewer copies of the JWT security trio to keep in sync (6 collapsed sets
  instead of 23 individual ones).
- Two fewer network hops on the hot path of "attach an activity to a tour"
  and "add a tour leg with a hotel room" -- both were same-transaction-
  adjacent operations that used to cross a network boundary for no reason
  once tour/business/hotel share a JVM.
- The identity-service schema inconsistency (the one service on `public`
  instead of its own schema) got fixed as a natural side effect of the
  merge, not a separate cleanup task.

**Accepted costs:**
- The minor exception-handler response-shape unification noted above: a
  handful of endpoints' generic validation-error JSON bodies changed shape
  (still 400/404/403 as appropriate, still contain the same message text,
  just nested differently). No client of this API exists yet to be broken
  by this.
- Six services now have a coarser internal package structure
  (sub-packages instead of top-level `controller`/`service`/etc.) --
  intentional, to keep each absorbed domain visually distinct within its
  merged service.
- This ADR does not undo ADR-001's core per-service-schema/independent-
  deployability principle -- it just redraws which bounded contexts get
  their own service. A future team could still deploy, say, catalog-service
  independently of identity-service; they just can't deploy tour-service
  independently of business-service anymore, because they're no longer
  separate deployables.

## Related

- `ADR-001-MICROSERVICES.md` -- the principle this amends (bounded contexts
  get their own service; this ADR redraws the boundaries, doesn't reverse
  the principle)
- `SERVICE-BOUNDARIES.md` -- current per-service ownership map, including
  which original service absorbed which
- `ARCHITECTURE.md` -- current topology diagram and the in-process-call
  detail
- `API-REFERENCE.md` -- confirms no path/request/response/auth-rule changed,
  only which port answers each path
