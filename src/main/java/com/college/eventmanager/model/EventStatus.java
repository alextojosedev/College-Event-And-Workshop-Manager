package com.college.eventmanager.model;

/**
 * Lifecycle status of an event.
 */
public enum EventStatus {
    UPCOMING("Upcoming"),
    ONGOING("Ongoing"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled");

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EventStatus fromString(String text) {
        if (text == null) return UPCOMING;
        for (EventStatus s : values()) {
            if (s.name().equalsIgnoreCase(text.trim()) || s.displayName.equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        return UPCOMING;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
