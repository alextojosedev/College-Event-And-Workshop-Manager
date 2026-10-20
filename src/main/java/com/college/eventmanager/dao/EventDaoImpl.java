package com.college.eventmanager.dao;

import com.college.eventmanager.config.DatabaseConnection;
import com.college.eventmanager.exception.DatabaseException;
import com.college.eventmanager.model.Event;
import com.college.eventmanager.model.EventCategory;
import com.college.eventmanager.model.EventStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of EventDao.
 */
public class EventDaoImpl implements EventDao {

    private Event mapRowToEvent(ResultSet rs) throws SQLException {
        int eventId = rs.getInt("event_id");
        String eventCode = rs.getString("event_code");
        String title = rs.getString("title");
        String description = rs.getString("description");
        String categoryStr = rs.getString("category");
        Timestamp dt = rs.getTimestamp("date_time");
        String venue = rs.getString("venue");
        int maxCapacity = rs.getInt("max_capacity");
        int availableSeats = rs.getInt("available_seats");
        String statusStr = rs.getString("status");
        int createdByVal = rs.getInt("created_by");
        Integer createdBy = rs.wasNull() ? null : createdByVal;
        Timestamp ca = rs.getTimestamp("created_at");

        Event event = new Event();
        event.setEventId(eventId);
        event.setEventCode(eventCode);
        event.setTitle(title);
        event.setDescription(description);
        event.setCategory(EventCategory.fromString(categoryStr));
        event.setDateTime(dt != null ? dt.toLocalDateTime() : LocalDateTime.now());
        event.setVenue(venue);
        event.setMaxCapacity(maxCapacity);
        event.setAvailableSeats(availableSeats);
        event.setStatus(EventStatus.fromString(statusStr));
        event.setCreatedBy(createdBy);
        event.setCreatedAt(ca != null ? ca.toLocalDateTime() : LocalDateTime.now());
        return event;
    }

    @Override
    public Optional<Event> findById(int eventId) {
        String sql = "SELECT * FROM events WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find event by ID: " + eventId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Event> findByCode(String eventCode) {
        String sql = "SELECT * FROM events WHERE event_code = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, eventCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find event by code: " + eventCode, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Event> findAll() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY date_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToEvent(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve all events", e);
        }
        return list;
    }

    @Override
    public List<Event> findUpcoming() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM events WHERE status = 'UPCOMING' ORDER BY date_time ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToEvent(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve upcoming events", e);
        }
        return list;
    }

    @Override
    public List<Event> search(String query, String category, EventStatus status) {
        List<Event> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM events WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append("AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ? OR LOWER(event_code) LIKE ? OR LOWER(venue) LIKE ?) ");
            String q = "%" + query.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
            params.add(q);
        }

        if (category != null && !category.trim().isEmpty() && !"All Categories".equalsIgnoreCase(category.trim())) {
            sql.append("AND category = ? ");
            params.add(category.trim());
        }

        if (status != null) {
            sql.append("AND status = ? ");
            params.add(status.name());
        }

        sql.append("ORDER BY date_time ASC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToEvent(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to search events", e);
        }
        return list;
    }

    @Override
    public Event create(Event event) {
        String sql = "INSERT INTO events (event_code, title, description, category, date_time, venue, max_capacity, available_seats, status, created_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getEventCode());
            ps.setString(2, event.getTitle());
            ps.setString(3, event.getDescription());
            ps.setString(4, event.getCategory() != null ? event.getCategory().getDisplayName() : "Workshop");
            ps.setTimestamp(5, Timestamp.valueOf(event.getDateTime()));
            ps.setString(6, event.getVenue());
            ps.setInt(7, event.getMaxCapacity());
            ps.setInt(8, event.getAvailableSeats());
            ps.setString(9, event.getStatus() != null ? event.getStatus().name() : "UPCOMING");
            if (event.getCreatedBy() != null) {
                ps.setInt(10, event.getCreatedBy());
            } else {
                ps.setNull(10, Types.INTEGER);
            }

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new DatabaseException("Failed to insert event, no rows affected.");
            }
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    event.setEventId(rs.getInt(1));
                    event.setCreatedAt(LocalDateTime.now());
                }
            }
            return event;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create event: " + event.getTitle(), e);
        }
    }

    @Override
    public boolean update(Event event) {
        String sql = "UPDATE events SET title = ?, description = ?, category = ?, date_time = ?, venue = ?, " +
                     "max_capacity = ?, available_seats = ?, status = ? WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, event.getTitle());
            ps.setString(2, event.getDescription());
            ps.setString(3, event.getCategory() != null ? event.getCategory().getDisplayName() : "Workshop");
            ps.setTimestamp(4, Timestamp.valueOf(event.getDateTime()));
            ps.setString(5, event.getVenue());
            ps.setInt(6, event.getMaxCapacity());
            ps.setInt(7, event.getAvailableSeats());
            ps.setString(8, event.getStatus() != null ? event.getStatus().name() : "UPCOMING");
            ps.setInt(9, event.getEventId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update event: " + event.getEventId(), e);
        }
    }

    @Override
    public boolean updateStatus(int eventId, EventStatus status) {
        String sql = "UPDATE events SET status = ? WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update status for event: " + eventId, e);
        }
    }

    @Override
    public boolean cancelEvent(int eventId) {
        // Atomic cancellation of event and registrations is handled in EventService using a transaction
        return updateStatus(eventId, EventStatus.CANCELLED);
    }

    @Override
    public boolean delete(int eventId) {
        String sql = "DELETE FROM events WHERE event_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete event: " + eventId, e);
        }
    }

    @Override
    public int countTotalEvents() {
        String sql = "SELECT COUNT(*) FROM events";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count events", e);
        }
        return 0;
    }

    @Override
    public int countUpcomingEvents() {
        String sql = "SELECT COUNT(*) FROM events WHERE status = 'UPCOMING'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count upcoming events", e);
        }
        return 0;
    }

    @Override
    public int sumAvailableSeats() {
        String sql = "SELECT COALESCE(SUM(available_seats), 0) FROM events WHERE status = 'UPCOMING'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to sum available seats", e);
        }
        return 0;
    }
}
