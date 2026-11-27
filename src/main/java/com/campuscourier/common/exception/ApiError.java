package com.campuscourier.common.exception;

import java.time.LocalDateTime;

/**
 * Shape of every error the API returns, so the React frontend
 * can always read "message" the same way.
 */
public class ApiError {

    private int status;
    private String message;
    private LocalDateTime timestamp;

    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
