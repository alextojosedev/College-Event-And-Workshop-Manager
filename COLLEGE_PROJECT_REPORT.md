# COLLEGE PROJECT REPORT

## Project Title: College Event and Workshop Manager
**Degree / Course**: Bachelor of Technology / Computer Science & Engineering  
**Application Type**: Desktop Enterprise Application  
**Primary Technologies**: Java (Swing, JDBC), MySQL, Git, Maven  

---

## 1. Abstract
The **College Event and Workshop Manager** is a desktop application developed in Java Swing and backed by a MySQL database using JDBC. The system streamlines the administration and student participation workflows for university events, national symposiums, hackathons, guest lectures, and hands-on workshops. Designed with a layered architecture (UI, Service, DAO, and Model), the software demonstrates object-oriented programming principles—encapsulation, inheritance, polymorphism, and abstraction—while guaranteeing data integrity through ACID database transactions and pessimistic locking for seat allocations.

---

## 2. Problem Statement
In many collegiate institutions, event and workshop management is handled through fragmented channels, including paper sign-up sheets, shared spreadsheets, and ad-hoc online forms. This manual methodology introduces serious operational vulnerabilities:
1. **Overbooking and Capacity Breaches**: Multiple students registering simultaneously without synchronized concurrency control results in more bookings than physical hall capacities permit.
2. **Duplicate Registrations**: Manual record-keeping struggles to enforce unique student-to-event registration constraints.
3. **Lack of Real-time Visibility**: Event organizers cannot easily monitor live seat counts, and students have no transparent view of upcoming opportunities or remaining seats.
4. **Inefficient Cancellation Workflows**: When students cancel or when organizers postpone or cancel an event, seat counts and student notifications are frequently out of sync.

---

## 3. Objective of the Project
- To develop a secure, desktop management system for college events and workshops.
- To provide role-based interfaces tailored for **Administrators** (event coordinators) and **Students**.
- To prevent duplicate registrations and race-condition overbooking using atomic JDBC transactions.
- To demonstrate OOP design, separation of concerns, and defensive exception handling in Java.
- To provide a modern, responsive user interface with real-time analytics and ticket generation.

---

## 4. Proposed System & Architecture

The proposed system adopts a **Four-Tier Architecture**:
1. **Presentation Layer (Swing UI)**: Native desktop GUI enhanced with FlatLaf styling, custom KPI cards, and responsive tables.
2. **Service / Business Logic Layer**: Enforces validation, handles password hashing with SHA-256, and orchestrates transaction boundaries.
3. **Data Access Object (DAO) Layer**: Abstracts SQL execution using `PreparedStatement`, ensuring protection against SQL injection.
4. **Persistence Layer (MySQL Database)**: Relational database enforcing primary keys, foreign keys, unique indexes, and ACID guarantees.

```
+-------------------------------------------------------------------+
|                       PRESENTATION LAYER                          |
|   LoginFrame  |  AdminDashboard  |  StudentDashboard  |  Modals   |
+-------------------------------------------------------------------+
                                  │
                                  ▼
+-------------------------------------------------------------------+
|                          SERVICE LAYER                            |
|       AuthService   |    EventService   |   RegistrationService   |
|                 (Validation, SHA-256, Business Rules)              |
+-------------------------------------------------------------------+
                                  │
                                  ▼
+-------------------------------------------------------------------+
|                        DATA ACCESS LAYER                          |
|         UserDao     |      EventDao     |    RegistrationDao      |
|              (PreparedStatement, ACID Transactions)               |
+-------------------------------------------------------------------+
                                  │
                                  ▼
+-------------------------------------------------------------------+
|                        DATABASE LAYER                             |
|               MySQL (users, events, registrations)                |
+-------------------------------------------------------------------+
```

---

## 5. System Modules

### Module 1: Authentication & Security
- Secure sign-in with role-based routing (Admin vs. Student).
- SHA-256 cryptographic hashing ensures raw passwords are never stored in the database.
- Student self-registration dialog directly accessible from the login window.
- Database diagnostics bar showing connection health with a 1-click configuration modal.

### Module 2: Administrator Dashboard & Event Management
- Real-time KPI summary cards: Total Events, Upcoming Events, Total Registrations, Available Seats, Enrolled Students.
- Comprehensive CRUD: Create new events, edit event details, delete events.
- Search and multi-criteria filters (keyword search, category filter, status filter).
- View enrolled students per event in a dedicated modal.
- Atomic Event Cancellation: Cancelling an event atomically updates the event status and all student registrations in a single database transaction.

### Module 3: Student Portal & Self-Service
- Browse all upcoming workshops and fests with real-time seat availability.
- Event Details viewer featuring a dynamic seat occupancy progress bar.
- One-click seat reservation with validation against overbooking or duplicate entries.
- Generates a unique event pass code (`REG-YYYY-XXXX`).
- "My Registrations" view allowing students to review tickets or cancel reservations, immediately restoring the seat to the open pool.

### Module 4: Transaction & Concurrency Management
- Pessimistic locking (`SELECT ... FOR UPDATE`) prevents concurrent overbooking.
- Managed JDBC transactions (`conn.setAutoCommit(false)`, `conn.commit()`, `conn.rollback()`).

---

## 6. Object-Oriented Programming (OOP) Implementation

| OOP Principle | Implementation in Project |
| :--- | :--- |
| **Encapsulation** | All entity fields (`User`, `Event`, `Registration`) are `private`. Access is granted exclusively via accessor (getter) and mutator (setter) methods with invariant validation. |
| **Inheritance** | Base abstract class `User` is extended by `Admin` and `Student`, inheriting common attributes (ID, username, email, phone) while adding specialized fields (`studentIdNumber`, `department`). |
| **Polymorphism** | Abstract methods `hasAdminPrivileges()` and `getRoleDisplayName()` are polymorphically implemented by `Admin` and `Student`. The DAO interface design allows interchangeable implementations. |
| **Abstraction** | Interfaces `UserDao`, `EventDao`, and `RegistrationDao` expose data operations without revealing underlying SQL mechanisms. UI components interact only with Services. |
| **Exception Handling** | Custom hierarchy rooted at `AppException` (`AuthenticationException`, `DuplicateRegistrationException`, `EventFullException`, `ValidationException`). Try-with-resources handles JDBC lifecycle. |
| **Collections** | Extensive utilization of Java Collections (`List<Event>`, `ArrayList`, `Map<String, User>`, `Optional<T>`) for filtering, sorting, and holding query results. |

---

## 7. Database Design & Relational Schema

### 7.1 Entity Relationship (ER) Summary
- **users (1) ────< (N) events** (Admin creates events)
- **users (1) ────< (N) registrations** (Student creates registrations)
- **events (1) ────< (N) registrations** (Event has multiple registrations)

### 7.2 Tables & Constraints
1. **users**
   - `user_id`: INT AUTO_INCREMENT PRIMARY KEY
   - `username`: VARCHAR(50) UNIQUE NOT NULL
   - `password_hash`: VARCHAR(255) NOT NULL
   - `full_name`: VARCHAR(100) NOT NULL
   - `email`: VARCHAR(100) UNIQUE NOT NULL
   - `phone`: VARCHAR(20)
   - `role`: ENUM('ADMIN', 'STUDENT') NOT NULL
   - `student_id_number`: VARCHAR(50) NULL
   - `department`: VARCHAR(100) NULL
   - `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

2. **events**
   - `event_id`: INT AUTO_INCREMENT PRIMARY KEY
   - `event_code`: VARCHAR(30) UNIQUE NOT NULL
   - `title`: VARCHAR(150) NOT NULL
   - `description`: TEXT
   - `category`: VARCHAR(50) NOT NULL
   - `date_time`: DATETIME NOT NULL
   - `venue`: VARCHAR(150) NOT NULL
   - `max_capacity`: INT NOT NULL (CHECK > 0)
   - `available_seats`: INT NOT NULL (CHECK >= 0 AND <= max_capacity)
   - `status`: ENUM('UPCOMING', 'ONGOING', 'COMPLETED', 'CANCELLED')
   - `created_by`: INT (FK REFERENCES users.user_id)
   - `created_at`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP

3. **registrations**
   - `registration_id`: INT AUTO_INCREMENT PRIMARY KEY
   - `registration_code`: VARCHAR(40) UNIQUE NOT NULL
   - `user_id`: INT NOT NULL (FK REFERENCES users.user_id ON DELETE CASCADE)
   - `event_id`: INT NOT NULL (FK REFERENCES events.event_id ON DELETE CASCADE)
   - `registration_date`: TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   - `status`: ENUM('CONFIRMED', 'CANCELLED') NOT NULL
   - `cancellation_date`: TIMESTAMP NULL
   - `notes`: VARCHAR(255) NULL
   - **UNIQUE CONSTRAINT**: `uq_user_event_reg (user_id, event_id)` (enforces single registration at DB level)

---

## 8. Testing & Quality Assurance

### 8.1 Automated Test Suite
The project incorporates automated test suites covering core functionality:
- **`PasswordUtilTest`**: Verifies SHA-256 output length, determinism, and correct/incorrect password verification.
- **`ModelTest`**: Verifies polymorphism, role permissions, capacity calculations, and registration status transitions.
- **`ValidationTest`**: Verifies boundary conditions, invalid emails, empty usernames, negative capacities, and past dates.

### 8.2 Execution Results
```
==================================================
   College Event & Workshop Manager - Test Suite  
==================================================
[TEST] PasswordUtil.testPasswordHashing() ... PASSED
[TEST] PasswordUtil.testPasswordVerification() ... PASSED
[TEST] ModelTest.testUserInheritanceAndPolymorphism() ... PASSED
[TEST] ModelTest.testEventCapacityLogic() ... PASSED
[TEST] ModelTest.testRegistrationStatus() ... PASSED
[TEST] ValidationTest.testAuthValidation() ... PASSED
[TEST] ValidationTest.testEventValidation() ... PASSED
==================================================
Summary: Total Tests: 7 | Passed: 7 | Failed: 0
==================================================
```

---

## 9. Conclusion & Future Enhancements

### 9.1 Conclusion
The **College Event and Workshop Manager** delivers an automated, transparent, and concurrency-safe solution for managing university events. By integrating Java Swing with MySQL through clean OOP design patterns and ACID transaction handling, the application eliminates overbooking, prevents duplicate records, and provides a polished user experience.

### 9.2 Future Scope
- **Email / SMS Notifications**: Automated dispatch of registration passes and cancellation updates.
- **QR Code Scanning**: QR codes embedded on digital tickets for instantaneous venue check-in.
- **Certificate Generation**: Automated PDF certificate generation for students who attended completed workshops.
- **Payment Gateway Integration**: Supporting paid workshops and symposiums.
