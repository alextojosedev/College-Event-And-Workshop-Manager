package com.college.eventmanager.exception;

/**
 * Thrown when business rules or user input validation fails.
 */
public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(message);
    }
}
