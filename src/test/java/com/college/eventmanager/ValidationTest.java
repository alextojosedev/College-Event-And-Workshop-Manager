package com.college.eventmanager;

import com.college.eventmanager.dao.EventDao;
import com.college.eventmanager.dao.RegistrationDao;
import com.college.eventmanager.dao.UserDao;
import com.college.eventmanager.exception.ValidationException;
import com.college.eventmanager.model.*;
import com.college.eventmanager.service.AuthService;
import com.college.eventmanager.service.EventService;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Unit tests verifying validation rules and business logic constraints.
 */
public class ValidationTest {

    public static void testAuthValidation() {
        // Mock UserDao
        UserDao mockDao = new MockUserDao();
        AuthService authService = new AuthService(mockDao);

        // 1. Test short username
        assertThrows(ValidationException.class, () -> {
            authService.registerStudent("ab", "password123", "Full Name", "test@college.edu", "123", "CS-001", "CS");
        }, "Should reject username shorter than 3 characters");

        // 2. Test short password
        assertThrows(ValidationException.class, () -> {
            authService.registerStudent("validuser", "123", "Full Name", "test@college.edu", "123", "CS-001", "CS");
        }, "Should reject password shorter than 6 characters");

        // 3. Test invalid email
        assertThrows(ValidationException.class, () -> {
            authService.registerStudent("validuser", "password123", "Full Name", "invalid-email", "123", "CS-001", "CS");
        }, "Should reject invalid email format");

        // 4. Test missing roll number
        assertThrows(ValidationException.class, () -> {
            authService.registerStudent("validuser", "password123", "Full Name", "test@college.edu", "123", "", "CS");
        }, "Should reject empty student roll number");
    }

    public static void testEventValidation() {
        EventService eventService = new EventService(new MockEventDao(), new MockRegistrationDao(), new MockUserDao());

        // 1. Test empty title
        Event e1 = new Event();
        e1.setEventCode("EVT-999");
        e1.setTitle("");
        e1.setDateTime(LocalDateTime.now().plusDays(2));
        e1.setVenue("Auditorium");
        e1.setMaxCapacity(50);

        assertThrows(ValidationException.class, () -> {
            eventService.createEvent(e1);
        }, "Should reject event with empty title");

        // 2. Test negative capacity
        Event e2 = new Event();
        e2.setEventCode("EVT-999");
        e2.setTitle("Valid Title");
        e2.setDateTime(LocalDateTime.now().plusDays(2));
        e2.setVenue("Auditorium");
        e2.setMaxCapacity(-5);

        assertThrows(ValidationException.class, () -> {
            eventService.createEvent(e2);
        }, "Should reject event with negative capacity");
    }

    private static void assertThrows(Class<? extends Throwable> expected, Runnable action, String message) {
        try {
            action.run();
            throw new AssertionError("Test Failed: Expected " + expected.getSimpleName() + " but no exception was thrown. (" + message + ")");
        } catch (Throwable t) {
            if (!expected.isInstance(t)) {
                throw new AssertionError("Test Failed: Expected " + expected.getSimpleName() + " but caught " + t.getClass().getSimpleName() + " (" + message + ")");
            }
        }
    }

    // Lightweight in-memory Mocks for isolated business logic validation
    private static class MockUserDao implements UserDao {
        private final Map<String, User> byUsername = new HashMap<>();
        private final Map<String, User> byEmail = new HashMap<>();

        @Override public Optional<User> findById(int userId) { return Optional.empty(); }
        @Override public Optional<User> findByUsername(String u) { return Optional.ofNullable(byUsername.get(u)); }
        @Override public Optional<User> findByEmail(String e) { return Optional.ofNullable(byEmail.get(e)); }
        @Override public User create(User user) {
            user.setUserId(1);
            byUsername.put(user.getUsername(), user);
            byEmail.put(user.getEmail(), user);
            return user;
        }
        @Override public boolean update(User user) { return true; }
        @Override public boolean delete(int userId) { return true; }
        @Override public List<Student> findAllStudents() { return new ArrayList<>(); }
        @Override public int countStudents() { return byUsername.size(); }
    }

    private static class MockEventDao implements EventDao {
        @Override public Optional<Event> findById(int id) { return Optional.empty(); }
        @Override public Optional<Event> findByCode(String code) { return Optional.empty(); }
        @Override public List<Event> findAll() { return new ArrayList<>(); }
        @Override public List<Event> findUpcoming() { return new ArrayList<>(); }
        @Override public List<Event> search(String q, String c, EventStatus s) { return new ArrayList<>(); }
        @Override public Event create(Event event) { event.setEventId(1); return event; }
        @Override public boolean update(Event event) { return true; }
        @Override public boolean updateStatus(int id, EventStatus s) { return true; }
        @Override public boolean cancelEvent(int id) { return true; }
        @Override public boolean delete(int id) { return true; }
        @Override public int countTotalEvents() { return 0; }
        @Override public int countUpcomingEvents() { return 0; }
        @Override public int sumAvailableSeats() { return 0; }
    }

    private static class MockRegistrationDao implements RegistrationDao {
        @Override public Optional<Registration> findById(int id) { return Optional.empty(); }
        @Override public Optional<Registration> findByCode(String code) { return Optional.empty(); }
        @Override public Optional<Registration> findByUserAndEvent(int u, int e) { return Optional.empty(); }
        @Override public List<Registration> findByUserId(int u) { return new ArrayList<>(); }
        @Override public List<Registration> findByEventId(int e) { return new ArrayList<>(); }
        @Override public List<Registration> findAll() { return new ArrayList<>(); }
        @Override public boolean isUserRegistered(int u, int e) { return false; }
        @Override public int countTotalRegistrations() { return 0; }
        @Override public int countActiveRegistrations() { return 0; }
        @Override public Registration registerStudentInTransaction(int u, int e, String n) { return new Registration(); }
        @Override public boolean cancelRegistrationInTransaction(int r, int u) { return true; }
        @Override public boolean cancelEventAndRegistrationsInTransaction(int e) { return true; }
    }
}
