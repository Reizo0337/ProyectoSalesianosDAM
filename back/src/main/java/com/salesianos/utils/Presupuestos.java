package com.salesianos.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Presupuestos {

    private static final Logger LOGGER = Logger.getLogger(Presupuestos.class.getName());

    public List<Map<String, String>> getAllPresupuestos() {
        String sql = "SELECT * FROM presupuesto";
        List<Map<String, String>> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, String> p = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String col = metaData.getColumnLabel(i);
                    Object val = rs.getObject(i);
                    // Map to lowercase for frontend consistency
                    p.put(col.toLowerCase(), val != null ? val.toString() : "");
                }
                list.add(p);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all presupuestos", e);
        }
        return list;
    }

    public List<Map<String, String>> getPresupuestosByDept(String dep) {
        if (dep == null) {
            throw new IllegalArgumentException("department cannot be null");
        }

        String sql = "CALL obtener_ordenes_por_departamento(?);";
        List<Map<String, String>> list = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection("webapp");
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dep);

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                while (rs.next()) {
                    Map<String, String> p = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String col = metaData.getColumnLabel(i);
                        Object val = rs.getObject(i);
                        // Map to lowercase for frontend consistency
                        p.put(col.toLowerCase(), val != null ? val.toString() : "");
                    }
                    list.add(p);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting presupuestos by dept: " + dep, e);
        }
        return list;
    }
}
