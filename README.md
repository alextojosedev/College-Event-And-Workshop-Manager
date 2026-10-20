# College Event and Workshop Manager

A modern, robust Java Swing desktop application integrated with MySQL via JDBC, designed to manage college academic events, workshops, hackathons, seminars, and student registrations.

Developed adhering to strict Object-Oriented Programming (OOP) principles, layered architecture (UI, Service, DAO, Model), and ACID transactional concurrency.

---

## 1. Key Features

### Role-Based Access
- **Admin**:
  - Real-time KPI Dashboard: Total Events, Upcoming Events, Confirmed Registrations, Open Seats, Enrolled Students.
  - Event Management: Add, edit, delete, search, and view events/workshops.
  - View list of enrolled students per event.
  - Cancel events atomically (automatically updates all registered students' status in a single database transaction).
  - Search & filter events by keyword, category, and status.
  - Master registration audit logs & student directory.
- **Student**:
  - Discover upcoming college workshops, hackathons, and technical fests.
  - Search events by title, description, venue, or category.
  - View event details with visual seat capacity progress bars.
  - 1-Click Seat Reservation with unique pass code generation (`REG-YYYY-XXXX`).
  - Strict prevention of duplicate registrations or overbooking when full.
  - "My Registrations" management with instant seat cancellation and refund.
  - Digital Ticket / Event Pass inspection.

### Reliability & User Experience
- **Modern UI**: Styled with **FlatLaf Light** look & feel, clean card layouts, responsive tables, and intuitive action modals.
- **ACID Transaction Safety**: Prevents race conditions and overbooking using pessimistic database locks (`SELECT ... FOR UPDATE`) and JDBC transactions (`commit` / `rollback`).
- **Database Resilience**: If MySQL is offline, the app displays a clear diagnostic warning and provides a **Database Configuration Dialog** to test and update host, port, database, and credentials without altering code or recompiling.
- **Security**: Passwords securely hashed with **SHA-256** before storage.

---

## 2. Technology Stack

- **Language**: Java 21 (LTS)
- **GUI Framework**: Java Swing with FlatLaf Modern Look & Feel
- **Database**: MySQL 8.x / MariaDB
- **Database Driver**: MySQL Connector/J 8.3.0
- **Architecture**: Layered MVC/DAO Architecture
- **Build Tools**:
  - Bundled scripts (`compile.bat`, `run.bat`, `test.bat`, `compile.ps1`, `run.ps1`, `test.ps1`) for instant execution using JDK
  - Standard Apache Maven (`pom.xml`) for IDEs (IntelliJ IDEA, Eclipse, NetBeans, VS Code)
- **Version Control**: Git

---

## 3. Project Structure

```
College Event And Workshop Manager/
├── pom.xml                               # Maven project configuration
├── .gitignore                            # Git ignore rules
├── README.md                             # Setup & execution guide
├── COLLEGE_PROJECT_REPORT.md             # Formal college project report
├── TEST_CASES_AND_EVALUATION.md          # Comprehensive test cases with results
├── SCREENSHOTS_GUIDE.md                  # Checklist of submission screenshots
├── compile.bat / compile.ps1             # Build scripts
├── run.bat / run.ps1                     # Application launcher
├── test.bat / test.ps1                   # Automated test runner
├── db.properties                         # Active database connection settings
├── db/
│   └── setup_database.sql                # MySQL schema & sample demonstration data
├── lib/
│   ├── mysql-connector-j-8.3.0.jar       # MySQL JDBC Driver
│   └── flatlaf-3.5.4.jar                 # Modern Swing Look & Feel
└── src/
    ├── main/
    │   ├── resources/
    │   │   ├── db.properties             # Bundled default database config
    │   │   └── db.properties.example     # Configuration template
    │   └── java/com/college/eventmanager/
    │       ├── Main.java                 # Desktop Application Entry Point
    │       ├── config/                   # DatabaseConnection & DbConfig
    │       ├── model/                    # User, Admin, Student, Event, Registration
    │       ├── exception/                # Custom application exceptions
    │       ├── dao/                      # UserDao, EventDao, RegistrationDao
    │       ├── service/                  # AuthService, EventService, RegistrationService
    │       └── ui/                       # Swing Frames, Panels, Dialogs & Components
    └── test/java/com/college/eventmanager/
        ├── PasswordUtilTest.java         # Security unit tests
        ├── ModelTest.java                # OOP model & capacity unit tests
        ├── ValidationTest.java           # Input validation unit tests
        └── AppTestSuite.java             # Master automated test suite
```

---

## 4. Prerequisites

1. **Java Development Kit (JDK)**: Java 17 or Java 21 installed (`javac` and `java` in PATH).
2. **MySQL Server**: MySQL 8.x, MariaDB, or XAMPP / WAMP / Docker MySQL on port 3306.
3. *(Optional)* **Apache Maven**: If building via Maven CLI.

---

## 5. Database Setup (MySQL)

### Step 1: Start MySQL
Ensure your MySQL service is running (e.g., click **Start MySQL** in XAMPP Control Panel, or run `net start mysql`).

### Step 2: Execute Schema & Sample Data
Open your MySQL terminal, MySQL Workbench, or phpMyAdmin, and execute `db/setup_database.sql`:

**Via MySQL Command Line:**
```bash
mysql -u root -p < db/setup_database.sql
```

**Via PowerShell:**
```powershell
Get-Content db\setup_database.sql | mysql -u root -p
```

### Step 3: Configure Database Credentials
Edit `db.properties` in the project root if your MySQL password is not blank:
```properties
db.host=localhost
db.port=3306
db.name=college_event_db
db.user=root
db.password=your_mysql_password
```

> **Note**: You can also configure or test the database directly from the application's login screen using the **"Configure Database"** button!

---

## 6. How to Run the Application

### Option A: Using the Bundled Scripts (Fastest - No Maven Needed)

**On Windows PowerShell:**
```powershell
# 1. Compile the project
.\compile.ps1

# 2. Run the automated test suite
.\test.ps1

# 3. Launch the Swing GUI
.\run.ps1
```

**On Windows Command Prompt (CMD):**
```cmd
compile.bat
test.bat
run.bat
```

### Option B: Using Apache Maven
```bash
# Compile and test
mvn clean test

# Run application
mvn exec:java

# Or build executable standalone JAR with all dependencies
mvn clean package
java -jar target/college-event-manager-1.0.0-jar-with-dependencies.jar
```

---

## 7. Demo Credentials for Evaluation

The database script pre-populates demo accounts with SHA-256 hashed passwords:

| Role | Username | Password | Full Name / Description |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `admin123` | Dr. Sarah Jenkins (Event Management Cell) |
| **Student 1** | `student1` | `student123` | Alex Rivera (CS-2024-001, Computer Science) |
| **Student 2** | `student2` | `student123` | Maya Patel (EC-2024-042, Electronics & Comm.) |
| **Student 3** | `student3` | `student123` | David Kim (ME-2024-015, Mechanical Eng.) |

> **Evaluator Tip**: On the login screen, convenient buttons (**"Fill Admin"** and **"Fill Student"**) are provided to autofill these credentials instantly! You can also click **"New Student? Register here"** to test live student self-registration.

---

## 8. Git Version Control Commands

Check status, commit changes, or push to your remote repository:

```bash
# Check working tree status
git status

# Stage changes
git add .

# Create a commit
git commit -m "Update student credentials and GitHub remote for alextojosedev"

# Set remote origin (configured for your GitHub username)
git remote add origin https://github.com/alextojosedev/College-Event-And-Workshop-Manager.git

# Set main branch and push (when you are ready to upload to GitHub)
git branch -M main
git push -u origin main
```
