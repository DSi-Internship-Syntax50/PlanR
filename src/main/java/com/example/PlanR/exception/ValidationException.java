package com.example.PlanR.exception;

/**
 * Thrown when a business validation rule is violated.
 * Replaces generic RuntimeException for validation-related errors.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }
}
