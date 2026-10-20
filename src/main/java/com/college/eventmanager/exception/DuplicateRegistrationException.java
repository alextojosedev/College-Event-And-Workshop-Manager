package com.college.eventmanager.exception;

/**
 * Thrown when a student attempts to register for an event they are already registered for.
 */
public class DuplicateRegistrationException extends AppException {
    public DuplicateRegistrationException(String message) {
        super(message);
    }
}
