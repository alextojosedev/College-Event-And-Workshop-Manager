package com.college.eventmanager.exception;

/**
 * Thrown when an event or workshop has reached its maximum capacity.
 */
public class EventFullException extends AppException {
    public EventFullException(String message) {
        super(message);
    }
}
