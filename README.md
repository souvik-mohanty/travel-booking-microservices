<p align="center">
  <img src="frontend/public/logo.png" alt="TourFlow logo" width="110" />
</p>

# TourFlow

A full-stack travel and tour booking platform built as a set of Spring Boot microservices behind an API gateway, with a React + TypeScript single-page app. Tourists search and book tours and pay online; businesses publish tours and manage hotels and rooms; admins moderate the platform.

![Java](https://img.shields.io/badge/Java-21-orange) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1-6DB33F) ![React](https://img.shields.io/badge/React-19-61DAFB) ![TypeScript](https://img.shields.io/badge/TypeScript-6-3178C6) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Flyway-336791) ![Kafka](https://img.shields.io/badge/Kafka-events-231F20) ![Docker](https://img.shields.io/badge/Docker-ready-2496ED)

## Live demo

| | URL |
|---|---|
| Web app | https://tourflow.vertexcodelabs.in |
| API gateway | https://souvik-tourflow-gateway.onrender.com |

> **Free-tier hosting.** The backend runs on Render's free plan, which puts a service to sleep after 15 minutes idle. The **first request after a quiet period can take one to two minutes** while the services wake up. The app shows a loading screen while it waits; later requests are fast.

## Demo accounts

Use these on the live demo. All share one password.

| Role | Email (login ID) | Password | Name |
|---|---|---|---|
| Tourist | `aarav.tourist@example.com` | `Demo@12345` | Aarav Sharma |
| Tourist | `diya.tourist@example.com` | `Demo@12345` | Diya Patel |
| Tourist | `rohan.tourist@example.com` | `Demo@12345` | Rohan Das |
| Business | `meera.business@example.com` | `Demo@12345` | Meera Nair |
| Business | `kabir.business@example.com` | `Demo@12345` | Kabir Singh |
| Business | `ananya.business@example.com` | `Demo@12345` | Ananya Rao |

- **Tourist** lands on *Explore Tours* and can book and pay.
- **Business** lands on *My Tours* and can manage their business, hotels, rooms and tours.
- **Admin** accounts are not published. The admin role cannot be chosen at sign-up; it is granted directly in the database (see [Create an admin](#create-an-admin-locally)).
- These are shared demo accounts on a demo database: anyone can use them, and data may be reset at any time. Do not enter real personal data.
- Payments run in Razorpay **test mode**; no real money moves. Use Razorpay's [test card details](https://razorpay.com/docs/payments/payments/test-card-details/).

## What works and what doesn't

**Legend:** ✅ working, 🟡 partly working or needs configuration, ❌ not built.

### Accounts and access

| Feature | Status | Notes |
|---|---|---|
| Register and sign in with email and password (tourist, business) | ✅ | Tested on the live demo |
| JWT access token + rotating, revocable refresh token; sign out | ✅ | 15-minute access token, 30-day refresh token |
| Role-based access (tourist / business / admin) | ✅ | Enforced in the UI **and** on the server: for example only tourists can book or pay, only admins can call admin endpoints |
| Disabled accounts are blocked at sign-in | ✅ | Clear "account disabled" message |
| Sign in with Google | 🟡 | Implemented end to end (OIDC through the gateway). Needs a Google OAuth client configured on the identity service, so it only works where those credentials are set correctly. Always creates a *tourist* account |
| Loading screen while signing in / registering | ✅ | Explains free-tier wake-up delays |

### Tourist

| Feature | Status | Notes |
|---|---|---|
| Explore tours, search and filter (text, destination, price) | ✅ | Live demo uses a Postgres-backed search (see below) |
| Destinations page, tour detail page | ✅ | Destinations are derived from published tours |
| Book a tour | ✅ | Price is calculated on the server, never trusted from the browser |
| Pay with Razorpay (test mode) | ✅ | Order creation and checkout verified; the server verifies the payment signature before marking a booking paid |
| Payment webhook (backup confirmation) | 🟡 | Implemented; needs the webhook URL and secret set in the Razorpay dashboard |
| My bookings, booking detail, printable receipt | ✅ | |
| Reviews, support tickets | ❌ | Backend endpoints exist; no UI yet |
| Notification centre | ❌ | Backend exists; no UI yet |

### Business

| Feature | Status | Notes |
|---|---|---|
| Create, publish and cancel tours | ✅ | Draft → published → cancelled |
| My Business (create and edit profile) | ✅ | |
| My Hotels: add and edit hotels | ✅ | |
| Rooms: add, edit, activate, deactivate, delete, check availability | ✅ | New rooms start as drafts until activated |
| Tour itinerary (legs) and activities | ❌ | Backend exists; no UI yet |
| Trips and trip logs | ❌ | Backend exists; no UI yet |

### Admin

| Feature | Status | Notes |
|---|---|---|
| Users: search, filter by role, enable or disable | ✅ | An admin cannot disable their own account |
| Businesses and hotels: suspend and reinstate | ✅ | With a confirmation step |
| Analytics dashboard (charts) | 🟡 | UI works, but its data comes from booking events sent through Kafka, which is not connected on the live demo, so the charts are empty there. Works locally |
| Audit log viewer | ❌ | Backend API exists (services can post audit entries); no UI yet |

### Platform

| Area | Status | Notes |
|---|---|---|
| 10 services deployed (gateway + 9 domain services) | ✅ | Render (backend), Vercel (frontend), Neon (PostgreSQL) |
| Kafka event streaming | 🟡 | Producers and consumers work locally. **Not connected on the live demo** (no free managed Kafka), so notifications and analytics events don't flow there |
| Elasticsearch search | 🟡 | Works locally and against Elastic Cloud. The live demo uses a Postgres-backed fallback instead |
| Mobility (rides, drivers, fleet, tracking), guides, file uploads | ❌ | Backend services exist; no UI. Uploaded files are not persistent on the free tier |
| Automated tests and CI pipeline | ❌ | No committed test suite; the workflow files are empty placeholders |
| Google sign-in, payments webhook | 🟡 | See above |

## Architecture

```
React SPA (Vercel)
      │  HTTPS
      ▼
API Gateway (Spring Cloud Gateway) ── single entry point, CORS, routing
      │
      ├── identity     :8081   sign-up/login, JWT, roles, Google OAuth2, admin users
      ├── catalog      :8082   tours, businesses, hotels & rooms, activities, guides
      ├── booking      :8083   bookings (server-side pricing)
      ├── payment      :8084   Razorpay orders, signature verification, webhook
      ├── mobility     :8086   rides, drivers, fleet, trips, tracking
      ├── engagement   :8087   reviews and support tickets
      ├── platform     :8088   notifications and files
      ├── insights     :8089   audit log and analytics
      └── search       :8099   tour search (Elasticsearch, or catalog fallback)

Each service owns its own PostgreSQL schema (Flyway migrations).
Services publish/consume events on Kafka: booking.events, tour.events.
```

**Booking and payment flow**

1. A tourist opens a published tour and books it. The booking service fetches the tour, calculates the price, and stores a `PENDING` booking.
2. The payment service creates a Razorpay order for that booking.
3. The browser opens Razorpay Checkout and the user pays.
4. The browser sends Razorpay's response to the payment service, which **verifies the signature on the server**. A browser callback alone is never trusted.
5. On success the payment service marks the booking paid using a service-to-service token. The webhook is an independent, idempotent backup for the same step.
6. Booking events go to Kafka, feeding notifications and analytics.

**Design notes:** the initial 25-service design was consolidated into 9 domain services plus a gateway ([ADR-002](docs/decisions/ADR-002-SERVICE-CONSOLIDATION.md)); the free-tier deployment topology and its constraints are in [ADR-003](docs/decisions/ADR-003-RENDER-DEPLOYMENT.md).

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | React 19, TypeScript, Vite 6, React Router 7, Tailwind CSS 4, shadcn/ui (Radix), TanStack Query 5, Zustand, React Hook Form + Zod, Axios, Recharts |
| Backend | Java 21, Spring Boot 4.1, Spring Security, Spring Data JPA (Hibernate), Spring Cloud Gateway (WebMVC), Bean Validation, jjwt, Flyway |
| Data | PostgreSQL (schema per service), Apache Kafka, Elasticsearch, Redis (provisioned locally, currently unused) |
| Payments | Razorpay |
| Auth | JWT + refresh tokens, Google OAuth2 / OIDC |
| Observability | Spring Actuator, Micrometer, Prometheus, Grafana, Kafka UI |
| DevOps | Docker (multi-stage images), Docker Compose, Render Blueprint (`render.yaml`), Vercel, Neon |

## Getting started locally

**Prerequisites:** JDK 21, Maven 3.9+, Node.js 20+, Docker Desktop.

### 1. Clone and configure

```bash
git clone https://github.com/souvik-mohanty/travel-booking-microservices.git
cd travel-booking-microservices/backend
cp .env.example .env        # keep the database defaults; set KAFKA_UI_PASSWORD
```

`.env` holds the passwords Docker uses. The example already contains working local database defaults (`tour_user` / `tour_password` / `tour_planner`), which are also what every service uses when nothing else is set, so keep them and just choose a Kafka UI password. `GOOGLE_*` and `RAZORPAY_*` are only needed for those features.

### 2. Start the infrastructure

```bash
docker compose up -d        # PostgreSQL, Kafka, Elasticsearch, Redis, Kafka UI, Prometheus, Grafana
```

### 3. Start the services

Run each in its own terminal. The defaults match the Docker setup, so no extra configuration is needed:

```bash
cd services/identity-service   && mvn spring-boot:run
cd services/catalog-service    && mvn spring-boot:run
cd services/booking-service    && mvn spring-boot:run
cd services/payment-service    && mvn spring-boot:run     # export RAZORPAY_KEY_ID / RAZORPAY_KEY_SECRET first for payments
cd services/search-service     && mvn spring-boot:run
# optional: mobility-service, engagement-service, platform-service, insights-service
cd gateway/api-gateway         && mvn spring-boot:run     # start last
```

Flyway creates every service's schema on first start.

### 4. Start the frontend

```bash
cd ../frontend
npm install
npm run dev                 # http://localhost:3000  (API base URL: http://localhost:8080)
```

### Ports

| App | Port | | Tool | Port |
|---|---|---|---|---|
| Frontend | 3000 | | Kafka UI | 8085 |
| Gateway | 8080 | | Prometheus | 9090 |
| identity / catalog / booking / payment | 8081 / 8082 / 8083 / 8084 | | Grafana | 3030 |
| mobility / engagement / platform / insights | 8086 / 8087 / 8088 / 8089 | | Elasticsearch | 9200 |
| search | 8099 | | PostgreSQL | 5432 |

### Create demo users locally

The hosted demo accounts live only on the hosted database. To create the same ones on your machine (gateway must be running):

```bash
reg() { curl -s -o /dev/null -w "$1 -> %{http_code}\n" -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$1\",\"password\":\"Demo@12345\",\"firstName\":\"$2\",\"lastName\":\"$3\",\"role\":\"$4\"}"; }
reg aarav.tourist@example.com  Aarav Sharma TOURIST
reg meera.business@example.com Meera Nair   BUSINESS
```

### Create an admin locally

Register a normal account, then promote it in the database and sign in again (the role is carried inside the login token):

```bash
docker exec -it tour-postgres psql -U tour_user -d tour_planner \
  -c "UPDATE identity_service.users SET role='ADMIN' WHERE email='you@example.com';"
```

(Use the `POSTGRES_USER` / `POSTGRES_DB` values from your `.env` if you changed them.)

### Sign in with Google locally (optional)

Run the identity service with `SPRING_PROFILES_ACTIVE=oauth2`, `GOOGLE_CLIENT_ID` and `GOOGLE_CLIENT_SECRET` set, and add `http://localhost:8080/login/oauth2/code/google` as an authorized redirect URI on your Google OAuth client.

## Deployment

The live demo runs on free tiers: **Render** (10 web services, defined in [`render.yaml`](render.yaml)), **Vercel** (frontend) and **Neon** (PostgreSQL). Step-by-step instructions, environment variables and the reasoning behind the topology are in [`backend/deploy/README.md`](backend/deploy/README.md).

## Project structure

```
backend/
  gateway/api-gateway/     Spring Cloud Gateway
  services/                identity, catalog, booking, payment, search,
                           mobility, engagement, platform, insights
  deploy/                  Dockerfiles and deployment runbook
  docker-compose.yml       local infrastructure
frontend/                  React + TypeScript app
docs/                      architecture, API, database, events, decisions (ADRs)
render.yaml                Render Blueprint
```

## Documentation

- [Architecture](docs/architecture/ARCHITECTURE.md) · [Service boundaries](docs/architecture/SERVICE-BOUNDARIES.md) · [Security](docs/architecture/SECURITY.md) · [Communication](docs/architecture/COMMUNICATION.md)
- [API reference](docs/api/API-REFERENCE.md) · [API standards](docs/api/API-STANDARDS.md)
- [Database design](docs/database/DATABASE-DESIGN.md) · [ER diagram](docs/database/ER-DIAGRAM.md)
- [Event catalog](docs/events/EVENT-CATALOG.md) · [Event schemas](docs/events/EVENT-SCHEMAS.md)
- Decisions: [ADR-001 Microservices](docs/decisions/ADR-001-MICROSERVICES.md) · [ADR-002 Service consolidation](docs/decisions/ADR-002-SERVICE-CONSOLIDATION.md) · [ADR-003 Render deployment](docs/decisions/ADR-003-RENDER-DEPLOYMENT.md)

## Roadmap

- Connect a managed Kafka on the live demo so notifications and analytics fill in
- UIs for tour itinerary and activities, trips, reviews, support tickets and notifications
- Automated test suite and a CI pipeline
- Persistent file storage and a production-grade search index

## License

No license has been chosen yet. Add a `LICENSE` before reusing this code.
