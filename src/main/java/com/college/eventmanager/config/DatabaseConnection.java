package com.college.eventmanager.config;

import com.college.eventmanager.exception.DatabaseException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton / Utility class managing JDBC database connectivity.
 * Implements defensive checks, driver registration, and diagnosis.
 */
public class DatabaseConnection {
    private static final DbConfig CONFIG = new DbConfig();
    private static boolean driverLoaded = false;
    private static boolean demoMode = false;

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found on classpath: " + e.getMessage());
        }
    }

    private DatabaseConnection() {
        // Prevent instantiation
    }

    public static boolean isDemoMode() {
        return demoMode;
    }

    public static void setDemoMode(boolean mode) {
        demoMode = mode;
    }

    public static DbConfig getConfig() {
        return CONFIG;
    }

    public static boolean isDriverLoaded() {
        return driverLoaded;
    }

    /**
     * Gets a new JDBC Connection using current configuration.
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            throw new SQLException("MySQL JDBC Driver (com.mysql.cj.Driver) is missing from classpath.");
        }
        return DriverManager.getConnection(
            CONFIG.getJdbcUrl(),
            CONFIG.getUser(),
            CONFIG.getPassword()
        );
    }

    /**
     * Diagnostic result class for connection checks.
     */
    public static class ConnectionStatus {
        private final boolean connected;
        private final String message;
        private final String details;

        public ConnectionStatus(boolean connected, String message, String details) {
            this.connected = connected;
            this.message = message;
            this.details = details;
        }

        public boolean isConnected() { return connected; }
        public String getMessage() { return message; }
        public String getDetails() { return details; }
    }

    /**
     * Tests the database connection and returns diagnostic details.
     */
    public static ConnectionStatus testConnection() {
        if (!driverLoaded) {
            return new ConnectionStatus(false, "MySQL JDBC Driver is missing.", 
                "Please ensure mysql-connector-j jar is present in the classpath.");
        }

        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                // Verify basic statement execution
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("SELECT 1");
                }
                return new ConnectionStatus(true, "Successfully connected to MySQL!", 
                    "Host: " + CONFIG.getHost() + ":" + CONFIG.getPort() + " | Database: " + CONFIG.getName());
            }
            return new ConnectionStatus(false, "Connection returned null or closed.", "");
        } catch (SQLException e) {
            String msg = "Unable to connect to MySQL database.";
            String details = e.getMessage();
            if (details.contains("Communications link failure") || details.contains("Connection refused")) {
                msg = "MySQL Server is not running or port " + CONFIG.getPort() + " is unreachable.";
                details = "Please start MySQL in XAMPP / MySQL Service and check that port " + CONFIG.getPort() + " is open.";
            } else if (details.contains("Access denied")) {
                msg = "Authentication failed (Access Denied).";
                details = "Please verify the username ('" + CONFIG.getUser() + "') and password in db.properties.";
            } else if (details.contains("Unknown database")) {
                msg = "Database '" + CONFIG.getName() + "' does not exist.";
                details = "Please run db/setup_database.sql to initialize the schema.";
            }
            return new ConnectionStatus(false, msg, details);
        }
    }

    /**
     * Auto-initializes database tables if they do not exist.
     */
    public static boolean autoInitializeTables() {
        String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
            "user_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "username VARCHAR(50) NOT NULL UNIQUE, " +
            "password_hash VARCHAR(255) NOT NULL, " +
            "full_name VARCHAR(100) NOT NULL, " +
            "email VARCHAR(100) NOT NULL UNIQUE, " +
            "phone VARCHAR(20), " +
            "role ENUM('ADMIN', 'STUDENT') NOT NULL DEFAULT 'STUDENT', " +
            "student_id_number VARCHAR(50) NULL, " +
            "department VARCHAR(100) NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB";

        String createEvents = "CREATE TABLE IF NOT EXISTS events (" +
            "event_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "event_code VARCHAR(30) NOT NULL UNIQUE, " +
            "title VARCHAR(150) NOT NULL, " +
            "description TEXT, " +
            "category VARCHAR(50) NOT NULL, " +
            "date_time DATETIME NOT NULL, " +
            "venue VARCHAR(150) NOT NULL, " +
            "max_capacity INT NOT NULL, " +
            "available_seats INT NOT NULL, " +
            "status ENUM('UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'UPCOMING', " +
            "created_by INT NULL, " +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB";

        String createRegistrations = "CREATE TABLE IF NOT EXISTS registrations (" +
            "registration_id INT AUTO_INCREMENT PRIMARY KEY, " +
            "registration_code VARCHAR(40) NOT NULL UNIQUE, " +
            "user_id INT NOT NULL, " +
            "event_id INT NOT NULL, " +
            "registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "status ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED', " +
            "cancellation_date TIMESTAMP NULL, " +
            "notes VARCHAR(255) NULL, " +
            "CONSTRAINT fk_reg_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE, " +
            "CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE, " +
            "CONSTRAINT uq_user_event_reg UNIQUE (user_id, event_id)) ENGINE=InnoDB";

        String insertDefaultUsers = "INSERT INTO users (username, password_hash, full_name, email, phone, role, student_id_number, department) " +
            "VALUES " +
            "('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Dr. Sarah Jenkins', 'admin@college.edu', '9876543210', 'ADMIN', NULL, 'Event Management Cell'), " +
            "('student1', '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'Alex Rivera', 'alex.rivera@student.college.edu', '9123456780', 'STUDENT', 'CS-2024-001', 'Computer Science & Engineering'), " +
            "('student2', '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'Maya Patel', 'maya.patel@student.college.edu', '9123456781', 'STUDENT', 'EC-2024-042', 'Electronics & Communication'), " +
            "('student3', '703b0a3d6ad75b649a28adde7d83c6251da457549263bc7ff45ec709b0a8448b', 'David Kim', 'david.kim@student.college.edu', '9123456782', 'STUDENT', 'ME-2024-015', 'Mechanical Engineering') " +
            "ON DUPLICATE KEY UPDATE password_hash = VALUES(password_hash);";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createUsers);
            stmt.executeUpdate(createEvents);
            stmt.executeUpdate(createRegistrations);
            stmt.executeUpdate(insertDefaultUsers);
            return true;
        } catch (SQLException e) {
            System.err.println("Could not auto-initialize tables: " + e.getMessage());
            return false;
        }
    }
}
