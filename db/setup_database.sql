-- ===================================================================
-- College Event and Workshop Manager - Database Setup Script
-- Compatible with MySQL 8.x and MariaDB
-- Safe to re-run: uses IF NOT EXISTS / ON DUPLICATE KEY UPDATE
-- ===================================================================

CREATE DATABASE IF NOT EXISTS college_event_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE college_event_db;

-- -------------------------------------------------------------------
-- 1. Table: users
-- Supports both ADMIN and STUDENT roles with role-specific fields
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role ENUM('ADMIN', 'STUDENT') NOT NULL DEFAULT 'STUDENT',
    student_id_number VARCHAR(50) NULL,
    department VARCHAR(100) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- -------------------------------------------------------------------
-- 2. Table: events
-- Stores events and workshops with capacity and remaining seats
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    event_code VARCHAR(30) NOT NULL UNIQUE,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    date_time DATETIME NOT NULL,
    venue VARCHAR(150) NOT NULL,
    max_capacity INT NOT NULL,
    available_seats INT NOT NULL,
    status ENUM('UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'UPCOMING',
    created_by INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_capacity CHECK (max_capacity > 0),
    CONSTRAINT chk_available_seats CHECK (available_seats >= 0 AND available_seats <= max_capacity),
    CONSTRAINT fk_events_user FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- -------------------------------------------------------------------
-- 3. Table: registrations
-- Stores student event registrations with unique constraint to prevent duplicates
-- -------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS registrations (
    registration_id INT AUTO_INCREMENT PRIMARY KEY,
    registration_code VARCHAR(40) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    event_id INT NOT NULL,
    registration_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status ENUM('CONFIRMED', 'CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
    cancellation_date TIMESTAMP NULL,
    notes VARCHAR(255) NULL,
    CONSTRAINT fk_reg_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_reg_event FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
    -- Prevent duplicate registrations at database level
    CONSTRAINT uq_user_event_reg UNIQUE (user_id, event_id)
) ENGINE=InnoDB;

-- -------------------------------------------------------------------
-- Indexes for optimized search and dashboard queries
-- Safe to re-run: drops existing indexes before recreating
-- -------------------------------------------------------------------
DROP INDEX IF EXISTS idx_events_status   ON events;
DROP INDEX IF EXISTS idx_events_category ON events;
DROP INDEX IF EXISTS idx_events_datetime ON events;
DROP INDEX IF EXISTS idx_reg_status      ON registrations;

CREATE INDEX idx_events_status   ON events(status);
CREATE INDEX idx_events_category ON events(category);
CREATE INDEX idx_events_datetime ON events(date_time);
CREATE INDEX idx_reg_status      ON registrations(status);

-- -------------------------------------------------------------------
-- Sample Demonstration Data
-- Passwords below are hashed using SHA-256 for security:
-- 'admin123'  -> 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- 'student123'-> 68858a74e2d31215b3e64b63896dfa99ff1165a250391ab1eb95d6666838a3d4
-- -------------------------------------------------------------------

INSERT INTO users (username, password_hash, full_name, email, phone, role, student_id_number, department)
VALUES
('admin',    '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'Dr. Sarah Jenkins', 'admin@college.edu',                    '9876543210', 'ADMIN',   NULL,          'Event Management Cell'),
('student1', '68858a74e2d31215b3e64b63896dfa99ff1165a250391ab1eb95d6666838a3d4', 'Alex Rivera',       'alex.rivera@student.college.edu',      '9123456780', 'STUDENT', 'CS-2024-001', 'Computer Science & Engineering'),
('student2', '68858a74e2d31215b3e64b63896dfa99ff1165a250391ab1eb95d6666838a3d4', 'Maya Patel',        'maya.patel@student.college.edu',       '9123456781', 'STUDENT', 'EC-2024-042', 'Electronics & Communication'),
('student3', '68858a74e2d31215b3e64b63896dfa99ff1165a250391ab1eb95d6666838a3d4', 'David Kim',         'david.kim@student.college.edu',        '9123456782', 'STUDENT', 'ME-2024-015', 'Mechanical Engineering')
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

INSERT INTO events (event_code, title, description, category, date_time, venue, max_capacity, available_seats, status, created_by)
VALUES
('EVT-AI-2026',    'National AI & Deep Learning Summit',          'Two-day hands-on summit featuring industry keynote speakers from leading tech firms, neural network design, and generative AI labs.',      'Workshop',     '2026-11-15 09:30:00', 'APJ Abdul Kalam Auditorium',       60,  58,  'UPCOMING', 1),
('EVT-HACK-2026',  'HackNova: 24-Hour Inter-College Hackathon',   'Annual university hackathon solving real-world challenges in smart healthcare, green energy, and fintech. Exciting cash prizes!',           'Hackathon',    '2026-11-20 10:00:00', 'Campus Innovation Center, Lab 3',  80,  79,  'UPCOMING', 1),
('EVT-CYBER-2026', 'Ethical Hacking & Network Defense Bootcamp',  'Intensive cybersecurity boot camp covering penetration testing, CTF challenges, and secure application development.',                        'Workshop',     '2026-11-28 14:00:00', 'Seminar Hall B, CSE Block',        40,  39,  'UPCOMING', 1),
('EVT-ROBO-2026',  'Autonomous Robotics & IoT Hands-on Expo',     'Design, build, and race micro-controllers and IoT-connected rovers. Hardware kits provided.',                                               'Technical Fest','2026-12-05 10:30:00', 'Robotics Research Lab',            30,  30,  'UPCOMING', 1),
('EVT-CULT-2026',  'Sanskriti: Annual Cultural Gala',             'Grand cultural festival with battle of bands, classical and street dance, fine arts, and dramatics.',                                        'Cultural',     '2026-12-18 17:00:00', 'Open Air Amphitheatre',           200, 200, 'UPCOMING', 1)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- Sample initial registrations
INSERT INTO registrations (registration_code, user_id, event_id, status, notes)
VALUES
('REG-2026-0001', 2, 1, 'CONFIRMED', 'Early bird student registration'),
('REG-2026-0002', 3, 1, 'CONFIRMED', 'Registered for AI Workshop'),
('REG-2026-0003', 2, 2, 'CONFIRMED', 'Hackathon team lead registration'),
('REG-2026-0004', 4, 3, 'CONFIRMED', 'Cybersecurity enthusiast')
ON DUPLICATE KEY UPDATE status=VALUES(status);
