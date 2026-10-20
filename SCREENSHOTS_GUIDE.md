# Screenshot Capture Guide for College Submission

This guide provides a step-by-step checklist of the screenshots you should capture from the running application for your college lab record, project report, presentation slides, or viva examination.

---

## Recommended Screenshot Checklist

### 1. Authentication & System Setup
| # | Screenshot Title | How to Capture | Key Elements to Highlight |
| :-: | :--- | :--- | :--- |
| **01** | **Login Window** | Run `run.bat` or `.\run.ps1`. | Modern dark header, "Sign In" card, demo credential helper buttons ("Fill Admin", "Fill Student"), and the connection status bar at the bottom. |
| **02** | **Database Configuration Dialog** | On the login screen, click **"Configure Database"** (bottom right). | Host, Port, Database name fields, "Test Connection" button showing live status, and "Initialize Tables" button. |
| **03** | **Student Self-Registration Dialog** | On the login screen, click **"New Student? Register here"**. | Input fields: Full Name, Student Roll No, Department dropdown, Email, Username, Password. |

---

### 2. Administrator Dashboard
| # | Screenshot Title | How to Capture | Key Elements to Highlight |
| :-: | :--- | :--- | :--- |
| **04** | **Admin Dashboard - Overview & KPI Cards** | Log in with `admin` / `admin123`. | Top navbar with Administrator badge, 5 KPI stat cards (Total Events, Upcoming, Registrations, Open Seats, Students), and the main Events JTable. |
| **05** | **Event Search & Filter in Action** | In Admin Dashboard, type a query (e.g. `Hackathon`) or select a category dropdown, and click **"Filter"**. | Filtered rows in the JTable matching the criteria, and the active toolbar. |
| **06** | **Create / Add New Event Modal** | Click the green **"+ Add Event"** button. | Clean form with Event Code, Title, Category dropdown, Date & Time (YYYY-MM-DD HH:MM), Venue, Capacity spinner, and Description. |
| **07** | **Edit Event Modal** | Select an event in the table and click **"Edit"**. | Pre-filled event details with code locked and capacity adjustments enabled. |
| **08** | **View Registered Students per Event** | Select an event (e.g. `National AI & Deep Learning Summit`) and click **"View Registrations"**. | Modal showing the list of enrolled students, their Roll Numbers, Departments, Emails, and Registration Codes. |
| **09** | **Master Registrations & Audit Log Tab** | Click on the second tab **"All Registrations & Reports"**. | Master audit table showing all student registrations across all campus events with search filter. |
| **10** | **Enrolled Students Directory Tab** | Click on the third tab **"Enrolled Students Directory"**. | Complete roster of all students registered in the university portal. |
| **11** | **Event Cancellation Dialog** | Select an upcoming event and click **"Cancel Event"**. | Confirmation alert warning that all student passes for this event will also be cancelled atomically. |

---

### 3. Student Portal
| # | Screenshot Title | How to Capture | Key Elements to Highlight |
| :-: | :--- | :--- | :--- |
| **12** | **Student Dashboard - Available Events** | Log in with `student1` / `student123`. | Student Portal banner, personal roll number/department card, upcoming events table with remaining seat counts. |
| **13** | **Event Details & Seat Progress Bar** | Select an event and click **"View Details"**. | Event metadata, description, and visual progress bar displaying seat occupancy percentage (e.g. 80% reserved). |
| **14** | **Seat Booking Confirmation Alert** | Click **"Register for Event"** and confirm. | Success dialog showing the generated unique Registration Pass Code (`REG-YYYY-XXXX`) and student confirmation. |
| **15** | **Duplicate Registration Warning Alert** | Try to register for the same event a second time. | Friendly warning dialog stating you are already registered for this event. |
| **16** | **"My Registrations" Tab** | Click on the second tab **"My Registrations & Tickets"**. | Table displaying the student's personal bookings, event details, and status. |
| **17** | **Digital Event Ticket / Pass Details** | Select a booking and click **"View Pass Details"**. | Formatted text ticket receipt featuring Pass Code, Student details, Event venue, date, and timestamp. |
| **18** | **Registration Cancellation & Seat Refund** | Select a confirmed registration and click **"Cancel Registration"**. | Confirmation dialog and subsequent success alert confirming the seat was returned to the open capacity pool. |

---

### 4. Technical Verification & Database
| # | Screenshot Title | How to Capture | Key Elements to Highlight |
| :-: | :--- | :--- | :--- |
| **19** | **Automated Test Suite Execution** | Run `test.bat` or `.\test.ps1` in PowerShell/Terminal. | Terminal showing 7/7 unit tests passing (PasswordUtil, ModelTest, ValidationTest). |
| **20** | **MySQL Database Tables in Workbench / phpMyAdmin** | Open MySQL Workbench or phpMyAdmin and expand `college_event_db`. | The 3 tables (`users`, `events`, `registrations`) with foreign keys and unique constraints. |

---

## Presentation & Lab Record Tips
1. **Crop Uniformly**: Use Windows Snipping Tool (`Win + Shift + S`) to capture the active window (`Alt + PrtScn`).
2. **Order Sequentially**: Place the screenshots in your report in chronological order: Setup -> Login -> Admin Management -> Student Registration -> Cancellation.
3. **Add Captions**: Always include a 1-sentence caption below each figure (e.g. *Figure 4.2: Admin Dashboard displaying real-time event KPIs and event management table*).
