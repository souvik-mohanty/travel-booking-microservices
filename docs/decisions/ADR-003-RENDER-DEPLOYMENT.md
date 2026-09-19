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

**Split by traffic criticality, not by ADR-002's domain boundaries:**

- Bundle the 5 services on the user's hot path (gateway, identity, catalog,
  booking, payment) into **one** Docker image / Render service
  (`tourflow-core`, `backend/deploy/core`), running all 5 JVMs internally and
  exposing only the gateway's port externally. One cold start instead of
  five. This is the one service worth keeping warm via external pinging,
  since it fits the shared hour budget on its own (~730 of 750 hours/month)
  and covers auth/browse/book/pay.
- Deploy the other 5 (mobility, engagement, platform, insights, search) as
  independent free Render services. Lower-traffic features; an occasional
  cold start on these is an acceptable tradeoff, not a defect.
- Move Postgres (Neon, **direct** endpoint -- the pooled PgBouncer endpoint
  breaks `SET search_path`/`currentSchema` and Flyway's advisory lock) and
  Kafka (deferred: Upstash Kafka was discontinued in 2025; see the deployment
  README's Phase 2) to external managed services
  reachable by every service as a normal outbound call — sidesteps the free
  private-networking restriction entirely, since it only blocks
  Render-to-Render inbound traffic, not calls to an external host. Redis is
  not provisioned; no service uses it. Elasticsearch moves to Elastic Cloud
  (serverless, API-key auth); the free trial is time-limited, so
  `search-service` also has an opt-in `SEARCH_BACKEND=catalog` mode
  (`CatalogFallbackTourSearchService`) that queries `tourflow-core`'s
  `/api/tours` directly and filters in memory. Elastic Cloud serverless
  required two code changes: the `tours` index is created explicitly with no
  shard/replica settings (`ToursIndexInitializer`), and Spring's ES health
  indicator is disabled in the deployed service (serverless returns 410 for
  `_cluster/health`).
- The gateway's HTTP client read-timeout is raised to 90s
  (`spring.cloud.gateway.httpclient.read-timeout`) so it doesn't 504 while a
  downstream free-tier service wakes up. The frontend's API client detects a
  slow-in-flight request (>4s) and shows a "waking up the server" toast
  instead of looking hung.

## Consequences

- This deployment topology (1 bundled + 5 standalone) is **specific to the
  free-tier deployment** and doesn't change local dev, which still runs all
  10 services independently against local Postgres/Kafka/Elasticsearch (see
  `backend/docker-compose.yml`) exactly as ADR-002 left it.
- `tourflow-core`'s RAM budget (512MB/0.1vCPU shared across 5 JVMs) is
  genuinely tight and was sized by estimate, not measured against a real
  Render instance; see `backend/deploy/README.md`'s known-limitations section.
- Elasticsearch-backed search is not available in this deployment. Restoring
  it would mean either a paid ES host or moving `tourflow-core` to a paid
  Render tier that can accept inbound private-network traffic from a
  standalone Elasticsearch-hosting service.
