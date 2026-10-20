package com.college.eventmanager.dao;

import com.college.eventmanager.config.DatabaseConnection;
import com.college.eventmanager.exception.DatabaseException;
import com.college.eventmanager.model.Admin;
import com.college.eventmanager.model.Student;
import com.college.eventmanager.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC implementation of UserDao.
 * Uses PreparedStatements for all operations to prevent SQL Injection.
 * Supports transparent fallback to InMemoryStore when running in Offline Demo Mode.
 */
public class UserDaoImpl implements UserDao {

    private User mapRowToUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String username = rs.getString("username");
        String passwordHash = rs.getString("password_hash");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String role = rs.getString("role");
        String studentIdNumber = rs.getString("student_id_number");
        String department = rs.getString("department");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toLocalDateTime() : LocalDateTime.now();

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(userId, username, passwordHash, fullName, email, phone, department, createdAt);
        } else {
            return new Student(userId, username, passwordHash, fullName, email, phone, studentIdNumber, department, createdAt);
        }
    }

    @Override
    public Optional<User> findById(int userId) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().findUserById(userId);
        }
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by ID: " + userId, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().findUserByUsername(username);
        }
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by username: " + username, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().findUserByEmail(email);
        }
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRowToUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find user by email: " + email, e);
        }
        return Optional.empty();
    }

    @Override
    public User create(User user) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().createUser(user);
        }
        String sql = "INSERT INTO users (username, password_hash, full_name, email, phone, role, student_id_number, department) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole());

            if (user instanceof Student s) {
                ps.setString(7, s.getStudentIdNumber());
                ps.setString(8, s.getDepartment());
            } else if (user instanceof Admin a) {
                ps.setString(7, null);
                ps.setString(8, a.getDepartment());
            } else {
                ps.setString(7, null);
                ps.setString(8, null);
            }

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new DatabaseException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setUserId(generatedKeys.getInt(1));
                    user.setCreatedAt(LocalDateTime.now());
                } else {
                    throw new DatabaseException("Creating user failed, no ID obtained.");
                }
            }
            return user;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create user: " + user.getUsername(), e);
        }
    }

    @Override
    public boolean update(User user) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().updateUser(user);
        }
        String sql = "UPDATE users SET full_name = ?, email = ?, phone = ?, student_id_number = ?, department = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPhone());
            if (user instanceof Student s) {
                ps.setString(4, s.getStudentIdNumber());
                ps.setString(5, s.getDepartment());
            } else {
                ps.setString(4, null);
                ps.setString(5, null);
            }
            ps.setInt(6, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to update user: " + user.getUserId(), e);
        }
    }

    @Override
    public boolean delete(int userId) {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().deleteUser(userId);
        }
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete user: " + userId, e);
        }
    }

    @Override
    public List<Student> findAllStudents() {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().findAllStudents();
        }
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'STUDENT' ORDER BY full_name ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = mapRowToUser(rs);
                if (u instanceof Student s) {
                    list.add(s);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to list students", e);
        }
        return list;
    }

    @Override
    public int countStudents() {
        if (DatabaseConnection.isDemoMode()) {
            return InMemoryStore.getInstance().countStudents();
        }
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'STUDENT'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count students", e);
        }
        return 0;
    }
}
