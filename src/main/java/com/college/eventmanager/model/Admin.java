package com.college.eventmanager.model;

import java.time.LocalDateTime;

/**
 * Represents an Administrator with elevated privileges.
 * Inherits from User and overrides abstract methods (Polymorphism).
 */
public class Admin extends User {
    private String department; // E.g., "Event Management Cell"

    public Admin() {
        super();
        setRole("ADMIN");
    }

    public Admin(int userId, String username, String passwordHash, String fullName, 
                 String email, String phone, String department, LocalDateTime createdAt) {
        super(userId, username, passwordHash, fullName, email, phone, "ADMIN", createdAt);
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public boolean hasAdminPrivileges() {
        return true;
    }

    @Override
    public String getRoleDisplayName() {
        return "Administrator";
    }
}
