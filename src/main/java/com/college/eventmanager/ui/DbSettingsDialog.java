package com.college.eventmanager.ui;

import com.college.eventmanager.config.DatabaseConnection;
import com.college.eventmanager.config.DbConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog allowing users and evaluators to inspect database connectivity,
 * edit credentials, and test connection without editing code or restarting.
 */
public class DbSettingsDialog extends JDialog {
    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField nameField;
    private final JTextField userField;
    private final JPasswordField passField;
    private final JLabel statusLabel;
    private final JTextArea detailsArea;
    private final Runnable onSettingsUpdated;

    public DbSettingsDialog(Frame parent, Runnable onSettingsUpdated) {
        super(parent, "Database Configuration & Diagnostics", true);
        this.onSettingsUpdated = onSettingsUpdated;
        setSize(520, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(Theme.BG_MAIN);

        DbConfig config = DatabaseConnection.getConfig();

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.PRIMARY_DARK);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel title = new JLabel("MySQL Database Settings");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Configure your local MySQL / XAMPP connection parameters");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(new Color(203, 213, 225));

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = Theme.createCardPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        hostField = Theme.createTextField(15);
        hostField.setText(config.getHost());

        portField = Theme.createTextField(6);
        portField.setText(String.valueOf(config.getPort()));

        nameField = Theme.createTextField(15);
        nameField.setText(config.getName());

        userField = Theme.createTextField(15);
        userField.setText(config.getUser());

        passField = Theme.createPasswordField(15);
        passField.setText(config.getPassword());

        int row = 0;
        addFormRow(formPanel, gbc, row++, "Host:", hostField);
        addFormRow(formPanel, gbc, row++, "Port:", portField);
        addFormRow(formPanel, gbc, row++, "Database Name:", nameField);
        addFormRow(formPanel, gbc, row++, "Username:", userField);
        addFormRow(formPanel, gbc, row++, "Password:", passField);

        // Status & Details
        statusLabel = new JLabel("Status: Untested");
        statusLabel.setFont(Theme.FONT_BOLD);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);

        detailsArea = new JTextArea(4, 30);
        detailsArea.setEditable(false);
        detailsArea.setFont(Theme.FONT_SMALL);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setBackground(Theme.BG_MAIN);
        detailsArea.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        formPanel.add(statusLabel, gbc);

        gbc.gridy = row++;
        formPanel.add(new JScrollPane(detailsArea), gbc);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(12, 16, 12, 16));
        centerWrapper.add(formPanel, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        actionPanel.setBackground(Theme.BG_MAIN);
        actionPanel.setBorder(new EmptyBorder(0, 16, 12, 16));

        JButton testBtn = Theme.createSecondaryButton("Test Connection");
        JButton saveBtn = Theme.createPrimaryButton("Save & Apply");
        JButton initBtn = Theme.createSuccessButton("Initialize Tables");
        JButton closeBtn = Theme.createSecondaryButton("Close");

        testBtn.addActionListener(e -> runConnectionTest());
        saveBtn.addActionListener(e -> saveSettings());
        initBtn.addActionListener(e -> initializeDatabaseTables());
        closeBtn.addActionListener(e -> dispose());

        actionPanel.add(testBtn);
        actionPanel.add(initBtn);
        actionPanel.add(saveBtn);
        actionPanel.add(closeBtn);
        add(actionPanel, BorderLayout.SOUTH);

        // Run initial test
        SwingUtilities.invokeLater(this::runConnectionTest);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 1;
        gbc.weightx = 0.3;
        JLabel label = new JLabel(labelText);
        label.setFont(Theme.FONT_BOLD);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(comp, gbc);
    }

    private void runConnectionTest() {
        DatabaseConnection.ConnectionStatus status = DatabaseConnection.testConnection();
        if (status.isConnected()) {
            statusLabel.setText("Status: CONNECTED");
            statusLabel.setForeground(Theme.SUCCESS);
            detailsArea.setText(status.getMessage() + "\n" + status.getDetails());
        } else {
            statusLabel.setText("Status: DISCONNECTED");
            statusLabel.setForeground(Theme.DANGER);
            detailsArea.setText(status.getMessage() + "\n\n" + status.getDetails());
        }
    }

    private void saveSettings() {
        try {
            int port = Integer.parseInt(portField.getText().trim());
            DatabaseConnection.getConfig().save(
                hostField.getText().trim(),
                port,
                nameField.getText().trim(),
                userField.getText().trim(),
                new String(passField.getPassword())
            );
            JOptionPane.showMessageDialog(this, 
                "Database settings saved to db.properties successfully!", 
                "Configuration Saved", 
                JOptionPane.INFORMATION_MESSAGE);
            runConnectionTest();
            if (onSettingsUpdated != null) {
                onSettingsUpdated.run();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Failed to save settings: " + ex.getMessage(), 
                "Save Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeDatabaseTables() {
        boolean success = DatabaseConnection.autoInitializeTables();
        if (success) {
            JOptionPane.showMessageDialog(this, 
                "Tables (users, events, registrations) verified / initialized successfully!", 
                "Tables Initialized", 
                JOptionPane.INFORMATION_MESSAGE);
            runConnectionTest();
        } else {
            JOptionPane.showMessageDialog(this, 
                "Failed to auto-initialize tables. Please verify database connection and ensure MySQL user has CREATE permissions, or execute db/setup_database.sql manually.", 
                "Initialization Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
