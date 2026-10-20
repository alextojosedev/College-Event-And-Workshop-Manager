package com.college.eventmanager.model;

/**
 * Categories for college events and workshops.
 */
public enum EventCategory {
    WORKSHOP("Workshop"),
    HACKATHON("Hackathon"),
    TECHNICAL_FEST("Technical Fest"),
    SEMINAR("Seminar"),
    CULTURAL("Cultural"),
    SPORTS("Sports"),
    GUEST_LECTURE("Guest Lecture");

    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static EventCategory fromString(String text) {
        if (text == null) return WORKSHOP;
        for (EventCategory c : values()) {
            if (c.name().equalsIgnoreCase(text.trim()) || c.displayName.equalsIgnoreCase(text.trim())) {
                return c;
            }
        }
        return WORKSHOP;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
