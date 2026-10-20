package com.college.eventmanager.exception;

/**
 * Thrown when user credentials cannot be authenticated.
 */
public class AuthenticationException extends AppException {
    public AuthenticationException(String message) {
        super(message);
    }
}
