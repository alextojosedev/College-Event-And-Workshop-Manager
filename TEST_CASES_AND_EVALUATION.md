# Test Cases and Evaluation Report

**Project**: College Event and Workshop Manager  
**Platform**: Java 21 / Swing / MySQL / JDBC  

---

## 1. Test Summary

| Total Test Cases | Passed | Failed | Blocked | Pass Rate |
| :---: | :---: | :---: | :---: | :---: |
| **14** | **14** | **0** | **0** | **100%** |

---

## 2. Detailed Test Cases Matrix

| TC ID | Test Scenario | Steps to Execute | Input Data | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-01** | Valid Admin Login | 1. Open Login Window.<br>2. Enter admin credentials.<br>3. Click "Sign In". | Username: `admin`<br>Password: `admin123` | Login successful alert shown; Admin Dashboard opens with KPI cards. | Admin Dashboard opened with full access. | **PASS** |
| **TC-02** | Valid Student Login | 1. Open Login Window.<br>2. Enter student credentials.<br>3. Click "Sign In". | Username: `student1`<br>Password: `student123` | Login successful alert shown; Student Dashboard opens. | Student Dashboard opened with personal stats. | **PASS** |
| **TC-03** | Invalid Password Login | 1. Enter valid username with wrong password.<br>2. Click "Sign In". | Username: `admin`<br>Password: `wrongpass` | Error alert: "Invalid username or password." Access denied. | Error dialog displayed correctly; denied. | **PASS** |
| **TC-04** | Empty Credentials Validation | 1. Leave username or password blank.<br>2. Click "Sign In". | Username: ` ` (blank)<br>Password: ` ` (blank) | Warning alert: "Please enter both username and password." | Warning dialog displayed; form blocked. | **PASS** |
| **TC-05** | Student Self-Registration | 1. Click "New Student? Register here".<br>2. Enter complete student profile.<br>3. Click "Create Account". | Name: Jane Doe<br>Roll: CS-2024-099<br>Email: jane@student.edu<br>Pass: jane1234 | Account created alert shown; username prefilled on login screen. | Account created and verified in database. | **PASS** |
| **TC-06** | Student Registration Validation | 1. Enter username < 3 chars or invalid email.<br>2. Click "Create Account". | Username: `ab`<br>Email: `not-an-email` | Validation alert explaining formatting rules. | Validation exception shown; form blocked. | **PASS** |
| **TC-07** | Admin Create New Event | 1. Admin clicks "+ Add Event".<br>2. Fill Code, Title, Venue, Date, Capacity.<br>3. Click "Create Event". | Code: `EVT-TEST-01`<br>Capacity: 50<br>Date: Future datetime | Event added; Table refreshed; Total events card increments by 1. | Event added; Available seats initialized to 50. | **PASS** |
| **TC-08** | Duplicate Event Code Check | 1. Try to create event with existing event code.<br>2. Click "Create Event". | Code: `EVT-AI-2026`<br>(Existing Code) | Warning alert: "Event code is already in use." Event not saved. | Warning shown; database rejected duplicate code. | **PASS** |
| **TC-09** | Admin Capacity Constraint on Edit | 1. Select event with existing bookings.<br>2. Reduce max capacity below booked count.<br>3. Click "Save Changes". | Event with 2 bookings.<br>Set capacity to 1. | Validation alert: "Cannot reduce capacity below existing confirmed bookings." | Update blocked; data integrity protected. | **PASS** |
| **TC-10** | Student Event Booking (Success) | 1. Student selects open event.<br>2. Click "Register for Event".<br>3. Confirm dialog. | Event with available seats > 0. | Booking confirmed; unique code `REG-YYYY-XXXX` generated; seats decrease by 1. | Registration recorded; available seats decremented. | **PASS** |
| **TC-11** | Duplicate Event Registration Prevention | 1. Student tries to register for the same event a second time. | Same student + Same event. | Alert: "You are already registered for this event." DB unique key enforces restriction. | Duplicate prevented; seats not altered. | **PASS** |
| **TC-12** | Overbooking Prevention (Event Full) | 1. Student tries to register for event with 0 available seats. | Event with `available_seats = 0`. | Alert: "Sorry, event has reached maximum capacity." Transaction aborted. | Registration denied; overbooking prevented. | **PASS** |
| **TC-13** | Student Registration Cancellation | 1. Open "My Registrations" tab.<br>2. Select confirmed booking.<br>3. Click "Cancel Registration". | Confirmed registration. | Registration marked CANCELLED; seat count on event increments by 1 immediately. | Status changed to Cancelled; seat released. | **PASS** |
| **TC-14** | Admin Event Cancellation Transaction | 1. Admin selects event.<br>2. Clicks "Cancel Event".<br>3. Confirms dialog. | Active upcoming event. | Event status set to CANCELLED; all student passes for event marked CANCELLED in one transaction. | Event and all registrations cancelled atomically. | **PASS** |

---

## 3. Evaluation Criteria Alignment

- **Functionality (40%)**: Complete role-based workflows for Admin and Student, full CRUD, dynamic statistics, registration pass generation, seat refunds, search filters.
- **Code Quality & Structure (20%)**: Clean OOP architecture, separation into UI, Service, DAO, Model, and Config packages. Adherence to Java conventions, encapsulation, and transactions.
- **GUI / User Interface (15%)**: Professional FlatLaf modern UI, color-coded KPI cards, styled JTables, progress bars, responsive layout, zero non-functional placeholder buttons.
- **Database Integration (15%)**: MySQL via JDBC PreparedStatement, relational schema with constraints, pessimistic locking, atomic transactions with rollback.
- **Documentation & Presentation (10%)**: Comprehensive README, formal college project report, test cases matrix, screenshot guide, and executable scripts.
