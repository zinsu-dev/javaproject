package com.campuscourier.rider.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.UUID;

/**
 * A dispatch rider who picks up and delivers parcels.
 *
 * Encapsulation: all fields are private. State can only change through
 * the constructors and setters below, and a new rider is ALWAYS created
 * available — callers cannot forget to set it.
 */
@Entity
@Table(name = "dispatch_riders")
public class DispatchRider {

    /**
     * Database-generated identity. NULL until the rider is saved for the
     * first time — GenerationType.UUID asks Hibernate to fill it in on
     * persist.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "available", nullable = false)
    private Boolean available;

    /** For Hibernate only. Do not use in application code. */
    protected DispatchRider() {
    }

    /**
     * New rider: marked available immediately. The id stays null until
     * the rider is saved — the database (via Hibernate) assigns it.
     */
    public DispatchRider(String fullName, String phoneNumber) {
        this.fullName = requireText(fullName, "fullName");
        this.phoneNumber = requireText(phoneNumber, "phoneNumber");
        this.available = Boolean.TRUE;
    }

    /**
     * Full constructor (used when rebuilding a rider whose id and
     * availability are already known, e.g. in tests).
     */
    public DispatchRider(UUID id, String fullName, String phoneNumber, Boolean available) {
        this(fullName, phoneNumber);
        this.id = id;
        this.available = Objects.requireNonNull(available, "available must not be null");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = requireText(fullName, "fullName");
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = requireText(phoneNumber, "phoneNumber");
    }

    public Boolean isAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = Objects.requireNonNull(available, "available must not be null");
    }

    @Override
    public String toString() {
        return "DispatchRider{fullName='" + fullName + "', available=" + available + "}";
    }
}
