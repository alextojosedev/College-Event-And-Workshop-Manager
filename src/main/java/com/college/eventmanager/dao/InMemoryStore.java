package com.college.eventmanager.dao;

import com.college.eventmanager.exception.DuplicateRegistrationException;
import com.college.eventmanager.exception.EventFullException;
import com.college.eventmanager.exception.ValidationException;
import com.college.eventmanager.model.*;
import com.college.eventmanager.service.PasswordUtil;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory thread-safe mock store used when MySQL is offline.
 * Allows evaluators and students to test all GUI flows, registrations, and capacity logic
 * even before local MySQL or XAMPP is started.
 */
public class InMemoryStore {
    private static final InMemoryStore INSTANCE = new InMemoryStore();
    private static final SecureRandom RANDOM = new SecureRandom();

    private final Map<Integer, User> users = new ConcurrentHashMap<>();
    private final Map<Integer, Event> events = new ConcurrentHashMap<>();
    private final Map<Integer, Registration> registrations = new ConcurrentHashMap<>();

    private final AtomicInteger userIdCounter = new AtomicInteger(10);
    private final AtomicInteger eventIdCounter = new AtomicInteger(10);
    private final AtomicInteger regIdCounter = new AtomicInteger(10);

    private InMemoryStore() {
        seedSampleData();
    }

    public static InMemoryStore getInstance() {
        return INSTANCE;
    }

    private void seedSampleData() {
        // Sample Admin
        Admin admin = new Admin(1, "admin", PasswordUtil.hashPassword("admin123"), 
            "Dr. Sarah Jenkins", "admin@college.edu", "9876543210", "Event Management Cell", LocalDateTime.now());
        users.put(1, admin);

        // Sample Students
        Student s1 = new Student(2, "student1", PasswordUtil.hashPassword("student123"), 
            "Alex Rivera", "alex.rivera@student.college.edu", "9123456780", "CS-2024-001", "Computer Science & Engineering", LocalDateTime.now());
        Student s2 = new Student(3, "student2", PasswordUtil.hashPassword("student123"), 
            "Maya Patel", "maya.patel@student.college.edu", "9123456781", "EC-2024-042", "Electronics & Communication", LocalDateTime.now());
        Student s3 = new Student(4, "student3", PasswordUtil.hashPassword("student123"), 
            "David Kim", "david.kim@student.college.edu", "9123456782", "ME-2024-015", "Mechanical Engineering", LocalDateTime.now());
        Student s4 = new Student(5, "alextojosedev", PasswordUtil.hashPassword("student123"), 
            "Alexto Jose", "alextojosedev@gmail.com", "9876543219", "CS-2024-007", "Computer Science & Engineering", LocalDateTime.now());

        users.put(2, s1);
        users.put(3, s2);
        users.put(4, s3);
        users.put(5, s4);

        // Sample Events
        Event e1 = new Event(1, "EVT-AI-2026", "National AI & Deep Learning Summit", 
            "Two-day hands-on summit featuring industry keynote speakers from leading tech firms, neural network design, and generative AI labs.",
            EventCategory.WORKSHOP, LocalDateTime.now().plusDays(10).withHour(9).withMinute(30), 
            "APJ Abdul Kalam Auditorium", 60, 58, EventStatus.UPCOMING, 1, LocalDateTime.now());

        Event e2 = new Event(2, "EVT-HACK-2026", "HackNova: 24-Hour Inter-College Hackathon", 
            "Annual university hackathon solving real-world challenges in smart healthcare, green energy, and fintech. Exciting cash prizes!",
            EventCategory.HACKATHON, LocalDateTime.now().plusDays(15).withHour(10).withMinute(0), 
            "Campus Innovation Center, Lab 3", 80, 79, EventStatus.UPCOMING, 1, LocalDateTime.now());

        Event e3 = new Event(3, "EVT-CYBER-2026", "Ethical Hacking & Network Defense Bootcamp", 
            "Intensive cybersecurity boot camp covering penetration testing, CTF challenges, and secure application development.",
            EventCategory.WORKSHOP, LocalDateTime.now().plusDays(22).withHour(14).withMinute(0), 
            "Seminar Hall B, CSE Block", 40, 39, EventStatus.UPCOMING, 1, LocalDateTime.now());

        Event e4 = new Event(4, "EVT-ROBO-2026", "Autonomous Robotics & IoT Hands-on Expo", 
            "Design, build, and race micro-controllers and IoT-connected rovers. Hardware kits provided.",
            EventCategory.TECHNICAL_FEST, LocalDateTime.now().plusDays(30).withHour(10).withMinute(30), 
            "Robotics Research Lab", 30, 30, EventStatus.UPCOMING, 1, LocalDateTime.now());

        Event e5 = new Event(5, "EVT-CULT-2026", "Sanskriti: Annual Cultural Gala", 
            "Grand cultural festival with battle of bands, classical and street dance, fine arts, and dramatics.",
            EventCategory.CULTURAL, LocalDateTime.now().plusDays(45).withHour(17).withMinute(0), 
            "Open Air Amphitheatre", 200, 200, EventStatus.UPCOMING, 1, LocalDateTime.now());

        events.put(1, e1);
        events.put(2, e2);
        events.put(3, e3);
        events.put(4, e4);
        events.put(5, e5);

        // Sample Registrations
        addSampleReg(1, "REG-2026-0001", 2, 1, "Early bird registration");
        addSampleReg(2, "REG-2026-0002", 3, 1, "Registered for AI workshop");
        addSampleReg(3, "REG-2026-0003", 2, 2, "Team lead entry");
        addSampleReg(4, "REG-2026-0004", 4, 3, "Cybersecurity registration");
    }

    private void addSampleReg(int id, String code, int userId, int eventId, String notes) {
        Registration r = new Registration();
        r.setRegistrationId(id);
        r.setRegistrationCode(code);
        r.setUserId(userId);
        r.setEventId(eventId);
        r.setStatus(RegistrationStatus.CONFIRMED);
        r.setRegistrationDate(LocalDateTime.now().minusDays(2));
        r.setNotes(notes);

        User u = users.get(userId);
        if (u != null) {
            r.setStudentName(u.getFullName());
            r.setStudentEmail(u.getEmail());
            if (u instanceof Student s) {
                r.setStudentIdNumber(s.getStudentIdNumber());
                r.setStudentDepartment(s.getDepartment());
            }
        }

        Event e = events.get(eventId);
        if (e != null) {
            r.setEventTitle(e.getTitle());
            r.setEventCode(e.getEventCode());
            r.setEventCategory(e.getCategory().getDisplayName());
            r.setEventDateTime(e.getDateTime());
            r.setEventVenue(e.getVenue());
        }

        registrations.put(id, r);
    }

    // --- Users ---
    public Optional<User> findUserById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<User> findUserByUsername(String u) {
        return users.values().stream()
            .filter(user -> user.getUsername().equalsIgnoreCase(u.trim()))
            .findFirst();
    }

    public Optional<User> findUserByEmail(String e) {
        return users.values().stream()
            .filter(user -> user.getEmail().equalsIgnoreCase(e.trim()))
            .findFirst();
    }

    public User createUser(User user) {
        int id = userIdCounter.incrementAndGet();
        user.setUserId(id);
        user.setCreatedAt(LocalDateTime.now());
        users.put(id, user);
        return user;
    }

    public boolean updateUser(User user) {
        if (users.containsKey(user.getUserId())) {
            users.put(user.getUserId(), user);
            return true;
        }
        return false;
    }

    public boolean deleteUser(int id) {
        return users.remove(id) != null;
    }

    public List<Student> findAllStudents() {
        List<Student> list = new ArrayList<>();
        for (User u : users.values()) {
            if (u instanceof Student s) list.add(s);
        }
        list.sort(Comparator.comparing(User::getFullName));
        return list;
    }

    public int countStudents() {
        return (int) users.values().stream().filter(u -> u instanceof Student).count();
    }

    // --- Events ---
    public Optional<Event> findEventById(int id) {
        return Optional.ofNullable(events.get(id));
    }

    public Optional<Event> findEventByCode(String code) {
        return events.values().stream()
            .filter(e -> e.getEventCode().equalsIgnoreCase(code.trim()))
            .findFirst();
    }

    public List<Event> findAllEvents() {
        List<Event> list = new ArrayList<>(events.values());
        list.sort(Comparator.comparing(Event::getDateTime));
        return list;
    }

    public List<Event> findUpcomingEvents() {
        List<Event> list = new ArrayList<>();
        for (Event e : events.values()) {
            if (e.getStatus() == EventStatus.UPCOMING) list.add(e);
        }
        list.sort(Comparator.comparing(Event::getDateTime));
        return list;
    }

    public List<Event> searchEvents(String query, String category, EventStatus status) {
        List<Event> list = new ArrayList<>();
        String q = query != null ? query.trim().toLowerCase() : "";
        for (Event e : events.values()) {
            if (!q.isEmpty()) {
                boolean match = e.getTitle().toLowerCase().contains(q) ||
                    e.getEventCode().toLowerCase().contains(q) ||
                    e.getVenue().toLowerCase().contains(q) ||
                    (e.getDescription() != null && e.getDescription().toLowerCase().contains(q));
                if (!match) continue;
            }
            if (category != null && !category.isEmpty() && !"All Categories".equalsIgnoreCase(category)) {
                if (!e.getCategory().getDisplayName().equalsIgnoreCase(category)) continue;
            }
            if (status != null) {
                if (e.getStatus() != status) continue;
            }
            list.add(e);
        }
        list.sort(Comparator.comparing(Event::getDateTime));
        return list;
    }

    public Event createEvent(Event e) {
        int id = eventIdCounter.incrementAndGet();
        e.setEventId(id);
        e.setCreatedAt(LocalDateTime.now());
        events.put(id, e);
        return e;
    }

    public boolean updateEvent(Event e) {
        if (events.containsKey(e.getEventId())) {
            events.put(e.getEventId(), e);
            return true;
        }
        return false;
    }

    public boolean cancelEvent(int id) {
        return cancelEventAndRegistrations(id);
    }

    public boolean deleteEvent(int id) {
        registrations.values().removeIf(r -> r.getEventId() == id);
        return events.remove(id) != null;
    }

    public int countTotalEvents() {
        return events.size();
    }

    public int countUpcomingEvents() {
        return (int) events.values().stream().filter(e -> e.getStatus() == EventStatus.UPCOMING).count();
    }

    public int sumAvailableSeats() {
        return events.values().stream()
            .filter(e -> e.getStatus() == EventStatus.UPCOMING)
            .mapToInt(Event::getAvailableSeats).sum();
    }

    // --- Registrations ---
    public Optional<Registration> findRegById(int id) {
        return Optional.ofNullable(registrations.get(id));
    }

    public Optional<Registration> findRegByCode(String code) {
        return registrations.values().stream()
            .filter(r -> r.getRegistrationCode().equalsIgnoreCase(code.trim()))
            .findFirst();
    }

    public List<Registration> findRegsByUserId(int userId) {
        List<Registration> list = new ArrayList<>();
        for (Registration r : registrations.values()) {
            if (r.getUserId() == userId) list.add(r);
        }
        list.sort((a, b) -> b.getRegistrationDate().compareTo(a.getRegistrationDate()));
        return list;
    }

    public List<Registration> findRegsByEventId(int eventId) {
        List<Registration> list = new ArrayList<>();
        for (Registration r : registrations.values()) {
            if (r.getEventId() == eventId) list.add(r);
        }
        list.sort(Comparator.comparing(Registration::getRegistrationDate));
        return list;
    }

    public List<Registration> findAllRegistrations() {
        List<Registration> list = new ArrayList<>(registrations.values());
        list.sort((a, b) -> b.getRegistrationDate().compareTo(a.getRegistrationDate()));
        return list;
    }

    public boolean isUserRegistered(int userId, int eventId) {
        return registrations.values().stream()
            .anyMatch(r -> r.getUserId() == userId && r.getEventId() == eventId && r.getStatus() == RegistrationStatus.CONFIRMED);
    }

    public int countTotalRegistrations() {
        return registrations.size();
    }

    public int countActiveRegistrations() {
        return (int) registrations.values().stream()
            .filter(r -> r.getStatus() == RegistrationStatus.CONFIRMED).count();
    }

    public synchronized Registration registerStudent(int userId, int eventId, String notes) {
        Event event = events.get(eventId);
        if (event == null) throw new ValidationException("Event not found.");
        if (event.getStatus() != EventStatus.UPCOMING) throw new ValidationException("Registration is closed.");
        if (event.getAvailableSeats() <= 0) {
            throw new EventFullException("Sorry, '" + event.getTitle() + "' has reached its maximum capacity.");
        }

        for (Registration r : registrations.values()) {
            if (r.getUserId() == userId && r.getEventId() == eventId && r.getStatus() == RegistrationStatus.CONFIRMED) {
                throw new DuplicateRegistrationException("You are already registered for this event.");
            }
        }

        // Decrement available seats
        event.setAvailableSeats(event.getAvailableSeats() - 1);

        int id = regIdCounter.incrementAndGet();
        int randomCode = 1000 + RANDOM.nextInt(9000);
        String code = String.format("REG-%d-%04d", Year.now().getValue(), randomCode);

        addSampleReg(id, code, userId, eventId, notes);
        return registrations.get(id);
    }

    public synchronized boolean cancelRegistration(int regId, int userId) {
        Registration reg = registrations.get(regId);
        if (reg == null || reg.getUserId() != userId) {
            throw new ValidationException("Registration not found or unauthorized.");
        }
        if (reg.getStatus() != RegistrationStatus.CONFIRMED) {
            throw new ValidationException("Registration is already cancelled.");
        }

        reg.setStatus(RegistrationStatus.CANCELLED);
        reg.setCancellationDate(LocalDateTime.now());

        Event event = events.get(reg.getEventId());
        if (event != null && event.getAvailableSeats() < event.getMaxCapacity()) {
            event.setAvailableSeats(event.getAvailableSeats() + 1);
        }
        return true;
    }

    public synchronized boolean cancelEventAndRegistrations(int eventId) {
        Event event = events.get(eventId);
        if (event == null) return false;

        event.setStatus(EventStatus.CANCELLED);
        for (Registration r : registrations.values()) {
            if (r.getEventId() == eventId && r.getStatus() == RegistrationStatus.CONFIRMED) {
                r.setStatus(RegistrationStatus.CANCELLED);
                r.setCancellationDate(LocalDateTime.now());
                r.setNotes((r.getNotes() != null ? r.getNotes() + " " : "") + "[Event Cancelled by Admin]");
            }
        }
        return true;
    }
}
