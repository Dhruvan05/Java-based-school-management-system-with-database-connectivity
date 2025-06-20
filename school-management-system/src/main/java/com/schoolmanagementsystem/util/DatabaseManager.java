package com.schoolmanagementsystem.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Simple Database connection manager using plain JDBC (no connection pool)
 */
public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);
    private static DatabaseManager instance;
    private final Properties properties = new Properties();

    private String url;
    private String username;
    private String password;
    private String driver;

    private DatabaseManager() {
        loadProperties();
        initialize();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new IOException("application.properties not found");
            }
            properties.load(input);

            url = properties.getProperty("db.url");
            username = properties.getProperty("db.username");
            password = properties.getProperty("db.password");
            driver = properties.getProperty("db.driver");

        } catch (IOException e) {
            logger.error("Error loading database configuration", e);
            throw new RuntimeException("Could not load DB config", e);
        }
    }

    private void initialize() {
        try {
            Class.forName(driver); // Load JDBC driver
            logger.info("JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load JDBC driver", e);
            throw new RuntimeException("Driver class not found", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    public void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            logger.info("Database connection test successful");
        }
    }

    public void close() {
        // Nothing to close in plain JDBC unless you're managing pooled resources
        logger.info("No pooled connections to close (JDBC)");
    }
}
