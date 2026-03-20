package com.salesianos.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBConnector {

	private static final Logger LOGGER = Logger.getLogger(DBConnector.class.getName());
    private final String role;

    public DBConnector(String role) {
        this.role = role;
    }

    public DBConnector() {
        this("webapp");
    }

    public int executeUpdate(String sql, Object... params) {
        try (Connection conn = DatabaseManager.getConnection(role);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            setParameters(stmt, params);
            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Update successful: " + rowsAffected + " rows affected.");
            return rowsAffected;
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Update error: " + sql, e);
            throw new RuntimeException("Database error during update operation", e);
        }
    }

    public void executeQuery(String sql, Object... params) {
        try (Connection conn = DatabaseManager.getConnection(role);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setParameters(stmt, params);
            
            try (ResultSet rs = stmt.executeQuery()) {
                int columns = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    StringBuilder row = new StringBuilder();
                    for (int i = 1; i <= columns; i++) {
                        row.append(rs.getString(i)).append("\t");
                    }
                    LOGGER.info("Query row: " + row.toString());
                }
            }
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "SQL Query error: " + sql, e);
            throw new RuntimeException("Database error during query operation", e);
        }
    }

    private void setParameters(PreparedStatement stmt, Object... params) throws SQLException {
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
        }
    }
}
