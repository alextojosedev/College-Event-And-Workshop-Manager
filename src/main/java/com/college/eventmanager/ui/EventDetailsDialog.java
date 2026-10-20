package com.college.eventmanager.ui;

import com.college.eventmanager.model.Event;
import com.college.eventmanager.model.EventStatus;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modal dialog displaying detailed information about a selected event.
 */
public class EventDetailsDialog extends JDialog {
    private boolean registerRequested = false;

    public EventDetailsDialog(Frame parent, Event event, boolean showRegisterButton) {
        super(parent, "Event Details: " + event.getTitle(), true);
        setSize(520, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_MAIN);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel(event.getTitle());
        titleLabel.setFont(Theme.FONT_HEADER);
        titleLabel.setForeground(Color.WHITE);

        JLabel catLabel = new JLabel(event.getEventCode() + " • " + event.getCategory().getDisplayName());
        catLabel.setFont(Theme.FONT_SMALL);
        catLabel.setForeground(new Color(203, 213, 225));

        header.add(titleLabel, BorderLayout.NORTH);
        header.add(catLabel, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Content
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        addDetailItem(card, "Date & Time", event.getFormattedDateTime());
        addDetailItem(card, "Venue", event.getVenue());
        addDetailItem(card, "Status", event.getStatus().getDisplayName());
        addDetailItem(card, "Capacity", event.getMaxCapacity() + " seats");
        addDetailItem(card, "Available Seats", event.getAvailableSeats() + " remaining");

        // Seat capacity progress bar
        int booked = event.getBookedSeats();
        int max = event.getMaxCapacity();
        int percent = (int) Math.round(((double) booked / max) * 100);

        JLabel progLabel = new JLabel("Seat Occupancy: " + booked + "/" + max + " (" + percent + "%)");
        progLabel.setFont(Theme.FONT_SMALL);
        progLabel.setForeground(Theme.TEXT_SECONDARY);
        progLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JProgressBar bar = new JProgressBar(0, max);
        bar.setValue(booked);
        bar.setStringPainted(true);
        bar.setString(booked + " of " + max + " seats reserved");
        bar.setMaximumSize(new Dimension(Short.MAX_VALUE, 22));
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (event.getAvailableSeats() == 0) {
            bar.setForeground(Theme.DANGER);
        } else if (percent > 75) {
            bar.setForeground(Theme.WARNING);
        } else {
            bar.setForeground(Theme.PRIMARY);
        }

        card.add(Box.createVerticalStrut(8));
        card.add(progLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(bar);
        card.add(Box.createVerticalStrut(12));

        JLabel descTitle = new JLabel("Description");
        descTitle.setFont(Theme.FONT_BOLD);
        descTitle.setForeground(Theme.TEXT_PRIMARY);
        descTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descArea = new JTextArea(event.getDescription() != null && !event.getDescription().isEmpty() 
            ? event.getDescription() : "No description provided.");
        descArea.setFont(Theme.FONT_REGULAR);
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setBackground(new Color(248, 250, 252));
        descArea.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(440, 100));
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(descTitle);
        card.add(Box.createVerticalStrut(4));
        card.add(descScroll);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(new EmptyBorder(12, 16, 12, 16));
        centerWrap.add(card, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Theme.BG_MAIN);

        if (showRegisterButton && event.getStatus() == EventStatus.UPCOMING && event.getAvailableSeats() > 0) {
            JButton regBtn = Theme.createSuccessButton("Register for this Event");
            regBtn.addActionListener(e -> {
                registerRequested = true;
                dispose();
            });
            footer.add(regBtn);
        }

        JButton closeBtn = Theme.createSecondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());
        footer.add(closeBtn);

        add(footer, BorderLayout.SOUTH);
    }

    private void addDetailItem(JPanel panel, String label, String value) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Short.MAX_VALUE, 26));

        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(Theme.FONT_BOLD);
        lbl.setForeground(Theme.TEXT_SECONDARY);
        lbl.setPreferredSize(new Dimension(130, 24));

        JLabel val = new JLabel(value);
        val.setFont(Theme.FONT_REGULAR);
        val.setForeground(Theme.TEXT_PRIMARY);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);

        panel.add(row);
        panel.add(Box.createVerticalStrut(4));
    }

    public boolean isRegisterRequested() {
        return registerRequested;
    }
}
