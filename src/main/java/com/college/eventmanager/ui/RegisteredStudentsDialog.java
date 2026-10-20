package com.college.eventmanager.ui;

import com.college.eventmanager.model.Event;
import com.college.eventmanager.model.Registration;
import com.college.eventmanager.service.RegistrationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dialog displaying all student registrations for a specific event.
 */
public class RegisteredStudentsDialog extends JDialog {
    private final JTable table;
    private final DefaultTableModel tableModel;
    private final RegistrationService registrationService;
    private final Event event;

    public RegisteredStudentsDialog(Frame parent, RegistrationService registrationService, Event event) {
        super(parent, "Registered Students - " + event.getTitle(), true);
        this.registrationService = registrationService;
        this.event = event;

        setSize(750, 480);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_MAIN);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Registrations: " + event.getTitle());
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Color.WHITE);

        JLabel sub = new JLabel("Event Code: " + event.getEventCode() + " • Venue: " + event.getVenue() + " • Date: " + event.getFormattedDateTime());
        sub.setFont(Theme.FONT_SMALL);
        sub.setForeground(new Color(224, 238, 255));

        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Table
        String[] columns = {"Reg Code", "Student Name", "Student ID", "Department", "Email", "Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        Theme.styleTable(table);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(12, 16, 12, 16));
        centerPanel.setOpaque(false);
        centerPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.BG_MAIN);
        footer.setBorder(new EmptyBorder(8, 16, 12, 16));

        JLabel countLabel = new JLabel("Loading students...");
        countLabel.setFont(Theme.FONT_BOLD);
        countLabel.setForeground(Theme.TEXT_SECONDARY);

        JButton closeBtn = Theme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());

        footer.add(countLabel, BorderLayout.WEST);
        footer.add(closeBtn, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        // Load data
        loadData(countLabel);
    }

    private void loadData(JLabel countLabel) {
        try {
            List<Registration> list = registrationService.getEventRegistrations(event.getEventId());
            tableModel.setRowCount(0);
            for (Registration r : list) {
                tableModel.addRow(new Object[]{
                    r.getRegistrationCode(),
                    r.getStudentName(),
                    r.getStudentIdNumber() != null ? r.getStudentIdNumber() : "N/A",
                    r.getStudentDepartment() != null ? r.getStudentDepartment() : "N/A",
                    r.getStudentEmail(),
                    r.getFormattedRegistrationDate(),
                    r.getStatus().getDisplayName()
                });
            }
            countLabel.setText("Total Registrations: " + list.size() + " (Capacity: " + event.getMaxCapacity() + ")");
        } catch (Exception e) {
            countLabel.setText("Error loading registrations: " + e.getMessage());
        }
    }
}
