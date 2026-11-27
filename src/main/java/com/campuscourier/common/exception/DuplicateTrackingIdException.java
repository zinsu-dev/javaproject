package com.campuscourier.common.exception;

/**
 * Raised when a generated tracking ID collides with an existing one.
 * Handled by GlobalExceptionHandler → HTTP 409 Conflict.
 * (Astronomically rare — 16^8 combinations — but specified, so explicit.)
 */
public class DuplicateTrackingIdException extends RuntimeException {

    public DuplicateTrackingIdException(String message) {
        super(message);
    }
}
