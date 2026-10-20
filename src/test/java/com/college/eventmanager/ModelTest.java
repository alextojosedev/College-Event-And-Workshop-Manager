package com.college.eventmanager;

import com.college.eventmanager.model.*;

import java.time.LocalDateTime;

/**
 * Unit tests verifying OOP encapsulation, inheritance, and business invariants in Models.
 */
public class ModelTest {

    public static void testUserInheritanceAndPolymorphism() {
        Admin admin = new Admin(1, "admin", "hash", "Admin Full Name", "admin@college.edu", "9999999999", "Management", LocalDateTime.now());
        Student student = new Student(2, "student1", "hash", "Student Full Name", "student@college.edu", "8888888888", "CS-2024-001", "Computer Science", LocalDateTime.now());

        // Polymorphic check
        User u1 = admin;
        User u2 = student;

        assertTrue(u1.hasAdminPrivileges(), "Admin must have admin privileges");
        assertFalse(u2.hasAdminPrivileges(), "Student must not have admin privileges");

        assertEquals("Administrator", u1.getRoleDisplayName(), "Admin role display name check");
        assertEquals("Student", u2.getRoleDisplayName(), "Student role display name check");

        assertEquals("CS-2024-001", student.getStudentIdNumber(), "Student ID number check");
        assertEquals("Management", admin.getDepartment(), "Admin department check");
    }

    public static void testEventCapacityLogic() {
        Event event = new Event();
        event.setMaxCapacity(50);
        event.setAvailableSeats(50);
        event.setStatus(EventStatus.UPCOMING);

        assertTrue(event.isRegistrationOpen(), "Event should be open when available seats > 0 and UPCOMING");
        assertEquals(0, event.getBookedSeats(), "Booked seats should initially be 0");

        event.setAvailableSeats(10);
        assertEquals(40, event.getBookedSeats(), "Booked seats should be max - available = 40");

        event.setAvailableSeats(0);
        assertFalse(event.isRegistrationOpen(), "Event should not be open when available seats == 0");

        event.setStatus(EventStatus.CANCELLED);
        assertFalse(event.isRegistrationOpen(), "Cancelled event should not be open");
    }

    public static void testRegistrationStatus() {
        Registration reg = new Registration();
        reg.setStatus(RegistrationStatus.CONFIRMED);
        assertTrue(reg.isConfirmed(), "Registration should be confirmed");

        reg.setStatus(RegistrationStatus.CANCELLED);
        assertFalse(reg.isConfirmed(), "Cancelled registration should not be confirmed");
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Test Failed: " + message);
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError("Test Failed: " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null && actual == null) return;
        if (expected != null && expected.equals(actual)) return;
        throw new AssertionError("Test Failed: " + message + " (Expected: " + expected + ", Actual: " + actual + ")");
    }
}
