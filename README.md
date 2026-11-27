# Campus Courier & Dispatch Tracking System (Backend)

Spring Boot 4.0.x · Java 21 · Maven · PostgreSQL · JPA · REST · Swagger.

## Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| Java (JDK) | 21 | `java -version` |
| Maven | 3.9+ (or use `./mvnw`) | `./mvnw -version` |
| PostgreSQL | 14+ | `psql --version` |
| Git | any | `git --version` |

Install JDK 21 (Ubuntu):

```bash
sudo apt update && sudo apt install -y openjdk-21-jdk
java -version   # must say 21
```

## Quick start (no Postgres installed?)

Run with the in-memory H2 fallback — zero setup, data resets on restart:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

## Standard start (PostgreSQL)

```bash
# 1. Copy env template and start Postgres (Docker option)
cp .env.example .env   # then set DB_PASSWORD inside .env
docker compose up -d

# ...or create the DB manually:
# createdb campus_courier

# 2. Run the app. Flyway migrates the schema automatically on startup
#    (Hibernate only VALIDATES — it never creates tables).
#    Defaults read DB_URL / DB_USERNAME / DB_PASSWORD, else localhost.
./mvnw spring-boot:run

# 3. Same, but with SQL logging to learn what Hibernate is doing:
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

App: http://localhost:8080 · Swagger UI: http://localhost:8080/swagger-ui.html

## API cheat sheet (Phase 3: parcel management)

```bash
# Register a parcel → 201 Created + Location header
curl -X POST http://localhost:8081/api/parcels \
  -H 'Content-Type: application/json' \
  -d '{"parcelType":"DOCUMENT","senderName":"Ama","recipientName":"Kofi",
       "pickupLocation":"Hall A","destination":"Library","weight":2.0}'

# List all parcels
curl http://localhost:8081/api/parcels

# Get one by UUID
curl http://localhost:8081/api/parcels/<uuid>

# Track by code
curl http://localhost:8081/api/parcels/track/CC-XXXXXXXX

# Update editable delivery details
curl -X PUT http://localhost:8081/api/parcels/<uuid> \
  -H 'Content-Type: application/json' \
  -d '{"senderName":"Yaw","recipientName":"Esi","pickupLocation":"Gate",
       "destination":"Faculty","weight":3.0}'

# Delete → 204 No Content
curl -X DELETE http://localhost:8081/api/parcels/<uuid>
```

`parcelType` is `DOCUMENT`, `FRAGILE` or `BULK` (anything else → 400).
New parcels start `PENDING`; `trackingId`, `status` and fees are set by
the backend. Every response carries `handlingFee` + `totalFee` (flat
₦500 / ₦1500 / ₦3000 per type — runtime polymorphism, no if/else).
Status transitions and rider assignment arrive in later phases.
Errors always look like
`{"timestamp":"...","status":404,"error":"Not Found","message":"...","path":"..."}`.

## Fee formulas (runtime polymorphism)

`Parcel.calculateFee()` is abstract; each subclass overrides it:

| Type | Formula |
|------|---------|
| DocumentParcel | 500 flat |
| FragileParcel | 1500 flat |
| BulkFreight | 3000 flat |

Calling `parcel.calculateFee()` / `calculateHandlingFee()` executes the
subclass version — no `if/else` in the service (type selection for
*construction* lives in the dedicated `ParcelFactory`).

## Project layout

```
com.campuscourier
├── config/          # OpenAPI (Swagger), CORS for React
├── parcel/
│   ├── model/       # Parcel (abstract entity) + 3 subclasses + enums
│   ├── dto/         # Request/response JSON shapes (validation lives here)
│   ├── repository/  # Spring Data JPA interface (no SQL written by hand)
│   ├── service/     # Business logic (fee, tracking numbers, status)
│   └── controller/  # REST endpoints under /api/v1/parcels
└── common/exception # ApiError + 404/400 handler (clean JSON errors)
```

Why DTOs? Entities map to the database; DTOs map to JSON. Keeping them
separate means you can change the DB without breaking the React app.

## Tests

```bash
./mvnw test   # H2 in PostgreSQL mode: runs the REAL Flyway migration,
              # then Hibernate validates entities against it.
              # Still not a live-Postgres proof — run one before production:
              #   docker compose up -d && ./mvnw spring-boot:run
```

Covers: fee polymorphism (`ParcelTest`), constructor validation, rider
defaults, repository persistence + discriminators + rider link + tracking
uniqueness (`ParcelRepositoryTest`, `DispatchRiderRepositoryTest`),
service logic with Mockito (`ParcelServiceTest`), HTTP layer with
MockMvc (`ParcelControllerTest`), Spring context
(`CampusCourierApplicationTests`).

## React integration

All endpoints return JSON. CORS already allows
`http://localhost:3000` (CRA) and `http://localhost:5173` (Vite).
Errors always look like
`{"timestamp":"...","status":404,"error":"Not Found","message":"...","path":"/api/..."}`.
