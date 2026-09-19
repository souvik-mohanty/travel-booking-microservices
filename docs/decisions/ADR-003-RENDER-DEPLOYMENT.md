# ADR-003: Free-Tier Render Deployment Topology

## Status

Accepted and implemented, 2026-09-18.

## Context

The user wants to deploy the backend to Render's free plan and the frontend
to Vercel, without paying for hosting. Render's free plan has two properties
that matter here:

1. A free web service spins down after 15 minutes idle and takes up to ~a
   minute to cold-start on the next request. With 10 separately-deployed
   services (9 domain services + gateway from ADR-002), a request chaining
   through several sleeping services synchronously could stack multiple cold
   starts into several minutes of latency.
2. **Free services can only initiate private-network connections, they
   cannot receive them** (confirmed against Render's own docs). This rules
   out the naive fix of running Postgres/Kafka/Elasticsearch inside one free
   service's container and having the other 9 reach into it.
3. The 750 free instance-hours/month pool is shared across every free
   service in the workspace, not granted per-service — so keeping all 10
   services always-on via external keep-alive pinging would need ~10x the
   available budget.

## Decision

**Ten independent free services, no bundle.** Deploy the gateway and each of the
9 domain services as its own Render free web service. An earlier design bundled
gateway+identity+catalog+booking+payment into one image; measuring them
(~1.2 GB of working set for the five JVMs) showed they cannot fit the free
plan 512 MB, and the bundle also left the 5 standalone services unreachable
because every gateway route and inter-service URL was hardcoded to localhost.

- Gateway route targets and every inter-service URL are environment variables
  (`IDENTITY_SERVICE_URL`, `CATALOG_SERVICE_URL`, ...) defaulting to localhost, so
  local dev is unchanged. In Render they are the services public
  `onrender.com` URLs (free services cannot receive private-network traffic).
  The gateway rewrites Host to the target hostname, verified locally.
- **Cold starts:** nothing is kept permanently warm (10 services always-on
  would need ~7,300 h against the 750 h shared pool). The frontend pings every
  service in parallel on page load so they wake together rather than in a
  chain; the API client shows a waking-up toast after 4 s; the gateway proxy
  read-timeout is 90 s.
- **Postgres:** Neon, DIRECT endpoint (the pooled PgBouncer endpoint breaks
  session state), with `SET search_path` set per connection via Hikari
  `connection-init-sql` because Neon ignores the `currentSchema` JDBC parameter.
- **Kafka:** deferred (Upstash Kafka was discontinued in 2025). Producers give
  up after 3 s so a missing broker does not stall requests.
- **Search:** Elasticsearch on Elastic Cloud once Kafka exists; until then
  `SEARCH_BACKEND=catalog` filters catalog-service tour list in memory.
  Serverless required creating the index explicitly with no shard/replica
  settings (`ToursIndexInitializer`) and disabling the ES health indicator (410).
- Redis is not provisioned; no service uses it.

## Consequences

- This topology is specific to the free-tier deployment; local dev is
  unchanged and still runs all 10 services against local Postgres/Kafka/
  Elasticsearch (see `backend/docker-compose.yml`), as ADR-002 left it.
- Every service-to-service call is a public HTTPS hop that can hit a sleeping
  service. Parallel warm-up bounds the damage but a fully cold system still
  takes 1-2 minutes to wake once.
- The 750 h pool is shared: watch usage, and do not keep more than a couple of
  services pinged.
- Kafka-driven features (search index, notifications, analytics events) are
  inert until a Kafka provider is chosen (backend/deploy/README.md, section 8).
