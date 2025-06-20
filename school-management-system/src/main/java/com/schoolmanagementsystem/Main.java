package com.schoolmanagementsystem;

import com.schoolmanagementsystem.view.MainWindow;
import com.schoolmanagementsystem.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.*;
/**
 * Main entry point for the School Management System application
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting School Management System...");

        // Set System Look and Feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Could not set system look and feel", e);
        }

        // Initialize database connection
        try {
            DatabaseManager.getInstance().testConnection();
            logger.info("Database connection established successfully");
        } catch (Exception e) {
            logger.error("Failed to establish database connection", e);
            JOptionPane.showMessageDialog(null, 
                "Failed to connect to database. Please check your configuration.", 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Launch the main application window
        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow mainWindow = new MainWindow();
                mainWindow.setVisible(true);
                logger.info("School Management System started successfully");
            } catch (Exception e) {
                logger.error("Failed to start application", e);
                JOptionPane.showMessageDialog(null, 
                    "Failed to start application: " + e.getMessage(), 
                    "Application Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}