package com.campuscourier.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Converts exceptions into clean, consistent JSON instead of HTML error
 * pages. @RestControllerAdvice = "apply this to every controller".
 *
 * Status map:
 * - ResourceNotFoundException      → 404 (unknown id / tracking id)
 * - @Valid failures                → 400 (blank names, bad weight...)
 * - Unreadable JSON / bad enum     → 400 (e.g. parcelType "EXPRESS")
 * - Bad path variable type         → 400 (e.g. id "not-a-uuid")
 * - IllegalArgumentException       → 400 (domain rule broken)
 * - DuplicateTrackingIdException   → 409 (tracking id collision)
 * - anything else                  → 500 with a SAFE generic message
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, details, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Malformed JSON request. parcelType must be one of DOCUMENT, FRAGILE, BULK.",
                request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Invalid value for '" + ex.getName() + "': " + ex.getValue(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleBadRequest(
            IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler({DuplicateTrackingIdException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> handleConflict(
            RuntimeException ex, HttpServletRequest request) {
        // The database unique constraint is the final backstop: even if a
        // tracking-id collision slipped past the service check, the client
        // still gets a clean 409 instead of a raw SQL error.
        String message = ex instanceof DuplicateTrackingIdException
                ? ex.getMessage()
                : "Duplicate value violates a uniqueness rule (e.g. tracking ID already exists).";
        return build(HttpStatus.CONFLICT, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpected(
            Exception ex, HttpServletRequest request) {
        // Full detail goes to the server log only — clients get a safe,
        // generic message with no internals.
        log.error("Unexpected error handling {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.", request);
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(
                status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }
}
