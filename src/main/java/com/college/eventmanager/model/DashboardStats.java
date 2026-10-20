package com.college.eventmanager.model;

/**
 * Data Transfer Object (DTO) holding aggregated statistics for dashboards.
 */
public class DashboardStats {
    private int totalEvents;
    private int upcomingEvents;
    private int totalRegistrations;
    private int totalAvailableSeats;
    private int totalStudents;
    private int cancelledEvents;

    public DashboardStats() {
    }

    public DashboardStats(int totalEvents, int upcomingEvents, int totalRegistrations, 
                          int totalAvailableSeats, int totalStudents, int cancelledEvents) {
        this.totalEvents = totalEvents;
        this.upcomingEvents = upcomingEvents;
        this.totalRegistrations = totalRegistrations;
        this.totalAvailableSeats = totalAvailableSeats;
        this.totalStudents = totalStudents;
        this.cancelledEvents = cancelledEvents;
    }

    public int getTotalEvents() {
        return totalEvents;
    }

    public void setTotalEvents(int totalEvents) {
        this.totalEvents = totalEvents;
    }

    public int getUpcomingEvents() {
        return upcomingEvents;
    }

    public void setUpcomingEvents(int upcomingEvents) {
        this.upcomingEvents = upcomingEvents;
    }

    public int getTotalRegistrations() {
        return totalRegistrations;
    }

    public void setTotalRegistrations(int totalRegistrations) {
        this.totalRegistrations = totalRegistrations;
    }

    public int getTotalAvailableSeats() {
        return totalAvailableSeats;
    }

    public void setTotalAvailableSeats(int totalAvailableSeats) {
        this.totalAvailableSeats = totalAvailableSeats;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getCancelledEvents() {
        return cancelledEvents;
    }

    public void setCancelledEvents(int cancelledEvents) {
        this.cancelledEvents = cancelledEvents;
    }
}
