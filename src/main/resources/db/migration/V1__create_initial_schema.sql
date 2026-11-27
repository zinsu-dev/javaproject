-- Campus Courier initial schema (Phase 2).
-- Applied by Flyway on startup. Hibernate NEVER creates tables
-- (ddl-auto=validate): this file is the single source of truth.
-- Kept compatible with PostgreSQL (production/dev) so every
-- repository test proves the REAL schema, not a Hibernate guess.

CREATE TABLE dispatch_riders (
    id           UUID         PRIMARY KEY,
    full_name    VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL,
    available    BOOLEAN      NOT NULL
);

-- ONE table for all parcel types (SINGLE_TABLE inheritance).
-- parcel_type tells Hibernate which subclass to build:
-- 'DOCUMENT', 'FRAGILE' or 'BULK'.
CREATE TABLE parcels (
    id               UUID          PRIMARY KEY,
    parcel_type      VARCHAR(31)   NOT NULL,
    tracking_id      VARCHAR(255)  NOT NULL UNIQUE,
    sender_name      VARCHAR(255)  NOT NULL,
    recipient_name   VARCHAR(255)  NOT NULL,
    pickup_location  VARCHAR(255)  NOT NULL,
    destination      VARCHAR(255)  NOT NULL,
    weight           NUMERIC(10, 3) NOT NULL,
    status           VARCHAR(50)   NOT NULL,
    rider_id         UUID          REFERENCES dispatch_riders (id)
);

-- Fast tracking-code lookups (used by findByTrackingId).
CREATE INDEX idx_parcels_tracking_id ON parcels (tracking_id);

-- Fast "all parcels of one rider" lookups.
CREATE INDEX idx_parcels_rider_id ON parcels (rider_id);
