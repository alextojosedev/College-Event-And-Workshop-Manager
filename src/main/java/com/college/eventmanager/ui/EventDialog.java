package com.college.eventmanager.ui;

import com.college.eventmanager.exception.AppException;
import com.college.eventmanager.model.Event;
import com.college.eventmanager.model.EventCategory;
import com.college.eventmanager.model.EventStatus;
import com.college.eventmanager.service.EventService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Dialog for creating or modifying an event or workshop.
 */
public class EventDialog extends JDialog {
    private final JTextField codeField;
    private final JTextField titleField;
    private final JComboBox<EventCategory> categoryCombo;
    private final JTextField dateTimeField;
    private final JTextField venueField;
    private final JSpinner capacitySpinner;
    private final JComboBox<EventStatus> statusCombo;
    private final JTextArea descArea;

    private final EventService eventService;
    private final Event existingEvent;
    private final Integer currentUserId;
    private boolean saved = false;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public EventDialog(Frame parent, EventService eventService, Event eventToEdit, Integer currentUserId) {
        super(parent, eventToEdit == null ? "Create New Event / Workshop" : "Edit Event: " + eventToEdit.getTitle(), true);
        this.eventService = eventService;
        this.existingEvent = eventToEdit;
        this.currentUserId = currentUserId;

        setSize(560, 620);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_MAIN);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(new EmptyBorder(14, 20, 14, 20));
        JLabel title = new JLabel(eventToEdit == null ? "New Event / Workshop" : "Update Event Details");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.NORTH);
        add(header, BorderLayout.NORTH);

        // Form Card
        JPanel card = Theme.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 6, 5, 6);

        codeField = Theme.createTextField(15);
        titleField = Theme.createTextField(15);
        categoryCombo = new JComboBox<>(EventCategory.values());
        categoryCombo.setFont(Theme.FONT_REGULAR);
        categoryCombo.setBackground(Color.WHITE);

        dateTimeField = Theme.createTextField(15);
        dateTimeField.setToolTipText("Format: YYYY-MM-DD HH:MM (e.g. 2026-11-25 14:30)");

        venueField = Theme.createTextField(15);

        SpinnerNumberModel capacityModel = new SpinnerNumberModel(50, 1, 5000, 5);
        capacitySpinner = new JSpinner(capacityModel);
        capacitySpinner.setFont(Theme.FONT_REGULAR);

        statusCombo = new JComboBox<>(EventStatus.values());
        statusCombo.setFont(Theme.FONT_REGULAR);
        statusCombo.setBackground(Color.WHITE);

        descArea = new JTextArea(4, 25);
        descArea.setFont(Theme.FONT_REGULAR);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        int row = 0;
        addFormRow(card, gbc, row++, "Event Code *:", codeField);
        addFormRow(card, gbc, row++, "Title *:", titleField);
        addFormRow(card, gbc, row++, "Category *:", categoryCombo);
        addFormRow(card, gbc, row++, "Date & Time (YYYY-MM-DD HH:MM) *:", dateTimeField);
        addFormRow(card, gbc, row++, "Venue *:", venueField);
        addFormRow(card, gbc, row++, "Maximum Capacity *:", capacitySpinner);
        addFormRow(card, gbc, row++, "Status:", statusCombo);

        gbc.gridx = 0; gbc.gridy = row++; gbc.weightx = 0.3;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(Theme.FONT_BOLD);
        card.add(descLabel, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        card.add(new JScrollPane(descArea), gbc);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(new EmptyBorder(12, 16, 12, 16));
        centerWrap.add(card, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        // Populate fields if editing
        if (existingEvent != null) {
            codeField.setText(existingEvent.getEventCode());
            codeField.setEditable(false); // Do not alter primary event code once created
            titleField.setText(existingEvent.getTitle());
            categoryCombo.setSelectedItem(existingEvent.getCategory());
            dateTimeField.setText(existingEvent.getDateTime().format(FORMATTER));
            venueField.setText(existingEvent.getVenue());
            capacitySpinner.setValue(existingEvent.getMaxCapacity());
            statusCombo.setSelectedItem(existingEvent.getStatus());
            descArea.setText(existingEvent.getDescription());
        } else {
            // Default future time suggestion
            dateTimeField.setText(LocalDateTime.now().plusDays(7).withHour(10).withMinute(0).format(FORMATTER));
        }

        // Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Theme.BG_MAIN);
        JButton saveBtn = Theme.createPrimaryButton(existingEvent == null ? "Create Event" : "Save Changes");
        JButton cancelBtn = Theme.createSecondaryButton("Cancel");

        saveBtn.addActionListener(e -> handleSave());
        cancelBtn.addActionListener(e -> dispose());

        footer.add(cancelBtn);
        footer.add(saveBtn);
        add(footer, BorderLayout.SOUTH);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(Theme.FONT_BOLD);
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(comp, gbc);
    }

    private void handleSave() {
        String code = codeField.getText().trim();
        String title = titleField.getText().trim();
        EventCategory category = (EventCategory) categoryCombo.getSelectedItem();
        String dtStr = dateTimeField.getText().trim();
        String venue = venueField.getText().trim();
        int capacity = (Integer) capacitySpinner.getValue();
        EventStatus status = (EventStatus) statusCombo.getSelectedItem();
        String desc = descArea.getText().trim();

        if (code.isEmpty() || title.isEmpty() || venue.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields (Code, Title, Venue).",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDateTime dt;
        try {
            dt = LocalDateTime.parse(dtStr, FORMATTER);
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, 
                "Invalid Date & Time format. Please use 'YYYY-MM-DD HH:MM' (e.g. 2026-11-20 14:00).",
                "Date Format Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            if (existingEvent == null) {
                Event newEvent = new Event();
                newEvent.setEventCode(code);
                newEvent.setTitle(title);
                newEvent.setCategory(category);
                newEvent.setDateTime(dt);
                newEvent.setVenue(venue);
                newEvent.setMaxCapacity(capacity);
                newEvent.setStatus(status);
                newEvent.setDescription(desc);
                newEvent.setCreatedBy(currentUserId);

                eventService.createEvent(newEvent);
                JOptionPane.showMessageDialog(this, "Event created successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                existingEvent.setTitle(title);
                existingEvent.setCategory(category);
                existingEvent.setDateTime(dt);
                existingEvent.setVenue(venue);
                existingEvent.setMaxCapacity(capacity);
                existingEvent.setStatus(status);
                existingEvent.setDescription(desc);

                eventService.updateEvent(existingEvent);
                JOptionPane.showMessageDialog(this, "Event updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
            saved = true;
            dispose();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Operation Failed", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage(), "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
