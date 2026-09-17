# API Reference

Every endpoint actually implemented across the 19 built services, as of
2026-09-02. Scaffold-only services (search, analytics, ai-orchestrator,
ai-chatbot) have no APIs yet and are omitted -- see
`architecture/SERVICE-BOUNDARIES.md` for their status.

This is the concrete "what exists" companion to `API-STANDARDS.md`, which
covers the *conventions* (auth, ownership, status codes, 404-vs-403,
idempotency, service-to-service calls) those conventions are referenced
here by name rather than repeated per endpoint -- read that doc first if
something below looks unexplained (e.g. why a mark-paid endpoint requires
`ROLE_SERVICE`, or why a create endpoint 404s instead of 403ing).

## How to call these

Each service is called **directly on its own port** -- `api-gateway`
(`:8080`) has no routes configured yet (its `application.yml` has no
`spring.cloud.gateway` section at all), so `Routing only` in
`ARCHITECTURE.md`'s status table is aspirational, not real yet. Every
example below uses `http://localhost:<port>`.

Every endpoint requires `Authorization: Bearer <access token>` **except**
the ones explicitly marked `Public` below (identity's register/login/
refresh/logout, most review/activity/room GET reads, Razorpay's webhook,
and the two Google OAuth2 endpoints). Get a token from
`POST /api/auth/login` on identity-service (`:8081`).

---

## Identity Domain

### identity-service (`:8081`)

Local email/password auth plus Google OAuth2. `AuthResponse` (returned by
register/login/refresh) always has the same shape:
`{ accessToken, refreshToken, userId, email, role }`.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/auth/register` | Public | `email, password, firstName, lastName?` | Creates the user, returns `AuthResponse` (201). `409` if the email is already registered. |
| POST | `/api/auth/login` | Public | `email, password` | Verifies credentials, returns a fresh `AuthResponse`. `401` on bad credentials or a disabled account. |
| POST | `/api/auth/refresh` | Public | `refreshToken` | Exchanges a still-valid refresh token for a new `AuthResponse` (rotates the refresh token too). |
| POST | `/api/auth/logout` | Public | `refreshToken` | Revokes that refresh token. `204`. Client still must discard its local tokens. |
| GET | `/oauth2/authorization/google` | Public | -- | Starts the Google OAuth2 handshake (redirect), only live when the `oauth2` profile has real Google credentials configured. |
| GET | `/login/oauth2/code/google` | Public | -- | Google's callback URL. On success, provisions/links the user and redirects to the frontend with the same `AuthResponse`-shaped token pair as local login. |

### user-service (`:8088`)

`UserProfileResponse`: `{ id, userId, fullName, phone, dateOfBirth, address, city, state, country, emergencyContactName, emergencyContactPhone, createdAt, updatedAt }`.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/users/profile` | Required | `fullName, phone?, dateOfBirth?, address?, city?, state?, country?, emergencyContactName?, emergencyContactPhone?` | Creates the caller's own profile (201). `409` if one already exists (`UNIQUE(user_id)`). |
| GET | `/api/users/profile/me` | Required | -- | The caller's own profile. |
| PUT | `/api/users/profile` | Required | Same fields as create | Updates the caller's own profile (full replace). |
| GET | `/api/users/{id}` | Required | -- | Any profile by its profile ID (not `userId`) -- used by other services/clients that already have the ID. |

### authorization-service (`:8089`)

Real RBAC: `Role` / `Permission` / `RolePermission` / `UserRole`. No admin
gate on any of these endpoints -- there's no admin role model anywhere in
the project yet (see `API-STANDARDS.md`); this service is the natural
future home for that gate.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/roles` | Required | `name, description?` | Creates a role, returns `RoleResponse` (201). |
| GET | `/api/roles` | Required | -- | All roles. |
| GET | `/api/roles/{id}` | Required | -- | One role. |
| GET | `/api/roles/{id}/permissions` | Required | -- | Permissions granted by that role (`List<PermissionResponse>`). |
| POST | `/api/permissions` | Required | `name, description?` | Creates a permission (201). |
| GET | `/api/permissions` | Required | -- | All permissions. |
| GET | `/api/permissions/{id}` | Required | -- | One permission. |
| POST | `/api/roles/{roleId}/permissions` | Required | `permissionId` | Grants a permission to a role. `201`, empty body. |
| POST | `/api/users/{userId}/roles` | Required | `roleId` | Grants a role to a user. `201`, empty body. |
| GET | `/api/users/{userId}/roles` | Required | -- | Roles assigned to a user (`List<RoleResponse>`). |
| GET | `/api/users/{userId}/permissions` | Required | -- | **Effective permissions**: the deduplicated union of permissions across every role the user has (`List<PermissionResponse>`) -- resolved through both join tables. |

---

## Travel / Commerce

### tour-service (`:8082`)

`TourResponse`: `{ id, title, description, destination, startDate, endDate, price, maxParticipants, status, createdBy, createdAt, updatedAt }`.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/tours` | Required | `title, description?, destination, startDate, endDate, price, maxParticipants` | Creates a tour owned by the caller (`createdBy` = JWT subject). `201`. |
| GET | `/api/tours/{id}` | Required | -- | One tour. `404` if it doesn't exist. |
| GET | `/api/tours` | Required | -- | All tours. |

### booking-service (`:8083`)

`BookingResponse`: `{ id, tourId, userId, numberOfParticipants, totalPrice, status, createdAt, updatedAt }`. `status` is `PENDING -> PAID -> COMPLETED`, or `CANCELLED`. `totalPrice` is computed server-side from tour-service's real price, never trusted from the client.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/bookings` | Required | `tourId, numberOfParticipants` | Fetches the tour's real price from tour-service, creates a `PENDING` booking. `201`. |
| GET | `/api/bookings/{id}` | Required | -- | One booking. |
| GET | `/api/bookings` | Required | -- | All bookings belonging to the caller. |
| PATCH | `/api/bookings/{id}/mark-paid` | `ROLE_SERVICE` only | -- | Flips `PENDING -> PAID`. Called only by payment-service via a minted service JWT, never by a user directly. Idempotent; publishes a `BookingConfirmed` Kafka event on success (see `architecture/COMMUNICATION.md`). |
| PATCH | `/api/bookings/{id}/complete` | Required (tour's creator only) | -- | Flips `PAID -> COMPLETED`. Verified against tour-service's `createdBy`, not the booking's own `userId`. `403` if the caller didn't create the tour. |
| PATCH | `/api/bookings/{id}/cancel` | Required (booking owner only) | -- | Flips to `CANCELLED`. `400` if already `COMPLETED`. |

### payment-service (`:8084`)

Real Razorpay integration (test mode). `PaymentResponse`: `{ id, bookingId, customerId, amount, currency, paymentStatus, payoutStatus, platformFee, businessAmount, gateway, gatewayOrderId, gatewayPaymentId, paidAt, payoutEligibleAt, paidOutAt, createdAt, updatedAt }`. `paymentStatus`: `CREATED/PENDING/PAID/FAILED/REFUNDED`. Platform fee is a flat 10% of the booking amount.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/payments` | Required | `bookingId` | Verifies the booking via booking-service, creates a Razorpay order. Returns `CreatePaymentResponse { paymentId, razorpayOrderId, razorpayKeyId, amount, currency, paymentStatus }` -- exactly what the frontend needs to open Razorpay Checkout. `201`. |
| POST | `/api/payments/{paymentId}/verify` | Required | `razorpay_order_id, razorpay_payment_id, razorpay_signature` (Razorpay Checkout's own field names) | Verifies Razorpay's signature; on success marks the payment `PAID` and calls booking-service's `mark-paid`. Returns the updated `PaymentResponse`. |
| POST | `/api/payments/webhook` | Public (signature-authenticated via `X-Razorpay-Signature` header, not a JWT) | Raw Razorpay webhook JSON | Server-to-server confirmation from Razorpay, idempotent with the `/verify` path (whichever arrives first wins). `200`, empty body. |
| GET | `/api/payments/{paymentId}` | Required | -- | One payment. |
| GET | `/api/payments` | Required | -- | All payments belonging to the caller. |

### business-service (`:8086`)

`BusinessResponse`: `{ id, ownerId, name, description, phone, email, address, city, state, country, status, createdAt, updatedAt }`. `status`: `ACTIVE/SUSPENDED/INACTIVE` (no approval workflow yet -- created `ACTIVE`). `ActivityResponse`: `{ id, businessId, name, description, location, price, durationMinutes, maxParticipants, status, createdAt, updatedAt }`. `status`: `DRAFT/ACTIVE/INACTIVE/DELETED` (created `DRAFT`).

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/businesses` | Required | `name, description?, phone?, email?, address?, city?, state?, country?` | Registers a business owned by the caller. `201`. Multiple businesses per owner are allowed. |
| GET | `/api/businesses` | Required | -- | All businesses. |
| GET | `/api/businesses/{id}` | Required | -- | One business. |
| PUT | `/api/businesses/{id}` | Required (owner only) | Same fields as create | Full update. `403` if not the owner. |
| GET | `/api/businesses/{businessId}/activities` | Required | -- | All activities offered by that business. |
| POST | `/api/activities` | Required (business owner) | `businessId, name, description?, location?, price, durationMinutes, maxParticipants` | Creates a `DRAFT` activity under a business the caller owns. `404` (not `403`) if the business doesn't exist or isn't the caller's -- see the 404-vs-403 rule in `API-STANDARDS.md`. `201`. |
| GET | `/api/activities` | Required | -- | All activities, any status. |
| GET | `/api/activities/{id}` | Required | -- | One activity. |
| GET | `/api/activities/active` | Required | -- | Only `ACTIVE` activities -- the public-facing "bookable now" list. |
| PUT | `/api/activities/{id}` | Required (owner only) | Same fields as create minus `businessId` | Full update. `403` if not the owning business's owner. |
| DELETE | `/api/activities/{id}` | Required (owner only) | -- | Soft-delete: sets status to `DELETED`, returns the updated `ActivityResponse` (not `204`). |
| PATCH | `/api/activities/{id}/activate` | Required (owner only) | -- | `-> ACTIVE`. Idempotent. |
| PATCH | `/api/activities/{id}/deactivate` | Required (owner only) | -- | `-> INACTIVE`. Idempotent. |

### hotel-service (`:8096`)

Exact mirror of business/activity's two-level pattern. `HotelResponse`: `{ id, ownerId, name, description, address, city, state, country, status, createdAt, updatedAt }`. `RoomResponse`: `{ id, hotelId, roomType, pricePerNight, capacity, totalRooms, status, createdAt, updatedAt }`, `status`: `DRAFT/ACTIVE/INACTIVE/DELETED`.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/hotels` | Required | `name, description?, address?, city?, state?, country?` | Registers a hotel owned by the caller. `201`. |
| GET | `/api/hotels` | Required | -- | All hotels. |
| GET | `/api/hotels/{id}` | Required | -- | One hotel. |
| PUT | `/api/hotels/{id}` | Required (owner only) | Same fields as create | Full update. |
| GET | `/api/hotels/{hotelId}/rooms` | Required | -- | All rooms at that hotel. |
| POST | `/api/rooms` | Required (hotel owner) | `hotelId, roomType, pricePerNight, capacity, totalRooms` | Creates a `DRAFT` room under a hotel the caller owns. `404` if not the owner (existence-hiding rule). `201`. |
| GET | `/api/rooms` | Required | -- | All rooms, any status. |
| GET | `/api/rooms/{id}` | Required | -- | One room. |
| GET | `/api/rooms/active` | Required | -- | Only `ACTIVE` rooms -- the bookable-now list. |
| PUT | `/api/rooms/{id}` | Required (owner only) | Same fields as create minus `hotelId` | Full update. |
| DELETE | `/api/rooms/{id}` | Required (owner only) | -- | Soft-delete (`-> DELETED`), returns the updated `RoomResponse`. |
| PATCH | `/api/rooms/{id}/activate` | Required (owner only) | -- | `-> ACTIVE`. Idempotent. |
| PATCH | `/api/rooms/{id}/deactivate` | Required (owner only) | -- | `-> INACTIVE`. Idempotent. |

### ride-service (`:8090`)

`RideResponse`: `{ id, createdBy, type, pickupLocation, dropLocation, totalSeats, availableSeats, pricePerSeat, status, createdAt, updatedAt }`. `RideBookingResponse`: `{ id, rideId, userId, seatsBooked, status, createdAt, updatedAt }`. Seat booking is pessimistic-locked at the DB row level -- stress-tested under real concurrent load (see `API-STANDARDS.md`'s Concurrency section).

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/rides` | Required | `type, pickupLocation, dropLocation, totalSeats, pricePerSeat` | Creates a ride owned by the caller. `201`. |
| GET | `/api/rides` | Required | -- | All rides, any status. |
| GET | `/api/rides/available` | Required | -- | Only rides with `availableSeats > 0` and not cancelled -- the bookable-now list. |
| GET | `/api/rides/mine` | Required | -- | Rides the caller created. |
| GET | `/api/rides/{id}` | Required | -- | One ride. |
| PATCH | `/api/rides/{id}/cancel` | Required (creator only) | -- | Cancels the ride. |
| GET | `/api/rides/{rideId}/bookings` | Required | -- | All seat bookings for that ride. |
| POST | `/api/ride-bookings` | Required | `rideId, seats` | Books `seats` seats on a ride under a row lock; `400` if not enough seats remain. `201`. |
| GET | `/api/ride-bookings/mine` | Required | -- | The caller's own seat bookings. |
| PATCH | `/api/ride-bookings/{id}/cancel` | Required (owner only) | -- | Cancels the booking and returns the seats to `availableSeats`. |

### review-service (`:8087`)

`ReviewResponse`: `{ id, bookingId, businessId, activityId, userId, rating, title, comment, status, createdAt, updatedAt }`. `status`: `ACTIVE/HIDDEN/DELETED`. Creation is gated: the booking must actually belong to the caller and be `COMPLETED` (verified live against booking-service), and `activityId` must actually belong to `businessId` (verified against business-service).

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/reviews` | Required | `bookingId, businessId, activityId, rating (1-5), title?, comment` | Creates a review for the caller's own completed booking. `201`. `400`/`403` if the booking isn't the caller's or isn't `COMPLETED`. |
| GET | `/api/reviews/{reviewId}` | Public | -- | One review. |
| GET | `/api/reviews/me` | Required | -- | Reviews written by the caller. |
| GET | `/api/reviews/business/{businessId}` | Public | -- | Active reviews for a business. |
| GET | `/api/reviews/activity/{activityId}` | Public | -- | Active reviews for an activity. |
| PUT | `/api/reviews/{reviewId}` | Required (author only) | `rating?, title?, comment?` (partial -- `null` means unchanged) | Updates the caller's own review. |
| DELETE | `/api/reviews/{reviewId}` | Required (author only) | -- | Soft-delete (`-> DELETED`). `204`. |

---

## Operations

### driver-service (`:8091`)

`DriverResponse`: `{ id, userId, licenseNumber, licenseExpiryDate, phone, status, availability, createdAt, updatedAt }`. `status` starts (and stays) `PENDING_VERIFICATION` -- no verifier role exists yet to move it. `availability`: `AVAILABLE/UNAVAILABLE`.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/drivers` | Required | `licenseNumber, licenseExpiryDate (future), phone?` | Registers the caller as a driver. `201`. |
| GET | `/api/drivers/me` | Required | -- | The caller's own driver profile. |
| GET | `/api/drivers/{id}` | Required | -- | One driver profile. |
| PATCH | `/api/drivers/{id}/available` | Required (self only) | -- | Sets `availability = AVAILABLE`. |
| PATCH | `/api/drivers/{id}/unavailable` | Required (self only) | -- | Sets `availability = UNAVAILABLE`. |

### fleet-service (`:8092`)

`VehicleResponse`: `{ id, ownerId, registrationNumber, type, capacity, status, createdAt, updatedAt }`.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/vehicles` | Required | `registrationNumber, type, capacity` | Registers a vehicle owned by the caller. `201`. |
| GET | `/api/vehicles` | Required | -- | All vehicles. |
| GET | `/api/vehicles/mine` | Required | -- | Vehicles the caller owns. |
| GET | `/api/vehicles/{id}` | Required | -- | One vehicle. |
| PUT | `/api/vehicles/{id}` | Required (owner only) | `registrationNumber, type, capacity` | Full update. |
| PATCH | `/api/vehicles/{id}/status?status=X` | Required (owner only) | -- (status via query param) | Generic status transition (e.g. `ACTIVE`, `MAINTENANCE`, `RETIRED`). |

### guide-service (`:8093`)

`GuideResponse`: `{ id, userId, bio, languagesSpoken, yearsOfExperience, phone, status, availability, createdAt, updatedAt }`. Mirrors driver-service exactly, including the permanent `PENDING_VERIFICATION` gap.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/guides` | Required | `bio?, languagesSpoken?, yearsOfExperience?, phone?` | Registers the caller as a guide. `201`. |
| GET | `/api/guides/me` | Required | -- | The caller's own guide profile. |
| GET | `/api/guides/{id}` | Required | -- | One guide profile. |
| PATCH | `/api/guides/{id}/available` | Required (self only) | -- | Sets `availability = AVAILABLE`. |
| PATCH | `/api/guides/{id}/unavailable` | Required (self only) | -- | Sets `availability = UNAVAILABLE`. |

### trip-service (`:8094`)

`TripResponse`: `{ id, bookingId, createdBy, driverId, vehicleId, status, startedAt, completedAt, createdAt, updatedAt }`. `status`: `SCHEDULED -> IN_PROGRESS -> COMPLETED`, or `CANCELLED`. `driverId`/`vehicleId` are accepted by ID only -- **not** cross-checked against driver-service/fleet-service (a documented gap, see `database/ER-DIAGRAM.md`).

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/trips` | Required | `bookingId` | Verifies the booking exists (via booking-service) and creates a `SCHEDULED` trip. `201`. |
| GET | `/api/trips/mine` | Required | -- | Trips the caller created. |
| GET | `/api/trips/{id}` | Required | -- | One trip. |
| PATCH | `/api/trips/{id}/assign` | Required (creator only) | `driverId, vehicleId` | Assigns a driver + vehicle before the trip can start. |
| PATCH | `/api/trips/{id}/start` | Required (creator only) | -- | `SCHEDULED -> IN_PROGRESS`. `400` if no driver/vehicle assigned yet. |
| PATCH | `/api/trips/{id}/complete` | Required (creator only) | -- | `IN_PROGRESS -> COMPLETED`. |
| PATCH | `/api/trips/{id}/cancel` | Required (creator only) | -- | `-> CANCELLED`. |

### tracking-service (`:8095`)

`TripLocationResponse`: `{ id, tripId, latitude, longitude, recordedAt }`. Append-only location points, REST-polling only (no WebSocket/Redis push -- see `architecture/COMMUNICATION.md`). `tripId` is **not** cross-checked against trip-service, and neither endpoint verifies the caller has anything to do with the trip -- authentication is required, but there's no ownership check at all here (a real, undocumented-until-now gap worth tightening before this is used for anything beyond a demo).

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/tracking/{tripId}/locations` | Required | `latitude (-90..90), longitude (-180..180)` | Records one location point for the trip. `201`. |
| GET | `/api/tracking/{tripId}/locations/latest` | Required | -- | The most recently recorded point. `404` if none exist yet. |
| GET | `/api/tracking/{tripId}/locations/history` | Required | -- | All points for the trip, oldest first. |

---

## Platform Services

### notification-service (`:8097`)

`NotificationResponse`: `{ id, userId, title, message, read, createdAt }`. In-app only (no email/SMS/push). As of 2026-09-02, also auto-created by a Kafka consumer on `booking.events` -- see `architecture/COMMUNICATION.md`.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/notifications` | Required | `userId (recipient, not caller), title, message` | Creates a notification for `userId`. The one deliberate exception to "ownership comes from the JWT" in the whole project -- see `API-STANDARDS.md`. `201`. |
| GET | `/api/notifications/mine` | Required | -- | All of the caller's notifications. |
| GET | `/api/notifications/mine/unread` | Required | -- | Only unread ones. |
| PATCH | `/api/notifications/{id}/read` | Required (owner only) | -- | Marks one as read. Idempotent. |
| PATCH | `/api/notifications/mine/read-all` | Required | -- | Marks every one of the caller's notifications as read. `204`. |

### support-service (`:8098`)

`TicketResponse`: `{ id, userId, subject, description, status, createdAt, updatedAt }`. `status`: `OPEN/ASSIGNED/IN_PROGRESS/ESCALATED/RESOLVED/CLOSED` (`CLOSED` is terminal). Self-service transitions -- no agent/admin role exists yet, so the ticket's own creator drives its status.

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/tickets` | Required | `subject, description` | Creates a ticket for the caller, status `OPEN`. `201`. |
| GET | `/api/tickets/mine` | Required | -- | The caller's own tickets. |
| GET | `/api/tickets/{id}` | Required | -- | One ticket. |
| PATCH | `/api/tickets/{id}/status?status=X` | Required (owner only) | -- (status via query param) | Transitions status. `400` if already `CLOSED` or the transition is invalid. |

### audit-service (`:8101`)

`AuditLogResponse`: `{ id, actorId, action, resourceType, resourceId, details, occurredAt }`. Append-only -- no update or delete endpoint exists anywhere in this service, deliberately (an editable audit trail isn't one).

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/audit-logs` | Required | `action, resourceType, resourceId?, details?` | Records one entry; `actorId` is the caller's JWT subject (a system caller with no user context would need a minted `ROLE_SERVICE` token, same pattern as payment-service -> booking-service). `201`. |
| GET | `/api/audit-logs` | Required | -- | Every audit log in the system. |
| GET | `/api/audit-logs/mine` | Required | -- | Only entries where the caller is the actor. |
| GET | `/api/audit-logs/{id}` | Required | -- | One entry. |

### file-service (`:8102`)

`FileMetadataResponse`: `{ id, ownerId, fileName, contentType, sizeBytes, createdAt }`. Real local-disk storage under `file.storage-path` -- a literal stand-in for S3, not a stub (verified via byte-for-byte upload/download and physical deletion on delete).

| Method | Path | Auth | Request Body | Returns / Purpose |
|---|---|---|---|---|
| POST | `/api/files` (multipart) | Required | `file` (multipart form field) | Stores the file on disk, owned by the caller. Returns `FileMetadataResponse`. `201`. |
| GET | `/api/files/mine` | Required | -- | Metadata for every file the caller owns. |
| GET | `/api/files/{id}` | Required | -- | Metadata for one file (not the content). |
| GET | `/api/files/{id}/download` | Required (owner only) | -- | The raw file bytes, with `Content-Disposition: attachment` and the original filename/content-type. `403` if the caller isn't the owner. |
| DELETE | `/api/files/{id}` | Required (owner only) | -- | Deletes the DB row **and** the physical file on disk. `204`. |

---

## Everything not in this list

`search-service`, `analytics-service`, `ai-orchestrator-service`,
`ai-chatbot-service` exist only as Phase-0 scaffolds (`pom.xml`,
`application.yml`, the JWT security trio, empty package structure) -- no
controllers, no entities, nothing to document yet. See
`architecture/SERVICE-BOUNDARIES.md` for why each one is still blocked.
