package com.college.eventmanager.model;

/**
 * Status of a student's event registration.
 */
public enum RegistrationStatus {
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled");

    private final String displayName;

    RegistrationStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static RegistrationStatus fromString(String text) {
        if (text == null) return CONFIRMED;
        for (RegistrationStatus s : values()) {
            if (s.name().equalsIgnoreCase(text.trim()) || s.displayName.equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        return CONFIRMED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
