package com.college.eventmanager.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model class representing a Student's Registration for an Event.
 */
public class Registration {
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private int registrationId;
    private String registrationCode;
    private int userId;
    private int eventId;
    private LocalDateTime registrationDate;
    private RegistrationStatus status;
    private LocalDateTime cancellationDate;
    private String notes;

    // Associated student and event details for convenient UI display and reporting
    private String studentName;
    private String studentEmail;
    private String studentIdNumber;
    private String studentDepartment;

    private String eventTitle;
    private String eventCode;
    private String eventCategory;
    private LocalDateTime eventDateTime;
    private String eventVenue;

    public Registration() {
        this.status = RegistrationStatus.CONFIRMED;
        this.registrationDate = LocalDateTime.now();
    }

    public Registration(int registrationId, String registrationCode, int userId, int eventId,
                        LocalDateTime registrationDate, RegistrationStatus status,
                        LocalDateTime cancellationDate, String notes) {
        this.registrationId = registrationId;
        this.registrationCode = registrationCode;
        this.userId = userId;
        this.eventId = eventId;
        this.registrationDate = registrationDate;
        this.status = status != null ? status : RegistrationStatus.CONFIRMED;
        this.cancellationDate = cancellationDate;
        this.notes = notes;
    }

    public boolean isConfirmed() {
        return status == RegistrationStatus.CONFIRMED;
    }

    public String getFormattedRegistrationDate() {
        return registrationDate != null ? registrationDate.format(DISPLAY_FORMATTER) : "N/A";
    }

    public String getFormattedEventDateTime() {
        return eventDateTime != null ? eventDateTime.format(DISPLAY_FORMATTER) : "N/A";
    }

    // Getters and Setters
    public int getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(int registrationId) {
        this.registrationId = registrationId;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    public RegistrationStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationStatus status) {
        this.status = status;
    }

    public LocalDateTime getCancellationDate() {
        return cancellationDate;
    }

    public void setCancellationDate(LocalDateTime cancellationDate) {
        this.cancellationDate = cancellationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentIdNumber() {
        return studentIdNumber;
    }

    public void setStudentIdNumber(String studentIdNumber) {
        this.studentIdNumber = studentIdNumber;
    }

    public String getStudentDepartment() {
        return studentDepartment;
    }

    public void setStudentDepartment(String studentDepartment) {
        this.studentDepartment = studentDepartment;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public LocalDateTime getEventDateTime() {
        return eventDateTime;
    }

    public void setEventDateTime(LocalDateTime eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    public String getEventVenue() {
        return eventVenue;
    }

    public void setEventVenue(String eventVenue) {
        this.eventVenue = eventVenue;
    }

    @Override
    public String toString() {
        return "Registration [" + registrationCode + "] Student: " + studentName + ", Event: " + eventTitle;
    }
}
