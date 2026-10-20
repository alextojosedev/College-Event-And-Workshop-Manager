package com.college.eventmanager.ui;

import com.college.eventmanager.exception.AppException;
import com.college.eventmanager.model.Student;
import com.college.eventmanager.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog allowing students to register a new account from the login screen.
 */
public class RegisterStudentDialog extends JDialog {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JTextField fullNameField;
    private final JTextField emailField;
    private final JTextField phoneField;
    private final JTextField studentIdField;
    private final JComboBox<String> deptCombo;
    private final AuthService authService;
    private boolean registered = false;

    private static final String[] DEPARTMENTS = {
        "Computer Science & Engineering",
        "Information Technology",
        "Electronics & Communication",
        "Electrical & Electronics",
        "Mechanical Engineering",
        "Civil Engineering",
        "Biotechnology",
        "Master of Computer Applications (MCA)",
        "Business Administration (MBA)"
    };

    public RegisterStudentDialog(Frame parent, AuthService authService) {
        super(parent, "Student Account Registration", true);
        this.authService = authService;
        setSize(480, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BG_MAIN);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Theme.PRIMARY);
        header.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("Create Student Account");
        title.setFont(Theme.FONT_HEADER);
        title.setForeground(Color.WHITE);
        JLabel subtitle = new JLabel("Register once to participate in college events & workshops");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(new Color(224, 238, 255));
        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Form
        JPanel card = Theme.createCardPanel();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        fullNameField = Theme.createTextField(15);
        studentIdField = Theme.createTextField(15);
        deptCombo = new JComboBox<>(DEPARTMENTS);
        deptCombo.setFont(Theme.FONT_REGULAR);
        deptCombo.setBackground(Color.WHITE);
        emailField = Theme.createTextField(15);
        phoneField = Theme.createTextField(15);
        usernameField = Theme.createTextField(15);
        passwordField = Theme.createPasswordField(15);

        int row = 0;
        addFormRow(card, gbc, row++, "Full Name *:", fullNameField);
        addFormRow(card, gbc, row++, "Student ID / Roll No *:", studentIdField);
        addFormRow(card, gbc, row++, "Department *:", deptCombo);
        addFormRow(card, gbc, row++, "College Email *:", emailField);
        addFormRow(card, gbc, row++, "Phone Number:", phoneField);
        addFormRow(card, gbc, row++, "Choose Username *:", usernameField);
        addFormRow(card, gbc, row++, "Choose Password *:", passwordField);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.setBorder(new EmptyBorder(12, 16, 12, 16));
        centerWrap.add(card, BorderLayout.CENTER);
        add(centerWrap, BorderLayout.CENTER);

        // Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        footer.setBackground(Theme.BG_MAIN);
        JButton submitBtn = Theme.createPrimaryButton("Create Account");
        JButton cancelBtn = Theme.createSecondaryButton("Cancel");

        submitBtn.addActionListener(e -> handleRegistration());
        cancelBtn.addActionListener(e -> dispose());

        footer.add(cancelBtn);
        footer.add(submitBtn);
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

    private void handleRegistration() {
        try {
            Student s = authService.registerStudent(
                usernameField.getText().trim(),
                new String(passwordField.getPassword()),
                fullNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim(),
                studentIdField.getText().trim(),
                (String) deptCombo.getSelectedItem()
            );

            registered = true;
            JOptionPane.showMessageDialog(this,
                "Account created successfully for " + s.getFullName() + "!\nYou can now log in with your username.",
                "Registration Success",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } catch (AppException ex) {
            JOptionPane.showMessageDialog(this,
                ex.getMessage(),
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Failed to register account: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isRegistered() {
        return registered;
    }

    public String getRegisteredUsername() {
        return usernameField.getText().trim();
    }
}
