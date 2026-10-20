package com.college.eventmanager.ui;

import com.college.eventmanager.config.DatabaseConnection;
import com.college.eventmanager.exception.AppException;
import com.college.eventmanager.model.Admin;
import com.college.eventmanager.model.Student;
import com.college.eventmanager.model.User;
import com.college.eventmanager.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * Modern Java Swing Login Window with role-based routing, database connectivity status,
 * and student self-registration support.
 */
public class LoginFrame extends JFrame {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JLabel dbStatusLabel;
    private final JButton dbConfigBtn;
    private final AuthService authService;

    public LoginFrame() {
        super("College Event & Workshop Manager - Login");
        this.authService = new AuthService();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(480, 580);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_MAIN);

        // Top Banner
        JPanel bannerPanel = new JPanel();
        bannerPanel.setBackground(Theme.PRIMARY_DARK);
        bannerPanel.setLayout(new BoxLayout(bannerPanel, BoxLayout.Y_AXIS));
        bannerPanel.setBorder(new EmptyBorder(24, 24, 20, 24));

        JLabel titleLabel = new JLabel("Campus Events & Workshops");
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitleLabel = new JLabel("College Event & Workshop Management System");
        subTitleLabel.setFont(Theme.FONT_SMALL);
        subTitleLabel.setForeground(new Color(203, 213, 225));
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        bannerPanel.add(titleLabel);
        bannerPanel.add(Box.createVerticalStrut(4));
        bannerPanel.add(subTitleLabel);
        add(bannerPanel, BorderLayout.NORTH);

        // Center Login Card
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(16, 24, 16, 24));

        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));
        card.setPreferredSize(new Dimension(380, 320));

        JLabel loginHeader = new JLabel("Sign In to Your Account");
        loginHeader.setFont(Theme.FONT_HEADER);
        loginHeader.setForeground(Theme.TEXT_PRIMARY);
        loginHeader.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel userLabel = new JLabel("Username / Student ID");
        userLabel.setFont(Theme.FONT_BOLD);
        userLabel.setForeground(Theme.TEXT_SECONDARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        usernameField = Theme.createTextField(20);
        usernameField.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.FONT_BOLD);
        passLabel.setForeground(Theme.TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        passwordField = Theme.createPasswordField(20);
        passwordField.setMaximumSize(new Dimension(Short.MAX_VALUE, 36));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField.addActionListener(e -> handleLogin());

        JButton loginBtn = Theme.createPrimaryButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(Short.MAX_VALUE, 38));
        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.addActionListener(e -> handleLogin());

        // Quick demo credentials helper
        JPanel demoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        demoPanel.setOpaque(false);
        demoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton adminDemoBtn = new JButton("Fill Admin");
        adminDemoBtn.setFont(Theme.FONT_SMALL);
        adminDemoBtn.addActionListener(e -> {
            usernameField.setText("admin");
            passwordField.setText("admin123");
        });

        JButton studentDemoBtn = new JButton("Fill Student");
        studentDemoBtn.setFont(Theme.FONT_SMALL);
        studentDemoBtn.addActionListener(e -> {
            usernameField.setText("student1");
            passwordField.setText("student123");
        });

        demoPanel.add(new JLabel("Demo:"));
        demoPanel.add(adminDemoBtn);
        demoPanel.add(studentDemoBtn);

        // Student Sign Up Button
        JButton registerLink = new JButton("New Student? Register here");
        registerLink.setFont(Theme.FONT_BOLD);
        registerLink.setForeground(Theme.PRIMARY);
        registerLink.setBorderPainted(false);
        registerLink.setContentAreaFilled(false);
        registerLink.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerLink.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerLink.addActionListener(e -> openStudentRegistration());

        card.add(loginHeader);
        card.add(Box.createVerticalStrut(16));
        card.add(userLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(12));
        card.add(passLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(passwordField);
        card.add(Box.createVerticalStrut(18));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(demoPanel);
        card.add(Box.createVerticalStrut(8));
        card.add(registerLink);

        centerWrapper.add(card);
        add(centerWrapper, BorderLayout.CENTER);

        // Bottom Status Bar
        JPanel statusBar = new JPanel(new BorderLayout(8, 0));
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            new EmptyBorder(6, 16, 6, 16)
        ));

        dbStatusLabel = new JLabel("Checking MySQL Connection...");
        dbStatusLabel.setFont(Theme.FONT_SMALL);

        dbConfigBtn = new JButton("Configure Database");
        dbConfigBtn.setFont(Theme.FONT_SMALL);
        dbConfigBtn.setFocusPainted(false);
        dbConfigBtn.addActionListener(e -> {
            DbSettingsDialog dialog = new DbSettingsDialog(this, this::checkDatabaseStatus);
            dialog.setVisible(true);
        });

        statusBar.add(dbStatusLabel, BorderLayout.WEST);
        statusBar.add(dbConfigBtn, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        // Initial connection check in background
        SwingUtilities.invokeLater(this::checkDatabaseStatus);
    }

    private void checkDatabaseStatus() {
        DatabaseConnection.ConnectionStatus status = DatabaseConnection.testConnection();
        if (status.isConnected()) {
            dbStatusLabel.setText("● MySQL Connected (" + DatabaseConnection.getConfig().getHost() + ":" + DatabaseConnection.getConfig().getPort() + ")");
            dbStatusLabel.setForeground(Theme.SUCCESS);
        } else {
            dbStatusLabel.setText("● MySQL Disconnected: " + status.getMessage());
            dbStatusLabel.setForeground(Theme.DANGER);
        }
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password.",
                "Login Incomplete",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = authService.authenticate(username, password);
            JOptionPane.showMessageDialog(this,
                "Welcome back, " + user.getFullName() + "!\nLogged in as: " + user.getRoleDisplayName(),
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE);

            this.dispose();

            if (user instanceof Admin admin) {
                SwingUtilities.invokeLater(() -> new AdminDashboard(admin).setVisible(true));
            } else if (user instanceof Student student) {
                SwingUtilities.invokeLater(() -> new StudentDashboard(student).setVisible(true));
            }

        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Authentication Failed",
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            DatabaseConnection.ConnectionStatus status = DatabaseConnection.testConnection();
            String extra = status.isConnected() ? "" : "\n\nDatabase is currently offline. Click 'Configure Database' below.";
            JOptionPane.showMessageDialog(this,
                "Database error during authentication: " + ex.getMessage() + extra,
                "Connection / System Error",
                JOptionPane.ERROR_MESSAGE);
            checkDatabaseStatus();
        }
    }

    private void openStudentRegistration() {
        RegisterStudentDialog dialog = new RegisterStudentDialog(this, authService);
        dialog.setVisible(true);
        if (dialog.isRegistered()) {
            usernameField.setText(dialog.getRegisteredUsername());
            passwordField.setText("");
            passwordField.requestFocusInWindow();
        }
    }
}
