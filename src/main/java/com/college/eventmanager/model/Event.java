package com.college.eventmanager.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a College Event or Workshop.
 */
public class Event {
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int eventId;
    private String eventCode;
    private String title;
    private String description;
    private EventCategory category;
    private LocalDateTime dateTime;
    private String venue;
    private int maxCapacity;
    private int availableSeats;
    private EventStatus status;
    private Integer createdBy;
    private LocalDateTime createdAt;

    public Event() {
        this.status = EventStatus.UPCOMING;
        this.category = EventCategory.WORKSHOP;
    }

    public Event(int eventId, String eventCode, String title, String description,
                 EventCategory category, LocalDateTime dateTime, String venue,
                 int maxCapacity, int availableSeats, EventStatus status,
                 Integer createdBy, LocalDateTime createdAt) {
        this.eventId = eventId;
        this.eventCode = eventCode;
        this.title = title;
        this.description = description;
        this.category = category != null ? category : EventCategory.WORKSHOP;
        this.dateTime = dateTime;
        this.venue = venue;
        this.maxCapacity = maxCapacity;
        this.availableSeats = availableSeats;
        this.status = status != null ? status : EventStatus.UPCOMING;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public boolean isRegistrationOpen() {
        return status == EventStatus.UPCOMING && availableSeats > 0;
    }

    public int getBookedSeats() {
        return Math.max(0, maxCapacity - availableSeats);
    }

    public String getFormattedDateTime() {
        return dateTime != null ? dateTime.format(DISPLAY_FORMATTER) : "TBD";
    }

    // Getters and Setters
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EventCategory getCategory() {
        return category;
    }

    public void setCategory(EventCategory category) {
        this.category = category;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public EventStatus getStatus() {
        return status;
    }

    public void setStatus(EventStatus status) {
        this.status = status;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "[" + eventCode + "] " + title + " (" + getFormattedDateTime() + ")";
    }
}
