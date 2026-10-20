package com.college.eventmanager.ui;

import com.college.eventmanager.model.Admin;
import com.college.eventmanager.model.DashboardStats;
import com.college.eventmanager.model.Event;
import com.college.eventmanager.model.EventCategory;
import com.college.eventmanager.model.EventStatus;
import com.college.eventmanager.model.Registration;
import com.college.eventmanager.model.Student;
import com.college.eventmanager.service.EventService;
import com.college.eventmanager.service.RegistrationService;
import com.college.eventmanager.ui.components.StatCard;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern Java Swing Dashboard for College Administrators.
 * Displays real-time KPIs, full event CRUD management, registered students per event,
 * master registration reporting, and event cancellation with transaction safety.
 */
public class AdminDashboard extends JFrame {
    private final Admin admin;
    private final EventService eventService;
    private final RegistrationService registrationService;

    // Stat Cards
    private StatCard totalEventsCard;
    private StatCard upcomingEventsCard;
    private StatCard registrationsCard;
    private StatCard availableSeatsCard;
    private StatCard studentsCard;

    // Event Management Components
    private JTable eventTable;
    private DefaultTableModel eventTableModel;
    private List<Event> currentEventList = new ArrayList<>();
    private JTextField eventSearchField;
    private JComboBox<String> categoryFilter;
    private JComboBox<String> statusFilter;

    // All Registrations Components
    private JTable regTable;
    private DefaultTableModel regTableModel;
    private JTextField regSearchField;
    private List<Registration> currentRegList = new ArrayList<>();

    // Student Directory Components
    private JTable studentTable;
    private DefaultTableModel studentTableModel;

    public AdminDashboard(Admin admin) {
        super("Campus Event Manager - Admin Dashboard (" + admin.getFullName() + ")");
        this.admin = admin;
        this.eventService = new EventService();
        this.registrationService = new RegistrationService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 720);
        setMinimumSize(new Dimension(980, 620));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_MAIN);

        // Build UI sections
        add(createTopNavbar(), BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(0, 14));
        mainContent.setOpaque(false);
        mainContent.setBorder(new EmptyBorder(16, 20, 16, 20));

        mainContent.add(createStatsRow(), BorderLayout.NORTH);
        mainContent.add(createTabbedContent(), BorderLayout.CENTER);

        add(mainContent, BorderLayout.CENTER);

        // Load initial data
        refreshAllData();
    }

    private JPanel createTopNavbar() {
        JPanel nav = new JPanel(new BorderLayout(10, 0));
        nav.setBackground(Theme.PRIMARY_DARK);
        nav.setBorder(new EmptyBorder(10, 16, 10, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 2));
        left.setOpaque(false);

        JLabel brand = new JLabel("College Event & Workshop Manager");
        brand.setFont(Theme.FONT_HEADER);
        brand.setForeground(Color.WHITE);

        JLabel roleBadge = new JLabel("  ADMINISTRATOR  ");
        roleBadge.setFont(Theme.FONT_SMALL);
        roleBadge.setOpaque(true);
        roleBadge.setBackground(Theme.PRIMARY);
        roleBadge.setForeground(Color.WHITE);
        roleBadge.setBorder(new EmptyBorder(3, 6, 3, 6));

        left.add(brand);
        left.add(roleBadge);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        right.setOpaque(false);

        JLabel userLabel = new JLabel("Admin: " + admin.getFullName());
        userLabel.setFont(Theme.FONT_REGULAR);
        userLabel.setForeground(new Color(203, 213, 225));

        JButton dbBtn = new JButton("DB Settings");
        dbBtn.setFont(Theme.FONT_SMALL);
        dbBtn.addActionListener(e -> new DbSettingsDialog(this, this::refreshAllData).setVisible(true));

        JButton deleteAdminAccountBtn = Theme.createDangerButton("Delete Admin Account");
        deleteAdminAccountBtn.setFont(Theme.FONT_SMALL);
        deleteAdminAccountBtn.addActionListener(e -> handleDeleteAdminAccount());

        JButton logoutBtn = Theme.createDangerButton("Log Out");
        logoutBtn.addActionListener(e -> handleLogout());

        right.add(userLabel);
        right.add(dbBtn);
        right.add(deleteAdminAccountBtn);
        right.add(logoutBtn);

        nav.add(left, BorderLayout.WEST);
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 5, 12, 0));
        row.setOpaque(false);

        totalEventsCard = new StatCard("Total Events", "0", "All Time", Theme.PRIMARY);
        upcomingEventsCard = new StatCard("Upcoming", "0", "Scheduled", Theme.ACCENT);
        registrationsCard = new StatCard("Registrations", "0", "Active Students", Theme.SUCCESS);
        availableSeatsCard = new StatCard("Open Seats", "0", "Across Events", Theme.WARNING);
        studentsCard = new StatCard("Students", "0", "Enrolled in Portal", Theme.PRIMARY_DARK);

        row.add(totalEventsCard);
        row.add(upcomingEventsCard);
        row.add(registrationsCard);
        row.add(availableSeatsCard);
        row.add(studentsCard);
        return row;
    }

    private JTabbedPane createTabbedContent() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_SUBHEADER);
        tabs.setBackground(Theme.BG_MAIN);

        tabs.addTab("Event & Workshop Management", createEventManagementPanel());
        tabs.addTab("All Registrations & Reports", createRegistrationsPanel());
        tabs.addTab("Enrolled Students Directory", createStudentsPanel());

        return tabs;
    }

    // ==========================================
    // TAB 1: Event Management
    // ==========================================
    private JPanel createEventManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 4, 4, 4));

        // Two-row Toolbar to prevent shifting and overlapping
        JPanel toolbar = Theme.createCardPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setBorder(new EmptyBorder(8, 12, 8, 12));

        // Row 1: Action Buttons
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actionRow.setOpaque(false);

        JButton addBtn = Theme.createSuccessButton("+ Add Event");
        addBtn.addActionListener(e -> openAddEventDialog());

        JButton editBtn = Theme.createSecondaryButton("Edit");
        editBtn.addActionListener(e -> openEditEventDialog());

        JButton viewStudentsBtn = Theme.createPrimaryButton("View Registrations");
        viewStudentsBtn.addActionListener(e -> openRegisteredStudentsDialog());

        JButton cancelBtn = Theme.createDangerButton("Cancel Event");
        cancelBtn.addActionListener(e -> handleCancelEvent());

        JButton deleteBtn = Theme.createSecondaryButton("Delete");
        deleteBtn.addActionListener(e -> handleDeleteEvent());

        actionRow.add(addBtn);
        actionRow.add(editBtn);
        actionRow.add(viewStudentsBtn);
        actionRow.add(cancelBtn);
        actionRow.add(deleteBtn);

        // Row 2: Search & Filter Controls
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterRow.setOpaque(false);

        filterRow.add(new JLabel("Search:"));
        eventSearchField = Theme.createTextField(14);
        eventSearchField.setToolTipText("Search by code, title, venue...");
        filterRow.add(eventSearchField);

        filterRow.add(new JLabel("Category:"));
        categoryFilter = new JComboBox<>();
        categoryFilter.addItem("All Categories");
        for (EventCategory cat : EventCategory.values()) {
            categoryFilter.addItem(cat.getDisplayName());
        }
        filterRow.add(categoryFilter);

        filterRow.add(new JLabel("Status:"));
        statusFilter = new JComboBox<>();
        statusFilter.addItem("All Statuses");
        for (EventStatus st : EventStatus.values()) {
            statusFilter.addItem(st.getDisplayName());
        }
        filterRow.add(statusFilter);

        JButton searchBtn = Theme.createPrimaryButton("Filter");
        searchBtn.addActionListener(e -> applyEventFilters());

        JButton resetBtn = Theme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            eventSearchField.setText("");
            categoryFilter.setSelectedIndex(0);
            statusFilter.setSelectedIndex(0);
            loadEvents();
        });

        filterRow.add(searchBtn);
        filterRow.add(resetBtn);

        toolbar.add(actionRow);
        toolbar.add(Box.createVerticalStrut(4));
        toolbar.add(filterRow);

        panel.add(toolbar, BorderLayout.NORTH);

        // Events Table
        String[] columns = {"ID", "Code", "Title", "Category", "Date & Time", "Venue", "Capacity", "Remaining", "Status"};
        eventTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        eventTable = new JTable(eventTableModel);
        Theme.styleTable(eventTable);

        // Column widths
        eventTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(90);
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        eventTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        eventTable.getColumnModel().getColumn(4).setPreferredWidth(125);
        eventTable.getColumnModel().getColumn(5).setPreferredWidth(140);
        eventTable.getColumnModel().getColumn(6).setPreferredWidth(65);
        eventTable.getColumnModel().getColumn(7).setPreferredWidth(75);
        eventTable.getColumnModel().getColumn(8).setPreferredWidth(85);

        panel.add(new JScrollPane(eventTable), BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // TAB 2: All Registrations & Reports
    // ==========================================
    private JPanel createRegistrationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 4, 4, 4));

        JPanel toolbar = Theme.createCardPanel();
        toolbar.setLayout(new BorderLayout(10, 0));

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterLeft.setOpaque(false);

        filterLeft.add(new JLabel("Search Registrations:"));
        regSearchField = Theme.createTextField(16);
        regSearchField.setToolTipText("Filter by student name, code, or event...");
        filterLeft.add(regSearchField);

        JButton filterRegBtn = Theme.createPrimaryButton("Search");
        filterRegBtn.addActionListener(e -> applyRegFilter());

        JButton refreshRegBtn = Theme.createSecondaryButton("Refresh");
        refreshRegBtn.addActionListener(e -> loadRegistrations());

        filterLeft.add(filterRegBtn);
        filterLeft.add(refreshRegBtn);
        toolbar.add(filterLeft, BorderLayout.WEST);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Reg Code", "Student Name", "Roll / ID", "Department", "Event Code", "Event Title", "Date Registered", "Status"};
        regTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        regTable = new JTable(regTableModel);
        Theme.styleTable(regTable);
        panel.add(new JScrollPane(regTable), BorderLayout.CENTER);

        return panel;
    }

    // ==========================================
    // TAB 3: Students Directory
    // ==========================================
    private JPanel createStudentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 4, 4, 4));

        // --- Top action bar ---
        JPanel toolbar = Theme.createCardPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 8));
        toolbar.setBorder(new EmptyBorder(8, 12, 8, 12));

        JLabel titleLabel = new JLabel("Enrolled Students Directory:");
        titleLabel.setFont(Theme.FONT_BOLD);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);

        JButton refreshStudentsBtn = Theme.createSecondaryButton("🔄 Refresh");
        refreshStudentsBtn.addActionListener(e -> loadStudents());

        JButton deleteStudentBtn = Theme.createDangerButton("🗑 Delete Selected Student");
        deleteStudentBtn.setFont(Theme.FONT_BOLD);
        deleteStudentBtn.setToolTipText("Select any student from the table below, then click here to delete");
        deleteStudentBtn.addActionListener(e -> handleDeleteStudent());

        JLabel tipLabel = new JLabel("  (Click a student row to select, then click 'Delete Selected Student')");
        tipLabel.setFont(Theme.FONT_SMALL);
        tipLabel.setForeground(Theme.TEXT_SECONDARY);

        toolbar.add(titleLabel);
        toolbar.add(refreshStudentsBtn);
        toolbar.add(deleteStudentBtn);
        toolbar.add(tipLabel);

        panel.add(toolbar, BorderLayout.NORTH);

        // --- Student Table ---
        String[] cols = {"User ID", "Student ID / Roll No", "Full Name", "Department", "Email", "Phone", "Account Created"};
        studentTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        studentTable = new JTable(studentTableModel);
        Theme.styleTable(studentTable);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Right-click context menu on table
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteMenuItem = new JMenuItem("🗑 Delete Student Account");
        deleteMenuItem.setFont(Theme.FONT_BOLD);
        deleteMenuItem.setForeground(Theme.DANGER);
        deleteMenuItem.addActionListener(e -> handleDeleteStudent());
        popupMenu.add(deleteMenuItem);

        studentTable.setComponentPopupMenu(popupMenu);
        studentTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                int row = studentTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    studentTable.setRowSelectionInterval(row, row);
                }
            }
        });

        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        return panel;
    }

    private void handleDeleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a student from the table first.",
                "Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = Integer.parseInt(studentTableModel.getValueAt(selectedRow, 0).toString());
        String studentRoll = studentTableModel.getValueAt(selectedRow, 1).toString();
        String studentName = studentTableModel.getValueAt(selectedRow, 2).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to permanently delete this student account?\n\n" +
            "  Name : " + studentName + "\n" +
            "  Roll : " + studentRoll + "\n\n" +
            "All their event registrations will also be deleted (CASCADE).\n" +
            "This action CANNOT be undone!",
            "Confirm Delete Student",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                com.college.eventmanager.dao.UserDao userDao = new com.college.eventmanager.dao.UserDaoImpl();
                boolean ok = userDao.delete(userId);
                if (ok) {
                    JOptionPane.showMessageDialog(this,
                        "Student account for '" + studentName + "' has been deleted successfully.",
                        "Deleted",
                        JOptionPane.INFORMATION_MESSAGE);
                    refreshAllData();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Could not delete the account. It may have already been removed.",
                        "Delete Failed",
                        JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Failed to delete student: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==========================================
    // Data Loading and Event Handlers
    // ==========================================
    public void refreshAllData() {
        loadStats();
        loadEvents();
        loadRegistrations();
        loadStudents();
    }

    private void loadStats() {
        try {
            DashboardStats stats = eventService.getDashboardStats();
            totalEventsCard.setValue(String.valueOf(stats.getTotalEvents()));
            upcomingEventsCard.setValue(String.valueOf(stats.getUpcomingEvents()));
            registrationsCard.setValue(String.valueOf(stats.getTotalRegistrations()));
            availableSeatsCard.setValue(String.valueOf(stats.getTotalAvailableSeats()));
            studentsCard.setValue(String.valueOf(stats.getTotalStudents()));
        } catch (Exception e) {
            System.err.println("Could not load stats: " + e.getMessage());
        }
    }

    private void loadEvents() {
        try {
            currentEventList = eventService.getAllEvents();
            populateEventTable(currentEventList);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load events: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateEventTable(List<Event> list) {
        eventTableModel.setRowCount(0);
        for (Event ev : list) {
            eventTableModel.addRow(new Object[]{
                ev.getEventId(),
                ev.getEventCode(),
                ev.getTitle(),
                ev.getCategory().getDisplayName(),
                ev.getFormattedDateTime(),
                ev.getVenue(),
                ev.getMaxCapacity(),
                ev.getAvailableSeats(),
                ev.getStatus().getDisplayName()
            });
        }
    }

    private void applyEventFilters() {
        String query = eventSearchField.getText().trim();
        String cat = (String) categoryFilter.getSelectedItem();
        String st = (String) statusFilter.getSelectedItem();

        EventStatus status = null;
        if (st != null && !"All Statuses".equalsIgnoreCase(st)) {
            status = EventStatus.fromString(st);
        }

        try {
            currentEventList = eventService.searchEvents(query, cat, status);
            populateEventTable(currentEventList);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Filter error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRegistrations() {
        try {
            currentRegList = registrationService.getAllRegistrations();
            populateRegTable(currentRegList);
        } catch (Exception e) {
            System.err.println("Failed to load registrations: " + e.getMessage());
        }
    }

    private void populateRegTable(List<Registration> list) {
        regTableModel.setRowCount(0);
        for (Registration r : list) {
            regTableModel.addRow(new Object[]{
                r.getRegistrationCode(),
                r.getStudentName(),
                r.getStudentIdNumber() != null ? r.getStudentIdNumber() : "N/A",
                r.getStudentDepartment() != null ? r.getStudentDepartment() : "N/A",
                r.getEventCode(),
                r.getEventTitle(),
                r.getFormattedRegistrationDate(),
                r.getStatus().getDisplayName()
            });
        }
    }

    private void applyRegFilter() {
        String q = regSearchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            populateRegTable(currentRegList);
            return;
        }

        List<Registration> filtered = new ArrayList<>();
        for (Registration r : currentRegList) {
            if ((r.getRegistrationCode() != null && r.getRegistrationCode().toLowerCase().contains(q)) ||
                (r.getStudentName() != null && r.getStudentName().toLowerCase().contains(q)) ||
                (r.getEventTitle() != null && r.getEventTitle().toLowerCase().contains(q)) ||
                (r.getStudentIdNumber() != null && r.getStudentIdNumber().toLowerCase().contains(q))) {
                filtered.add(r);
            }
        }
        populateRegTable(filtered);
    }

    private void loadStudents() {
        try {
            com.college.eventmanager.dao.UserDao userDao = new com.college.eventmanager.dao.UserDaoImpl();
            List<Student> students = userDao.findAllStudents();
            studentTableModel.setRowCount(0);
            for (Student s : students) {
                studentTableModel.addRow(new Object[]{
                    s.getUserId(),
                    s.getStudentIdNumber() != null ? s.getStudentIdNumber() : "N/A",
                    s.getFullName(),
                    s.getDepartment() != null ? s.getDepartment() : "N/A",
                    s.getEmail(),
                    s.getPhone() != null ? s.getPhone() : "N/A",
                    s.getCreatedAt() != null ? s.getCreatedAt().toString() : "N/A"
                });
            }
        } catch (Exception e) {
            System.err.println("Could not load student directory: " + e.getMessage());
        }
    }

    private Event getSelectedEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int eventId = (Integer) eventTableModel.getValueAt(selectedRow, 0);
        return currentEventList.stream().filter(e -> e.getEventId() == eventId).findFirst().orElse(null);
    }

    private void openAddEventDialog() {
        EventDialog dialog = new EventDialog(this, eventService, null, admin.getUserId());
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            refreshAllData();
        }
    }

    private void openEditEventDialog() {
        Event selected = getSelectedEvent();
        if (selected != null) {
            EventDialog dialog = new EventDialog(this, eventService, selected, admin.getUserId());
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                refreshAllData();
            }
        }
    }

    private void openRegisteredStudentsDialog() {
        Event selected = getSelectedEvent();
        if (selected != null) {
            new RegisteredStudentsDialog(this, registrationService, selected).setVisible(true);
        }
    }

    private void handleCancelEvent() {
        Event selected = getSelectedEvent();
        if (selected == null) return;

        if (selected.getStatus() == EventStatus.CANCELLED) {
            JOptionPane.showMessageDialog(this, "Event '" + selected.getTitle() + "' is already cancelled.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel event '" + selected.getTitle() + "'?\n" +
            "This will mark all active student registrations as cancelled.",
            "Confirm Event Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = eventService.cancelEvent(selected.getEventId());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Event has been cancelled and student registrations updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshAllData();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to cancel event: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteEvent() {
        Event selected = getSelectedEvent();
        if (selected == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to permanently delete event '" + selected.getTitle() + "'?\n" +
            "All registration records for this event will also be deleted.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = eventService.deleteEvent(selected.getEventId());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Event deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
                    refreshAllData();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Failed to delete event: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleDeleteAdminAccount() {
        // Password confirmation first
        JPasswordField pwField = new JPasswordField(20);
        JPanel confirmPanel = new JPanel(new BorderLayout(0, 8));
        confirmPanel.add(new JLabel("<html><b>⚠ WARNING: This will permanently delete your Admin account!</b><br>" +
            "Events you created will remain but orphaned (created_by = NULL).<br><br>" +
            "Enter your password to confirm:</html>"), BorderLayout.NORTH);
        confirmPanel.add(pwField, BorderLayout.CENTER);

        int result = JOptionPane.showConfirmDialog(
            this,
            confirmPanel,
            "Confirm Admin Account Deletion",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) return;

        String enteredPassword = new String(pwField.getPassword());
        if (enteredPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty.", "Cancelled", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Verify password before deleting
        com.college.eventmanager.service.AuthService authService = new com.college.eventmanager.service.AuthService();
        try {
            authService.authenticate(admin.getUsername(), enteredPassword);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Incorrect password. Account deletion cancelled.",
                "Authentication Failed",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Final confirmation
        int finalConfirm = JOptionPane.showConfirmDialog(this,
            "FINAL CONFIRMATION\n\nDelete admin account permanently?\n\n" +
            "  Name    : " + admin.getFullName() + "\n" +
            "  Username: " + admin.getUsername() + "\n\n" +
            "This CANNOT be undone!",
            "Final Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);

        if (finalConfirm != JOptionPane.YES_OPTION) return;

        try {
            com.college.eventmanager.dao.UserDao userDao = new com.college.eventmanager.dao.UserDaoImpl();
            boolean ok = userDao.delete(admin.getUserId());
            if (ok) {
                JOptionPane.showMessageDialog(this,
                    "Admin account deleted successfully.\nYou will now be redirected to the login screen.",
                    "Account Deleted",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
                SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
            } else {
                JOptionPane.showMessageDialog(this,
                    "Could not delete account. Please try again.",
                    "Delete Failed",
                    JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error deleting account: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
