package com.college.eventmanager.dao;

import com.college.eventmanager.config.DatabaseConnection;
import com.college.eventmanager.exception.AppException;
import com.college.eventmanager.exception.DatabaseException;
import com.college.eventmanager.exception.DuplicateRegistrationException;
import com.college.eventmanager.exception.EventFullException;
import com.college.eventmanager.model.Registration;
import com.college.eventmanager.model.RegistrationStatus;

import java.security.SecureRandom;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of RegistrationDao.
 * Enforces ACID transaction boundaries for registration, seat reservation, and cancellation.
 * Supports transparent fallback to InMemoryStore when running in Offline Demo Mode.
 */
public class RegistrationDaoImpl implements RegistrationDao {

    private static final SecureRandom RANDOM = new SecureRandom();

    private String generateRegistrationCode() {
        int randomNum = 1000 + RANDOM.nextInt(9000);
        return String.format("REG-%d-%04d", Year.now().getValue(), randomNum);
    }

    private Registration mapRowToRegistration(ResultSet rs) throws SQLException {
        Registration reg = new Registration();
        reg.setRegistrationId(rs.getInt("registration_id"));
        reg.setRegistrationCode(rs.getString("registration_code"));
        reg.setUserId(rs.getInt("user_id"));
        reg.setEventId(rs.getInt("event_id"));

        Timestamp rd = rs.getTimestamp("registration_date");
        reg.setRegistrationDate(rd != null ? rd.toLocalDateTime() : LocalDateTime.now());

        String statusStr = rs.getString("status");
        reg.setStatus(RegistrationStatus.fromString(statusStr));

        Timestamp cd = rs.getTimestamp("cancellation_date");
        reg.setCancellationDate(cd != null ? cd.toLocalDateTime() : null);

        reg.setNotes(rs.getString("notes"));

        try {
            reg.setStudentName(rs.getString("full_name"));
            reg.setStudentEmail(rs.getString("email"));
            reg.setStudentIdNumber(rs.getString("student_id_number"));
            reg.setStudentDepartment(rs.getString("user_dept"));
        } catch (SQLException ignored) {}

        try {
            reg.setEventTitle(rs.getString("title"));
            reg.setEventCode(rs.getString("event_code"));
            reg.setEventCategory(rs.getString("category"));
            Timestamp edt = rs.getTimestamp("date_time");
            reg.setEventDateTime(edt != null ? edt.toLocalDateTime() : null);
            reg.setEventVenue(rs.getString("venue"));
        } catch (SQLException ignored) {}

        return reg;
    }

    private final String BASE_SELECT_JOIN = 
        "SELECT r.*, u.full_name, u.email, u.student_id_number, u.department AS user_dept, " +
        "       e.title, e.event_code, e.category, e.date_time, e.venue " +
        "FROM registrations r " +
        "JOIN users u ON r.user_id = u.user_id " +
        "JOIN events e ON r.event_id = e.event_id ";

    @Override
    public Optional<Registration> findById(int registrationId) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().findRegById(registrationId);
        }
        String sql = BASE_SELECT_JOIN + "WHERE r.registration_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, registrationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRegistration(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find registration by ID: " + registrationId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Registration> findByCode(String registrationCode) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().findRegByCode(registrationCode);
        }
        String sql = BASE_SELECT_JOIN + "WHERE r.registration_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, registrationCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRegistration(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find registration by code: " + registrationCode, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Registration> findByUserAndEvent(int userId, int eventId) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().findRegsByUserId(userId).stream()
                .filter(r -> r.getEventId() == eventId)
                .findFirst();
        }
        String sql = BASE_SELECT_JOIN + "WHERE r.user_id = ? AND r.event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToRegistration(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find registration for user and event", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Registration> findByUserId(int userId) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().findRegsByUserId(userId);
        }
        List<Registration> list = new ArrayList<>();
        String sql = BASE_SELECT_JOIN + "WHERE r.user_id = ? ORDER BY r.registration_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRegistration(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find registrations for user: " + userId, e);
        }
        return list;
    }

    @Override
    public List<Registration> findByEventId(int eventId) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().findRegsByEventId(eventId);
        }
        List<Registration> list = new ArrayList<>();
        String sql = BASE_SELECT_JOIN + "WHERE r.event_id = ? ORDER BY r.registration_date ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToRegistration(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find registrations for event: " + eventId, e);
        }
        return list;
    }

    @Override
    public List<Registration> findAll() {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().findAllRegistrations();
        }
        List<Registration> list = new ArrayList<>();
        String sql = BASE_SELECT_JOIN + "ORDER BY r.registration_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToRegistration(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list all registrations", e);
        }
        return list;
    }

    @Override
    public boolean isUserRegistered(int userId, int eventId) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().isUserRegistered(userId, eventId);
        }
        String sql = "SELECT COUNT(*) FROM registrations WHERE user_id = ? AND event_id = ? AND status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to check existing registration", e);
        }
        return false;
    }

    @Override
    public int countTotalRegistrations() {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().countTotalRegistrations();
        }
        String sql = "SELECT COUNT(*) FROM registrations";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count registrations", e);
        }
        return 0;
    }

    @Override
    public int countActiveRegistrations() {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().countActiveRegistrations();
        }
        String sql = "SELECT COUNT(*) FROM registrations WHERE status = 'CONFIRMED'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count active registrations", e);
        }
        return 0;
    }

    @Override
    public Registration registerStudentInTransaction(int userId, int eventId, String notes) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().registerStudent(userId, eventId, notes);
        }

        String checkEventSql = "SELECT available_seats, max_capacity, status, title FROM events WHERE event_id = ? FOR UPDATE";
        String checkExistingSql = "SELECT registration_id, status FROM registrations WHERE user_id = ? AND event_id = ?";
        String insertRegSql = "INSERT INTO registrations (registration_code, user_id, event_id, status, notes) VALUES (?, ?, ?, 'CONFIRMED', ?)";
        String reactivateRegSql = "UPDATE registrations SET status = 'CONFIRMED', cancellation_date = NULL, notes = ? WHERE registration_id = ?";
        String decrementSeatSql = "UPDATE events SET available_seats = available_seats - 1 WHERE event_id = ? AND available_seats > 0";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Begin ACID Transaction

            int availableSeats = 0;
            String eventStatus = "";
            String eventTitle = "";
            try (PreparedStatement ps = conn.prepareStatement(checkEventSql)) {
                ps.setInt(1, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new DatabaseException("Event not found with ID: " + eventId);
                    }
                    availableSeats = rs.getInt("available_seats");
                    eventStatus = rs.getString("status");
                    eventTitle = rs.getString("title");
                }
            }

            if (!"UPCOMING".equalsIgnoreCase(eventStatus)) {
                throw new DatabaseException("Registration is closed. Event is " + eventStatus.toLowerCase() + ".");
            }

            if (availableSeats <= 0) {
                throw new EventFullException("Sorry, '" + eventTitle + "' has reached its maximum capacity. No seats available.");
            }

            Integer existingRegId = null;
            String existingStatus = null;
            try (PreparedStatement ps = conn.prepareStatement(checkExistingSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, eventId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        existingRegId = rs.getInt("registration_id");
                        existingStatus = rs.getString("status");
                    }
                }
            }

            if (existingRegId != null && "CONFIRMED".equalsIgnoreCase(existingStatus)) {
                throw new DuplicateRegistrationException("You are already registered for this event.");
            }

            int finalRegId = 0;
            String finalCode = "";

            if (existingRegId != null && "CANCELLED".equalsIgnoreCase(existingStatus)) {
                try (PreparedStatement ps = conn.prepareStatement(reactivateRegSql)) {
                    ps.setString(1, notes);
                    ps.setInt(2, existingRegId);
                    ps.executeUpdate();
                    finalRegId = existingRegId;
                }
            } else {
                finalCode = generateRegistrationCode();
                try (PreparedStatement ps = conn.prepareStatement(insertRegSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, finalCode);
                    ps.setInt(2, userId);
                    ps.setInt(3, eventId);
                    ps.setString(4, notes);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            finalRegId = rs.getInt(1);
                        }
                    }
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(decrementSeatSql)) {
                ps.setInt(1, eventId);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    throw new EventFullException("Unable to secure seat. Event reached capacity simultaneously.");
                }
            }

            conn.commit();

            final int assignedId = finalRegId;
            return findById(assignedId).orElseGet(() -> {
                Registration r = new Registration();
                r.setRegistrationId(assignedId);
                r.setUserId(userId);
                r.setEventId(eventId);
                r.setStatus(RegistrationStatus.CONFIRMED);
                return r;
            });

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            if (e.getErrorCode() == 1062) {
                throw new DuplicateRegistrationException("You are already registered for this event.");
            }
            throw new DatabaseException("Transaction failed while registering for event: " + e.getMessage(), e);
        } catch (AppException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean cancelRegistrationInTransaction(int registrationId, int userId) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().cancelRegistration(registrationId, userId);
        }

        String checkRegSql = "SELECT r.status, r.event_id, e.max_capacity, e.available_seats " +
                             "FROM registrations r " +
                             "JOIN events e ON r.event_id = e.event_id " +
                             "WHERE r.registration_id = ? AND r.user_id = ? FOR UPDATE";
        String cancelRegSql = "UPDATE registrations SET status = 'CANCELLED', cancellation_date = CURRENT_TIMESTAMP WHERE registration_id = ?";
        String incrementSeatSql = "UPDATE events SET available_seats = available_seats + 1 WHERE event_id = ? AND available_seats < max_capacity";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int eventId = 0;
            try (PreparedStatement ps = conn.prepareStatement(checkRegSql)) {
                ps.setInt(1, registrationId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new DatabaseException("Registration not found or does not belong to the student.");
                    }
                    String currentStatus = rs.getString("status");
                    if (!"CONFIRMED".equalsIgnoreCase(currentStatus)) {
                        throw new DatabaseException("Registration is already cancelled.");
                    }
                    eventId = rs.getInt("event_id");
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(cancelRegSql)) {
                ps.setInt(1, registrationId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(incrementSeatSql)) {
                ps.setInt(1, eventId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Failed to cancel registration in transaction: " + e.getMessage(), e);
        } catch (AppException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    @Override
    public boolean cancelEventAndRegistrationsInTransaction(int eventId) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().cancelEventAndRegistrations(eventId);
        }

        String cancelEventSql = "UPDATE events SET status = 'CANCELLED' WHERE event_id = ?";
        String cancelRegistrationsSql = "UPDATE registrations SET status = 'CANCELLED', cancellation_date = CURRENT_TIMESTAMP, " +
                                        "notes = CONCAT(COALESCE(notes, ''), ' [Event Cancelled by Admin]') " +
                                        "WHERE event_id = ? AND status = 'CONFIRMED'";

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(cancelEventSql)) {
                ps.setInt(1, eventId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(cancelRegistrationsSql)) {
                ps.setInt(1, eventId);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new DatabaseException("Failed to cancel event and registrations in transaction", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }
}
