package com.college.eventmanager.model;

import java.time.LocalDateTime;

/**
 * Represents a Student user.
 * Inherits from User and provides student-specific attributes.
 */
public class Student extends User {
    private String studentIdNumber; // e.g. CS-2024-001
    private String department;      // e.g. Computer Science & Engineering

    public Student() {
        super();
        setRole("STUDENT");
    }

    public Student(int userId, String username, String passwordHash, String fullName, 
                   String email, String phone, String studentIdNumber, String department, 
                   LocalDateTime createdAt) {
        super(userId, username, passwordHash, fullName, email, phone, "STUDENT", createdAt);
        this.studentIdNumber = studentIdNumber;
        this.department = department;
    }

    public String getStudentIdNumber() {
        return studentIdNumber;
    }

    public void setStudentIdNumber(String studentIdNumber) {
        this.studentIdNumber = studentIdNumber;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public boolean hasAdminPrivileges() {
        return false;
    }

    @Override
    public String getRoleDisplayName() {
        return "Student";
    }
}
