# TourFlow deployment runbook — Render (backend) + Vercel (frontend)

Free-tier stack: **Render** (10 web services) · **Vercel** (SPA) · **Neon**
(Postgres) · **Elastic Cloud** (search, Phase 2) · Kafka *deferred to Phase 2*.

## 0. Architecture and the constraints behind it

Render's free plan: services **sleep after 15 min idle** (cold start), share a
**750 instance-hours/month pool**, get **512 MB RAM / 0.1 vCPU each**, and
**cannot receive private-network traffic** (they can only call out).

Consequences, all measured or confirmed rather than assumed:

- **One service per JVM.** gateway + identity + catalog + booking + payment
  measured ~1.2 GB together (Windows working set; Linux is leaner but nowhere
  near 512 MB), so they cannot share one free instance. Each service alone is
  ~170–280 MB and fits.
- **Services talk to each other over public `onrender.com` URLs.** The gateway's
  route targets and every inter-service call (booking→catalog, payment→booking,
  engagement→booking/catalog, mobility→booking/catalog, search→catalog) are
  env vars (`*_SERVICE_URL`), defaulting to localhost so local dev is unchanged.
  The gateway rewrites `Host` to the target's own hostname (verified), so Render
  routes each hop to the right service.
- **Cold starts are handled by waking everything in parallel**, not by keeping
  everything warm (the hour pool can't afford that) — see §7.
- **Neon must be the DIRECT endpoint** (no `-pooler`), and each service sets its
  own `search_path` (§2).

| Piece | Where |
|---|---|
| gateway + 9 domain services | 10 Render free web services (`tourflow-<name>`) |
| Postgres | Neon (direct endpoint), one DB, one schema per service |
| Search | Phase 1: catalog fallback (`SEARCH_BACKEND=catalog`); Phase 2: Elastic Cloud |
| Kafka | Phase 2 (Upstash Kafka was discontinued Mar 2025; see §8) |
| Redis | not provisioned — no service uses it |
| Frontend | Vercel (static SPA; talks only to the gateway) |

## 1. Prerequisites

- [ ] Latest code **pushed to GitHub** (Render clones GitHub, not your disk).
- [ ] Accounts: Render, Vercel, Neon, (Elastic Cloud later), Razorpay test keys.
- [ ] Rotate any credential that was pasted into chats/tickets.
- [ ] Service names in `render.yaml` are `tourflow-gateway`, `-identity`,
      `-catalog`, `-booking`, `-payment`, `-mobility`, `-engagement`,
      `-platform`, `-insights`, `-search`. Their URLs must be
      `https://<name>.onrender.com`; if Render appends a suffix to any name
      (name already taken), fix that service's `*_SERVICE_URL` in the group (§4).

## 2. Neon (Postgres)

1. Neon project in `ap-southeast-1` (Singapore, same as Render's region).
2. Use the **DIRECT host — no `-pooler` in the name.** The pooled endpoint is
   PgBouncer in transaction mode and breaks session state (`SET search_path`,
   Flyway's advisory lock).
3. Values: `DB_HOST`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` (§4). Constants
   already in the group: `DB_PORT=5432`, `DB_SSLMODE=require`,
   `DB_CHANNEL_BINDING=require` (JDBC's spelling of Neon's `channel_binding`).
4. **Neon ignores the `currentSchema` JDBC parameter**, so every service sets
   `hikari.connection-init-sql: SET search_path TO <its schema>`; without it
   Flyway creates tables in the service's schema but Hibernate looks in
   `public` ("missing table [...]"). Don't remove that line.
5. Flyway creates each schema on first boot. Hikari pools are capped at 3 per
   service to stay under Neon's direct-endpoint connection limit.
6. Neon free scales to zero after ~5 min idle; the first query afterwards takes
   a few seconds.

## 3. Elastic Cloud (Phase 2 only)

Serverless project → endpoint + API key. Used only after Kafka works and you set
`SEARCH_BACKEND=elasticsearch` (§8). Notes: serverless rejects Spring Data's
default shard/replica settings (`ToursIndexInitializer` creates the index with
none) and returns HTTP 410 for `_cluster/health` (so
`MANAGEMENT_HEALTH_ELASTICSEARCH_ENABLED=false` is set). Env vars then:
`ELASTICSEARCH_URIS`, `SPRING_ELASTICSEARCH_APIKEY` (on `tourflow-search`).

## 4. Render — deploy the Blueprint

1. Dashboard → **New → Blueprint** → connect the repo → branch `main`. It lists
   10 services and the `tourflow-shared` env group.
2. Prompted values (service-level `sync: false`): only **tourflow-payment**:
   `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET` (any long
   random string for now; must match the Razorpay webhook in §6).
3. **Immediately add the secrets to the shared group.** Render ignores
   `sync: false`/`generateValue` inside env groups, so `render.yaml` only holds
   constants and URLs. **Env Groups → tourflow-shared → Edit**, add:

| Key | Value |
|---|---|
| `DB_HOST` | Neon **direct** host |
| `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | from Neon |
| `JWT_SECRET` | one value for all services: `node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"` |
| `CORS_ALLOWED_ORIGINS` | placeholder now (`https://placeholder.vercel.app`); real Vercel URL in §6 |

   The group already contains the constants and the `*_SERVICE_URL` values
   (`IDENTITY_SERVICE_URL`, `CATALOG_SERVICE_URL`, …, plus `BUSINESS_SERVICE_URL`
   and `TOUR_SERVICE_URL`, both = the catalog URL). Verify each matches the real
   URL Render shows on that service's page.
4. Builds take a few minutes each. **Migrating from the old 6-service layout:**
   delete `tourflow-core` (it can't fit in 512 MB), keep the existing
   mobility/engagement/platform/insights/search services, then **Manual Sync**
   the Blueprint to create `tourflow-gateway/-identity/-catalog/-booking/-payment`.
5. Check each service's Logs: `Started …Application`, and for DB services
   `Successfully applied N migrations to schema "<x>_service"`.

## 5. Verify the backend

`$GW` = `https://tourflow-gateway.onrender.com`. The first request after idle can
take 1–2 min per sleeping service (§7).

```bash
curl -s $GW/actuator/health
curl -s -X POST $GW/api/auth/register -H "Content-Type: application/json" \
  -d '{"email":"smoke@example.com","password":"Password123!","firstName":"Smoke","lastName":"Test","role":"BUSINESS"}'
curl -s -X POST $GW/api/tours -H "Authorization: Bearer $JWT" -H "Content-Type: application/json" \
  -d '{"title":"Smoke","destination":"Goa","startDate":"2027-01-10","endDate":"2027-01-14","price":5000,"maxParticipants":5}'
curl -s "$GW/api/search/tours?destination=Goa" -H "Authorization: Bearer $JWT"
```
Then confirm in Neon's SQL editor that each service created its schema.

## 6. Vercel (frontend)

1. **Add New → Project** → import the repo → **Root Directory `frontend`** →
   Vite preset (`npm run build`, output `dist`).
2. Environment variables (inlined at build time — redeploy after changing):
   - `VITE_API_BASE_URL` = `https://tourflow-gateway.onrender.com` (no trailing slash)
   - `VITE_WARMUP_URLS` = comma-separated public URLs of all services, e.g.
     `https://tourflow-gateway.onrender.com,https://tourflow-identity.onrender.com,https://tourflow-catalog.onrender.com,https://tourflow-booking.onrender.com,https://tourflow-payment.onrender.com,https://tourflow-mobility.onrender.com,https://tourflow-engagement.onrender.com,https://tourflow-platform.onrender.com,https://tourflow-insights.onrender.com,https://tourflow-search.onrender.com`
3. `frontend/vercel.json` rewrites all paths to `index.html` (deep links/refresh).
4. **Close the CORS loop:** set the group's `CORS_ALLOWED_ORIGINS` to the Vercel
   production URL (scheme included, no trailing slash) — this restarts every
   service. Preview URLs are rejected unless added (comma-separated).
5. **Razorpay webhook**: URL `https://tourflow-payment.onrender.com/api/payments/webhook`,
   secret = `RAZORPAY_WEBHOOK_SECRET`.

## 7. Cold starts (the honest version)

10 services × always-on would need ~7,300 h against a 750 h pool, so nothing is
kept warm permanently. Instead:

- **Parallel warm-up:** on page load the frontend pings every service in
  parallel (`src/lib/warmUp.ts`), so a cold visit costs roughly one cold start
  (~1–2 min at 0.1 vCPU), not one per service in a chain. The API client also
  shows a "waking up the server" toast after 4 s of a pending request, and the
  gateway's proxy read-timeout is 90 s.
- **Before a demo/interview:** open the site (or `curl` each `/actuator/health`)
  a couple of minutes early.
- Optional: a free pinger (cron-job.org/UptimeRobot) on the **gateway + identity**
  only, during hours you need it. Budget: 2 always-on services ≈ 1,490 h/month —
  too much; ~10 h/day each is ≈ 620 h. Don't ping all ten.
- Exceeding 750 h suspends free services until the next month — watch usage.

## 8. Phase 2 — Kafka (search index, notifications, analytics)

Kafka carries `booking.events` (→ platform notifications, insights analytics)
and `tour.events` (→ search index). Until it exists, publishing fails fast (3 s
cap) and quietly and those features are inert. Upstash Kafka no longer exists.

| Option | Fit |
|---|---|
| Redpanda Serverless | Kafka-API drop-in, public TLS certs → works with the current SASL_SSL config as-is. Free credits, not permanently free — check terms. |
| Aiven free Kafka | Permanently free but auto-powers-off when idle, tiny limits, private-CA brokers → needs a small code addition (PEM truststore) and SASL enabled. |
| Skip Kafka | Keep `SEARCH_BACKEND=catalog`; no notifications/analytics events. |

With a cluster: create topics `tour.events`, `booking.events`; add to
`tourflow-shared`: `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SECURITY_PROTOCOL`
(`SASL_SSL`), `KAFKA_SASL_MECHANISM` (`SCRAM-SHA-256` unless told otherwise),
`KAFKA_SASL_JAAS_CONFIG`
(`org.apache.kafka.common.security.scram.ScramLoginModule required username="…" password="…";`).
Consumer groups: `platform-service`, `insights-service`, `search-service`. Then on
`tourflow-search` set `SEARCH_BACKEND=elasticsearch` and add the Elastic vars (§3).

## 9. Troubleshooting

| Symptom | Likely cause |
|---|---|
| `lstat …/backend/deploy: no such file` | code not pushed to GitHub |
| `Connection to localhost:5432 refused` | `DB_HOST`/`DB_*` not in `tourflow-shared`, or the service isn't linked to the group |
| `Schema validation: missing table [...]` | `connection-init-sql` (search_path) removed, or pooled Neon host used |
| Out of memory / restart loop on one service | check its Metrics; each service is sized for 512 MB (`-Xmx200m`) |
| Gateway returns 502/504 for a route | the target service is asleep (wait) or its `*_SERVICE_URL` in the group is wrong |
| A service calls another and fails | the caller's `*_SERVICE_URL` (booking→`CATALOG_`, payment→`BOOKING_`, engagement→`BOOKING_`/`BUSINESS_`, mobility→`BOOKING_`/`TOUR_`) |
| Browser CORS error | `CORS_ALLOWED_ORIGINS` ≠ exact Vercel origin |
| Search returns nothing | `SEARCH_BACKEND=elasticsearch` without Kafka, or `CATALOG_SERVICE_URL` wrong |
| Health DOWN on tourflow-search | `MANAGEMENT_HEALTH_ELASTICSEARCH_ENABLED` not `false` |

Rollback: Render → service → Deploys → redeploy an earlier commit; Vercel →
Deployments → Promote a previous one.

## 10. Known limitations

- **Google login is not wired through the gateway** (no `/oauth2/**` or
  `/login/oauth2/**` routes, no forwarded-header config). Email/password works.
- **File uploads are ephemeral** (platform-service writes to local disk).
- **Every hop is a public HTTPS call** between services; each can cold-start.
  Parallel warm-up mitigates the chain but a fully cold system is slow once.
- **Catalog search fallback filters in memory** — demo scale only.
- Neon/Elastic/Aiven free tiers have quotas and expiry; re-check if things stop
  working weeks later.
