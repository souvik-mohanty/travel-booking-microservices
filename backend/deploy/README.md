# TourFlow deployment runbook — Render (backend) + Vercel (frontend)

Free-tier stack: **Render** (6 web services) · **Vercel** (SPA) · **Neon**
(Postgres) · **Elastic Cloud** (search) · Kafka *deferred to Phase 2*.

## 0. Architecture and why it looks like this

Render's free plan spins a service down after 15 min idle (cold start), has a
**shared 750 instance-hours/month pool** across all free services, gives each
instance **512 MB RAM / 0.1 vCPU**, and — critically — **free services can
send private-network connections but not receive them**, so data stores can't
live inside one Render service for the others to reach. Hence:

| Piece | Where | Notes |
|---|---|---|
| gateway + identity + catalog + booking + payment | **tourflow-core** (1 Docker image, 5 JVMs) | one cold start, only the gateway port is exposed |
| mobility, engagement, platform, insights, search | 5 standalone Render services | sleep independently, cold-start on demand |
| Postgres | **Neon** (direct endpoint) | one DB, one schema per service — same as local |
| Search | **Elastic Cloud serverless** | API-key auth |
| Kafka | **Phase 2** | Upstash Kafka was discontinued (Mar 2025); see §8 |
| Redis | not provisioned | no service uses it |
| Frontend | **Vercel** | static SPA, talks only to the gateway |

The browser only ever talks to **tourflow-core's gateway** — never to a
service directly (two services have no CORS config at all).

## 1. Prerequisites

- [ ] Latest code **pushed to GitHub** (`git push`) — Render clones the commit
      on GitHub, not your working tree. `backend/deploy/` and `render.yaml`
      must be visible on github.com.
- [ ] Accounts: Render, Vercel, Neon, Elastic Cloud, (Razorpay test keys).
- [ ] **Rotate any credential that has been pasted into chats/tickets** (Neon
      password, Elastic API key, Razorpay secret) before going live, then use
      the rotated values below.
- [ ] Decide the names now — Render URLs derive from them:
      `tourflow-core` → `https://tourflow-core.onrender.com` (Render appends a
      suffix if the name is taken; always copy the real URL from the dashboard).

## 2. Neon (Postgres)

1. Neon → project (region `ap-southeast-1` Singapore, matching Render's).
2. **Use the DIRECT host — the one WITHOUT `-pooler` in its name.**
   The pooled endpoint is PgBouncer in transaction mode; it breaks the
   `SET search_path` behind each service's `currentSchema` and Flyway's
   session advisory lock (schemas/tables can land in the wrong place or
   migrations fail silently).
3. Note host, database, user, password → `DB_HOST`, `DB_NAME`, `DB_USERNAME`,
   `DB_PASSWORD`. Keep `DB_PORT=5432`, `DB_SSLMODE=require`,
   `DB_CHANNEL_BINDING=require` (JDBC's spelling of Neon's `channel_binding`).
4. Nothing to create by hand: each service's Flyway creates its own schema on
   first boot. Hikari pools are capped at 3 per service
   (`SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE`) to stay under the direct
   endpoint's connection limit (9 JVMs × default 10 would not fit).
5. Neon free scales to zero after ~5 min idle; the first query after that
   takes a few seconds. Expected, not a bug.

## 3. Elastic Cloud (search)

1. Create a serverless Elasticsearch project → copy the endpoint URL
   (`https://….es.<region>.gcp.elastic.cloud:443`) and create an API key
   (use the base64 **encoded** value).
2. Nothing to create by hand — `tourflow-search` creates the `tours` index
   itself on first boot (serverless rejects Spring Data's default shard/replica
   settings, so `ToursIndexInitializer` creates it with none).
3. Used only once you switch `SEARCH_BACKEND=elasticsearch` (§8). Serverless
   also doesn't support the health API Spring's ES health indicator calls
   (HTTP 410), so `MANAGEMENT_HEALTH_ELASTICSEARCH_ENABLED=false` is set.

## 4. Render — deploy the Blueprint

1. Render dashboard → **New → Blueprint** → connect the GitHub repo → branch
   `main` → it detects the root `render.yaml` and lists 6 services + the
   `tourflow-shared` env group.
2. Fill in the prompted (`sync: false`) values, service-level only:

| Service | Prompted keys |
|---|---|
| tourflow-core | `RAZORPAY_KEY_ID`, `RAZORPAY_KEY_SECRET`, `RAZORPAY_WEBHOOK_SECRET` (any long random string for now; must match the Razorpay webhook in §6) |
| tourflow-search | `CATALOG_SERVICE_URL` = tourflow-core's public URL (`https://tourflow-core.onrender.com` if that name was free; otherwise a placeholder now and the real URL after §5) |
| others | none |

   Never set `PORT` — Render injects it.
3. **Immediately after applying, add the secrets to the shared env group.**
   Render ignores `sync: false` and `generateValue` inside env groups, so
   `render.yaml` can only hold the constants (`DB_PORT`, `DB_SSLMODE`,
   `DB_CHANNEL_BINDING`, Hikari caps). Dashboard → **Env Groups →
   tourflow-shared → Edit** → add:

| Key | Value |
|---|---|
| `DB_HOST` | Neon **direct** host (no `-pooler`) |
| `DB_NAME` / `DB_USERNAME` / `DB_PASSWORD` | from Neon |
| `JWT_SECRET` | one random value shared by all 6 services: `node -e "console.log(require('crypto').randomBytes(64).toString('base64'))"` |
| `CORS_ALLOWED_ORIGINS` | placeholder now (`https://placeholder.vercel.app`); real Vercel URL in §6 |

   Render keeps variables added in the dashboard even though they aren't in
   the file. The first builds take 5–15 min, so add these before the first
   boot to avoid crash-loops on missing DB settings (saving a group change
   restarts every linked service).
4. Build order/time: the first `tourflow-core` build runs 5 Maven builds in
   one Docker build (expect ~10–15 min); the standalone ones are faster.
5. **Decision gate — watch tourflow-core's first boot** (Logs + Metrics):
   - all 4 sub-services log `[core] … is healthy` and the gateway starts;
   - Render marks the deploy live (the gateway binds its port *last*, after the
     other 4 services are healthy — on 0.1 vCPU that can take several minutes);
   - memory stays under the 512 MB limit (no OOM-kill / restart loop).
   If it fails or is too slow, options in order: (a) start the gateway first
   in `entrypoint.sh` so the port opens early; (b) move `tourflow-core` to
   Render's cheapest **paid** instance (more CPU/RAM and no spin-down — check
   current specs/pricing); (c) split core into two services. The heap sizes in
   `deploy/core/entrypoint.sh` were estimated, not measured.

## 5. Verify the backend (before touching the frontend)

Replace `$CORE` with tourflow-core's URL. First request after idle can take
minutes (core cold start) — that's the thing §7 mitigates.

```bash
curl -s $CORE/actuator/health                       # {"status":"UP"...}
# register + login
curl -s -X POST $CORE/api/auth/register -H "Content-Type: application/json" \
  -d '{"email":"smoke@example.com","password":"Password123!","firstName":"Smoke","lastName":"Test","role":"BUSINESS"}'
# use the returned accessToken:
curl -s -X POST $CORE/api/tours -H "Authorization: Bearer $JWT" -H "Content-Type: application/json" \
  -d '{"title":"Smoke","destination":"Goa","startDate":"2027-01-10","endDate":"2027-01-14","price":5000,"maxParticipants":5}'
curl -s "$CORE/api/search/tours?destination=Goa" -H "Authorization: Bearer $JWT"   # via search (cold start)
```
Then confirm in Neon's SQL editor that each service created its own schema
(`identity_service`, `catalog_service`, `booking_service`, `payment_service`, …).

Set `CATALOG_SERVICE_URL` on tourflow-search now and redeploy it.

## 6. Vercel (frontend)

1. Vercel → **Add New → Project** → import the repo →
   **Root Directory: `frontend`** → Framework Preset **Vite**
   (build `npm run build`, output `dist` — auto-detected).
2. Environment variable: `VITE_API_BASE_URL` = tourflow-core's public URL, **no
   trailing slash**. Vite inlines it at build time → changing it requires a
   redeploy.
3. Deploy. `frontend/vercel.json` rewrites every path to `index.html`, so deep
   links and refreshes (`/tours/123`, `/bookings/…/pay`) don't 404.
4. **Close the CORS loop:** copy the Vercel production URL → Render →
   `tourflow-shared` → `CORS_ALLOWED_ORIGINS` = that URL (comma-separate to add
   a custom domain; no trailing slash) → services restart automatically.
   Preview deployments have different URLs and will be rejected unless added.
5. **Razorpay webhook** (payments confirm server-side even if the browser
   closes): Razorpay dashboard → Webhooks → URL
   `https://<core>/api/payments/webhook`, secret = `RAZORPAY_WEBHOOK_SECRET`.

## 7. Keep tourflow-core warm (and don't blow the hour pool)

750 free instance-hours/month are shared by *all* free services. An always-on
core alone is ~720–744 h, leaving almost nothing for the other five when they
wake — exceeding the pool suspends free services until the next month. So:

- Ping `https://<core>/actuator/health` every **10 min** from a free pinger
  (cron-job.org / UptimeRobot) **only during hours you need it** (e.g. 08:00–24:00
  ≈ 480 h/month), leaving ~270 h for the standalone services.
- Do **not** ping the other five.
- A cold core = full sequential boot of 4 JVMs on 0.1 vCPU (minutes). The
  frontend shows a "waking up the server" toast after 4 s of a pending
  request, and the gateway's proxy read-timeout is 90 s for the standalone
  services' cold starts.

## 8. Phase 2 — Kafka (enables search index, notifications, analytics)

Kafka carries `booking.events` (→ platform notifications, insights analytics)
and `tour.events` (→ search index). Until it exists, publishing fails fast
(3 s cap) and quietly and those features are simply inert. **Upstash Kafka no
longer exists.** Options:

| Option | Fit |
|---|---|
| **Redpanda Serverless** | Kafka-API drop-in, public TLS certs → works with the current SASL_SSL config as-is. Free credits, not permanently free — check current terms. |
| **Aiven free Kafka** | Permanently free but: auto-powers-off when idle, tiny limits (250 kB/s, 3-day retention), private-CA brokers → needs a small code addition (PEM truststore: `ssl.truststore.type=PEM` + `ssl.truststore.certificates`) and SASL enabled on the service. |
| Skip Kafka | Keep `SEARCH_BACKEND=catalog`; no notifications/analytics events. |

When you have a cluster: create topics `tour.events`, `booking.events`;
add to `tourflow-shared`: `KAFKA_BOOTSTRAP_SERVERS`, `KAFKA_SECURITY_PROTOCOL`
(`SASL_SSL`), `KAFKA_SASL_MECHANISM` (`SCRAM-SHA-256` unless the provider says
otherwise), `KAFKA_SASL_JAAS_CONFIG`
(`org.apache.kafka.common.security.scram.ScramLoginModule required username="…" password="…";`).
Consumer groups used: `platform-service`, `insights-service`, `search-service`.
Then on tourflow-search set `SEARCH_BACKEND=elasticsearch` (and re-publish
existing tours or accept that only newly published tours get indexed).

## 9. Rollback / troubleshooting

| Symptom | Likely cause |
|---|---|
| `lstat …/backend/deploy: no such file` | code not pushed to GitHub |
| Service crash on boot: JDBC/`SSL`/`channel binding` | wrong DB env, or `-pooler` host |
| Flyway/`relation … does not exist` or tables in `public` | used the pooled Neon host |
| Browser CORS error | `CORS_ALLOWED_ORIGINS` doesn't exactly match the Vercel origin (scheme, no trailing slash) |
| 502/504 right after idle | cold start — wait; check §7 |
| Search returns nothing | `SEARCH_BACKEND=elasticsearch` without Kafka, or `CATALOG_SERVICE_URL` unset |
| tourflow-search "unhealthy" with ES | `MANAGEMENT_HEALTH_ELASTICSEARCH_ENABLED` not `false` |
| Core restarts repeatedly | OOM — see decision gate in §4 |

Rollback: Render → service → Deploys → redeploy a previous commit; Vercel →
Deployments → Promote a previous one.

## 10. Known limitations (by design, not oversight)

- **Google login is not wired through the gateway** (no `/oauth2/**` or
  `/login/oauth2/**` routes, and no forwarded-header config for the OAuth
  redirect). Email/password auth is unaffected. Fixing it is a separate task.
- **File uploads are ephemeral** (`platform-service` writes to local disk; no
  persistent disk on free tier).
- **tourflow-core's RAM/CPU budget is tight and unmeasured** (§4 gate).
- **Catalog search fallback filters in memory** — fine for a demo only.
- Neon free / Elastic trial / Aiven free have their own quotas and expiry;
  re-check them if something stops working weeks later.
