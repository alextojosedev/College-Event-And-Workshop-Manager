package com.college.eventmanager.ui;

import com.college.eventmanager.exception.AppException;
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
 * Modern Java Swing Dashboard for Students.
 * Enables students to discover events/workshops, view remaining seats,
 * register with real-time seat decrement, and manage their personal registrations.
 */
public class StudentDashboard extends JFrame {
    private final Student student;
    private final EventService eventService;
    private final RegistrationService registrationService;

    // Stat Cards
    private StatCard availableEventsCard;
    private StatCard myRegistrationsCard;
    private StatCard collegeRollCard;

    // Browse Events Tab
    private JTable eventTable;
    private DefaultTableModel eventTableModel;
    private List<Event> currentUpcomingEvents = new ArrayList<>();
    private JTextField searchField;
    private JComboBox<String> categoryCombo;

    // My Registrations Tab
    private JTable regTable;
    private DefaultTableModel regTableModel;
    private List<Registration> myRegistrationsList = new ArrayList<>();

    public StudentDashboard(Student student) {
        super("Campus Event Portal - Student Dashboard (" + student.getFullName() + ")");
        this.student = student;
        this.eventService = new EventService();
        this.registrationService = new RegistrationService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 700);
        setMinimumSize(new Dimension(900, 580));
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
        refreshAll();
    }

    private JPanel createTopNavbar() {
        JPanel nav = new JPanel(new BorderLayout());
        nav.setBackground(Theme.PRIMARY_DARK);
        nav.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel brand = new JLabel("College Event & Workshop Manager");
        brand.setFont(Theme.FONT_HEADER);
        brand.setForeground(Color.WHITE);

        JLabel roleBadge = new JLabel("  STUDENT PORTAL  ");
        roleBadge.setFont(Theme.FONT_SMALL);
        roleBadge.setOpaque(true);
        roleBadge.setBackground(Theme.ACCENT);
        roleBadge.setForeground(Color.WHITE);
        roleBadge.setBorder(new EmptyBorder(3, 6, 3, 6));

        left.add(brand);
        left.add(roleBadge);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel userLabel = new JLabel("Welcome, " + student.getFullName() + " (" + 
            (student.getStudentIdNumber() != null ? student.getStudentIdNumber() : "ID: " + student.getUserId()) + ")");
        userLabel.setFont(Theme.FONT_REGULAR);
        userLabel.setForeground(new Color(203, 213, 225));

        JButton logoutBtn = Theme.createDangerButton("Log Out");
        logoutBtn.addActionListener(e -> handleLogout());

        right.add(userLabel);
        right.add(logoutBtn);

        nav.add(left, BorderLayout.WEST);
        nav.add(right, BorderLayout.EAST);
        return nav;
    }

    private JPanel createStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 14, 0));
        row.setOpaque(false);

        availableEventsCard = new StatCard("Available Events", "0", "Open for Registration", Theme.PRIMARY);
        myRegistrationsCard = new StatCard("My Confirmed Bookings", "0", "Active Passes", Theme.SUCCESS);
        collegeRollCard = new StatCard("Department", student.getDepartment() != null ? student.getDepartment() : "General", 
            "Roll: " + (student.getStudentIdNumber() != null ? student.getStudentIdNumber() : "N/A"), Theme.ACCENT);

        row.add(availableEventsCard);
        row.add(myRegistrationsCard);
        row.add(collegeRollCard);
        return row;
    }

    private JTabbedPane createTabbedContent() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_SUBHEADER);
        tabs.setBackground(Theme.BG_MAIN);

        tabs.addTab("Upcoming Events & Workshops", createBrowseEventsPanel());
        tabs.addTab("My Registrations & Tickets", createMyRegistrationsPanel());
        tabs.addTab("Student Profile", createProfilePanel());

        return tabs;
    }

    // ==========================================
    // TAB 1: Browse Events
    // ==========================================
    private JPanel createBrowseEventsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 4, 4, 4));

        // Search & Filter Toolbar
        JPanel toolbar = Theme.createCardPanel();
        toolbar.setLayout(new BorderLayout(10, 0));

        JPanel filterLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        filterLeft.setOpaque(false);

        filterLeft.add(new JLabel("Search Events:"));
        searchField = Theme.createTextField(16);
        searchField.setToolTipText("Filter by name, venue, description...");
        filterLeft.add(searchField);

        filterLeft.add(new JLabel("Category:"));
        categoryCombo = new JComboBox<>();
        categoryCombo.addItem("All Categories");
        for (EventCategory cat : EventCategory.values()) {
            categoryCombo.addItem(cat.getDisplayName());
        }
        filterLeft.add(categoryCombo);

        JButton filterBtn = Theme.createPrimaryButton("Search");
        filterBtn.addActionListener(e -> applyFilters());

        JButton resetBtn = Theme.createSecondaryButton("Reset");
        resetBtn.addActionListener(e -> {
            searchField.setText("");
            categoryCombo.setSelectedIndex(0);
            loadUpcomingEvents();
        });

        filterLeft.add(filterBtn);
        filterLeft.add(resetBtn);
        toolbar.add(filterLeft, BorderLayout.WEST);

        // Action Buttons
        JPanel actionRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionRight.setOpaque(false);

        JButton detailsBtn = Theme.createSecondaryButton("View Details");
        detailsBtn.addActionListener(e -> viewSelectedEventDetails());

        JButton registerBtn = Theme.createSuccessButton("Register for Event");
        registerBtn.addActionListener(e -> handleRegisterSelectedEvent());

        actionRight.add(detailsBtn);
        actionRight.add(registerBtn);
        toolbar.add(actionRight, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"Event Code", "Title", "Category", "Date & Time", "Venue", "Total Seats", "Remaining", "Status"};
        eventTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        eventTable = new JTable(eventTableModel);
        Theme.styleTable(eventTable);

        eventTable.getColumnModel().getColumn(0).setPreferredWidth(100);
        eventTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        eventTable.getColumnModel().getColumn(2).setPreferredWidth(110);
        eventTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        eventTable.getColumnModel().getColumn(4).setPreferredWidth(140);
        eventTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        eventTable.getColumnModel().getColumn(6).setPreferredWidth(80);
        eventTable.getColumnModel().getColumn(7).setPreferredWidth(90);

        panel.add(new JScrollPane(eventTable), BorderLayout.CENTER);
        return panel;
    }

    // ==========================================
    // TAB 2: My Registrations
    // ==========================================
    private JPanel createMyRegistrationsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(10, 4, 4, 4));

        JPanel toolbar = Theme.createCardPanel();
        toolbar.setLayout(new BorderLayout(10, 0));

        JLabel info = new JLabel("Manage your booked events, download/view details, or cancel your seat reservation.");
        info.setFont(Theme.FONT_REGULAR);
        info.setForeground(Theme.TEXT_SECONDARY);
        toolbar.add(info, BorderLayout.WEST);

        JPanel actionRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        actionRight.setOpaque(false);

        JButton viewTicketBtn = Theme.createPrimaryButton("View Pass Details");
        viewTicketBtn.addActionListener(e -> viewSelectedPass());

        JButton cancelBtn = Theme.createDangerButton("Cancel Registration");
        cancelBtn.addActionListener(e -> handleCancelMyRegistration());

        JButton refreshBtn = Theme.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> loadMyRegistrations());

        actionRight.add(viewTicketBtn);
        actionRight.add(cancelBtn);
        actionRight.add(refreshBtn);
        toolbar.add(actionRight, BorderLayout.EAST);

        panel.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Pass / Reg Code", "Event Code", "Event Title", "Category", "Date & Time", "Venue", "Registration Date", "Status"};
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
    // TAB 3: Student Profile
    // ==========================================
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        panel.setOpaque(false);

        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(480, 320));
        card.setBorder(new EmptyBorder(24, 24, 24, 24));

        JLabel title = new JLabel("Student Profile Record");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(16));

        addProfileField(card, "Full Name", student.getFullName());
        addProfileField(card, "Student ID / Roll No", student.getStudentIdNumber() != null ? student.getStudentIdNumber() : "N/A");
        addProfileField(card, "Department", student.getDepartment() != null ? student.getDepartment() : "N/A");
        addProfileField(card, "College Email", student.getEmail());
        addProfileField(card, "Contact Phone", student.getPhone() != null && !student.getPhone().isEmpty() ? student.getPhone() : "Not provided");
        addProfileField(card, "Username", student.getUsername());
        addProfileField(card, "Role Status", student.getRoleDisplayName());

        panel.add(card);
        return panel;
    }

    private void addProfileField(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(150, 24));

        JLabel val = new JLabel(value);
        val.setFont(Theme.FONT_REGULAR);
        val.setForeground(Theme.TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);

        panel.add(row);
        panel.add(Box.createVerticalStrut(4));
    }

    // ==========================================
    // Data Loading & Actions
    // ==========================================
    public void refreshAll() {
        loadUpcomingEvents();
        loadMyRegistrations();
    }

    private void loadUpcomingEvents() {
        try {
            currentUpcomingEvents = eventService.getUpcomingEvents();
            populateUpcomingTable(currentUpcomingEvents);
            availableEventsCard.setValue(String.valueOf(currentUpcomingEvents.size()));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load events: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateUpcomingTable(List<Event> list) {
        eventTableModel.setRowCount(0);
        for (Event ev : list) {
            eventTableModel.addRow(new Object[]{
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

    private void applyFilters() {
        String q = searchField.getText().trim();
        String cat = (String) categoryCombo.getSelectedItem();
        try {
            currentUpcomingEvents = eventService.searchEvents(q, cat, EventStatus.UPCOMING);
            populateUpcomingTable(currentUpcomingEvents);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Filter error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadMyRegistrations() {
        try {
            myRegistrationsList = registrationService.getStudentRegistrations(student.getUserId());
            regTableModel.setRowCount(0);
            int confirmedCount = 0;
            for (Registration r : myRegistrationsList) {
                if (r.isConfirmed()) confirmedCount++;
                regTableModel.addRow(new Object[]{
                    r.getRegistrationCode(),
                    r.getEventCode(),
                    r.getEventTitle(),
                    r.getEventCategory() != null ? r.getEventCategory() : "Event",
                    r.getFormattedEventDateTime(),
                    r.getEventVenue() != null ? r.getEventVenue() : "Main Campus",
                    r.getFormattedRegistrationDate(),
                    r.getStatus().getDisplayName()
                });
            }
            myRegistrationsCard.setValue(String.valueOf(confirmedCount));
        } catch (Exception e) {
            System.err.println("Failed to load personal registrations: " + e.getMessage());
        }
    }

    private Event getSelectedUpcomingEvent() {
        int row = eventTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an event from the list.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String eventCode = (String) eventTableModel.getValueAt(row, 0);
        return currentUpcomingEvents.stream().filter(e -> e.getEventCode().equals(eventCode)).findFirst().orElse(null);
    }

    private void viewSelectedEventDetails() {
        Event selected = getSelectedUpcomingEvent();
        if (selected != null) {
            EventDetailsDialog dialog = new EventDetailsDialog(this, selected, true);
            dialog.setVisible(true);
            if (dialog.isRegisterRequested()) {
                performRegistration(selected);
            }
        }
    }

    private void handleRegisterSelectedEvent() {
        Event selected = getSelectedUpcomingEvent();
        if (selected != null) {
            performRegistration(selected);
        }
    }

    private void performRegistration(Event event) {
        if (event.getAvailableSeats() <= 0) {
            JOptionPane.showMessageDialog(this,
                "Sorry, '" + event.getTitle() + "' is fully booked! No available seats.",
                "Event Full",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Check if already registered
        boolean already = registrationService.isStudentRegistered(student.getUserId(), event.getEventId());
        if (already) {
            JOptionPane.showMessageDialog(this,
                "You are already registered for '" + event.getTitle() + "'. Check the 'My Registrations' tab.",
                "Already Registered",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirm registration for:\n\n" +
            "Event: " + event.getTitle() + " (" + event.getEventCode() + ")\n" +
            "Category: " + event.getCategory().getDisplayName() + "\n" +
            "Date: " + event.getFormattedDateTime() + "\n" +
            "Venue: " + event.getVenue() + "\n" +
            "Remaining Seats: " + event.getAvailableSeats() + "\n\n" +
            "Do you wish to proceed?",
            "Confirm Seat Reservation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Registration reg = registrationService.registerStudent(
                    student.getUserId(),
                    event.getEventId(),
                    "Self registration via Student Portal"
                );

                JOptionPane.showMessageDialog(this,
                    "Registration successful!\n\n" +
                    "Registration Pass Code: " + reg.getRegistrationCode() + "\n" +
                    "Student: " + student.getFullName() + "\n" +
                    "Event: " + event.getTitle() + "\n\n" +
                    "Your seat is reserved. Please save your registration code for event check-in.",
                    "Booking Confirmed",
                    JOptionPane.INFORMATION_MESSAGE);

                refreshAll();

            } catch (AppException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Registration Incomplete", JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "System error during registration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewSelectedPass() {
        int row = regTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a registration from the table.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String code = (String) regTableModel.getValueAt(row, 0);
        Registration reg = myRegistrationsList.stream().filter(r -> r.getRegistrationCode().equals(code)).findFirst().orElse(null);
        if (reg != null) {
            String details = "========================================\n" +
                             "     CAMPUS EVENT PASS / TICKET         \n" +
                             "========================================\n\n" +
                             "Pass Code:       " + reg.getRegistrationCode() + "\n" +
                             "Student Name:    " + student.getFullName() + "\n" +
                             "Roll / ID:       " + (student.getStudentIdNumber() != null ? student.getStudentIdNumber() : "N/A") + "\n" +
                             "Department:      " + (student.getDepartment() != null ? student.getDepartment() : "N/A") + "\n\n" +
                             "Event Title:     " + reg.getEventTitle() + "\n" +
                             "Event Code:      " + reg.getEventCode() + "\n" +
                             "Event Category:  " + reg.getEventCategory() + "\n" +
                             "Date & Time:     " + reg.getFormattedEventDateTime() + "\n" +
                             "Venue:           " + reg.getEventVenue() + "\n\n" +
                             "Booked On:       " + reg.getFormattedRegistrationDate() + "\n" +
                             "Status:          " + reg.getStatus().getDisplayName() + "\n" +
                             "========================================\n";

            JTextArea area = new JTextArea(details);
            area.setFont(new Font("Consolas", Font.PLAIN, 12));
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Registration Ticket Pass", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleCancelMyRegistration() {
        int row = regTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a registration to cancel.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String code = (String) regTableModel.getValueAt(row, 0);
        Registration reg = myRegistrationsList.stream().filter(r -> r.getRegistrationCode().equals(code)).findFirst().orElse(null);
        if (reg == null) return;

        if (!reg.isConfirmed()) {
            JOptionPane.showMessageDialog(this, "This registration is already cancelled.", "Notice", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to cancel your registration for:\n'" + reg.getEventTitle() + "'?\n\n" +
            "Your seat will be released back to other students immediately.",
            "Confirm Cancellation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                boolean ok = registrationService.cancelRegistration(reg.getRegistrationId(), student.getUserId());
                if (ok) {
                    JOptionPane.showMessageDialog(this, "Registration cancelled successfully. Your seat has been released.", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                    refreshAll();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to cancel registration: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
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
