package com.campuscourier.common.exception;

/**
 * Thrown when a parcel (or any resource) is not found.
 * Handled by GlobalExceptionHandler -> returns HTTP 404 JSON.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
