package com.salesianos.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static Properties properties = new Properties();
    
    private static final int POOL_SIZE = 10;
    private static final ArrayBlockingQueue<Connection> connectionPool = new ArrayBlockingQueue<>(POOL_SIZE);

    static {
        try (InputStream input = DatabaseManager.class.getClassLoader()
                 .getResourceAsStream("db.properties")) {
            if (input == null) {
                LOGGER.severe("Unable to find db.properties in the classpath.");
                throw new RuntimeException("Database configuration file not found.");
            }
            properties.load(input);
            Class.forName(properties.getProperty("db.driver"));
            
            // Initialize the pool with default webapp credentials
            String dbUrl = properties.getProperty("db.url");
            String user = properties.getProperty("db.user.webapp");
            String password = properties.getProperty("db.password.webapp");
            
            for (int i = 0; i < POOL_SIZE; i++) {
                connectionPool.add(DriverManager.getConnection(dbUrl, user, password));
            }
            LOGGER.info("Connection pool initialized with " + POOL_SIZE + " connections.");
        } catch (IOException | ClassNotFoundException | SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database properties or pool", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = connectionPool.poll(3, TimeUnit.SECONDS);
            if (conn == null || conn.isClosed() || !conn.isValid(1)) {
                String dbUrl = properties.getProperty("db.url");
                String user = properties.getProperty("db.user.webapp");
                String password = properties.getProperty("db.password.webapp");
                conn = DriverManager.getConnection(dbUrl, user, password);
            }
            return wrapConnection(conn);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for a database connection", e);
        }
    }

    private static Connection wrapConnection(final Connection realConnection) {
        return (Connection) Proxy.newProxyInstance(
                DatabaseManager.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("close".equals(method.getName())) {
                            if (connectionPool.size() < POOL_SIZE) {
                                if (realConnection.isValid(1)) {
                                    connectionPool.offer(realConnection);
                                    return null;
                                }
                            }
                            realConnection.close();
                            return null;
                        }
                        return method.invoke(realConnection, args);
                    }
                });
    }
}
