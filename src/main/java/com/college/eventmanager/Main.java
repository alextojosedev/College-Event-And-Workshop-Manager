package com.college.eventmanager;

import com.college.eventmanager.config.DatabaseConnection;
import com.college.eventmanager.ui.LoginFrame;
import com.college.eventmanager.ui.Theme;

import javax.swing.*;

/**
 * Main application entry point for College Event and Workshop Manager.
 */
public class Main {
    public static void main(String[] args) {
        // Set system properties for high-DPI scaling on Windows
        System.setProperty("sun.java2d.uiScale.enabled", "true");

        // Initialize UI Theme (FlatLaf with fallback)
        Theme.initializeLookAndFeel();

        // Check and optionally initialize tables in background
        new Thread(() -> {
            try {
                DatabaseConnection.ConnectionStatus status = DatabaseConnection.testConnection();
                if (status.isConnected()) {
                    DatabaseConnection.autoInitializeTables();
                }
            } catch (Exception e) {
                System.err.println("Background DB check notice: " + e.getMessage());
            }
        }).start();

        // Launch Login Frame on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
