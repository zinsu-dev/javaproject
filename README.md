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
# 1. Start Postgres (Docker option)
docker compose up -d

# ...or create the DB manually:
# createdb campuscourier

# 2. Run the app (reads DB_URL / DB_USERNAME / DB_PASSWORD, defaults to localhost)
./mvnw spring-boot:run
```

App: http://localhost:8080 · Swagger UI: http://localhost:8080/swagger-ui.html

## API cheat sheet

```bash
# Register a parcel
curl -X POST http://localhost:8080/api/v1/parcels \
  -H 'Content-Type: application/json' \
  -d '{"parcelType":"DOCUMENT","senderName":"Ama","recipientName":"Kofi",
       "pickupLocation":"Hall A","dropoffLocation":"Library",
       "weightKg":1.0,"distanceKm":2.0}'

# List all
curl http://localhost:8080/api/v1/parcels

# Track by code
curl http://localhost:8080/api/v1/parcels/track/CC-XXXXXXXX

# Update status
curl -X PATCH http://localhost:8080/api/v1/parcels/1/status \
  -H 'Content-Type: application/json' \
  -d '{"status":"IN_TRANSIT"}'

# Fee
curl http://localhost:8080/api/v1/parcels/1/fee
```

`parcelType` is `DOCUMENT`, `FRAGILE` or `BULK`. Status flows
`REGISTERED → ASSIGNED → IN_TRANSIT → DELIVERED` (or `CANCELLED`).

## Fee formulas (runtime polymorphism)

`Parcel.calculateFee()` is abstract; each subclass overrides it:

| Type | Formula |
|------|---------|
| DocumentParcel | 500 + 100·kg + 20·km |
| FragileParcel | 1500 + 300·kg + 50·km |
| BulkFreight | 3000 + 500·kg + 100·km |

Calling `parcel.calculateFee()` executes the subclass version — no `if/else` in the service.

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
./mvnw test   # H2 in-memory, never touches Postgres
```

Covers: fee polymorphism (`ParcelFeeTest`), service logic with Mockito
(`ParcelServiceTest`), Spring context (`CampusCourierApplicationTests`).

## React integration

All endpoints return JSON. CORS already allows
`http://localhost:3000` (CRA) and `http://localhost:5173` (Vite).
Errors always look like `{"status":404,"message":"...","timestamp":"..."}`.
