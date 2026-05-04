package com.salesianos.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static Properties properties = new Properties();
    
    static {
        try (InputStream input = DatabaseManager.class.getClassLoader()
                 .getResourceAsStream("db.properties")) {
            if (input == null) {
                LOGGER.severe("Unable to find db.properties in the classpath.");
                throw new RuntimeException("Database configuration file not found.");
            }
            properties.load(input);

            // Load driver only once
            Class.forName(properties.getProperty("db.driver"));
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database properties", e);
            throw new ExceptionInInitializerError(e);
        }
    }
    public static Connection getConnection(String role) throws SQLException {
        String dbUrl = properties.getProperty("db.url");
        String user = properties.getProperty("db.user." + role);
        String password = properties.getProperty("db.password." + role);

        if (user == null || password == null) {
            LOGGER.warning("Invalid role requested: " + role);
            throw new SQLException("Invalid database role/credentials for role: " + role);
        }

        LOGGER.info("Attempting connection for role: " + role);
        return DriverManager.getConnection(dbUrl, user, password);
    }
    
    // Convenience method for the default webapp role
    public static Connection getWebappConnection() throws SQLException {
        return getConnection("webapp");
    }
}
