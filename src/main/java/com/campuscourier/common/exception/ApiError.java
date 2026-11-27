package com.campuscourier.common.exception;

import java.time.LocalDateTime;

/**
 * Shape of EVERY error the API returns, so the React frontend can
 * always read the same fields. Example (404):
 * {
 *   "timestamp": "2026-11-27T10:15:30",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Parcel not found with id: ...",
 *   "path": "/api/parcels/..."
 * }
 * Stack traces and internal details are NEVER included.
 */
public class ApiError {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    public ApiError(int status, String error, String message, String path) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }
}
