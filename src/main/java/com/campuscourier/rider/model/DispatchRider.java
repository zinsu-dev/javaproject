package com.campuscourier.rider.model;

import java.util.Objects;
import java.util.UUID;

/**
 * A dispatch rider who picks up and delivers parcels.
 *
 * Encapsulation: all fields are private. State can only change through
 * the constructor and the setters below, and a new rider is ALWAYS
 * created available — callers cannot forget to set it.
 */
public class DispatchRider {

    private final UUID id;
    private String fullName;
    private String phoneNumber;
    private Boolean available;

    /**
     * New rider: identity generated, marked available immediately.
     */
    public DispatchRider(String fullName, String phoneNumber) {
        this(UUID.randomUUID(), fullName, phoneNumber, Boolean.TRUE);
    }

    /**
     * Full constructor (used when rebuilding a rider loaded from storage
     * in a later phase, where id and availability are already known).
     */
    public DispatchRider(UUID id, String fullName, String phoneNumber, Boolean available) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.fullName = requireText(fullName, "fullName");
        this.phoneNumber = requireText(phoneNumber, "phoneNumber");
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
