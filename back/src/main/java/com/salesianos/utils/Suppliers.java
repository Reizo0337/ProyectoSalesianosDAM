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

/**
 * Utilidad para la gestión de proveedores.
 */
public class Suppliers {

    private static final Logger LOGGER = Logger.getLogger(Suppliers.class.getName());

    public List<Map<String, String>> getAllSuppliers() {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT * FROM proveedores";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i), rs.getString(i));
                }
                list.add(row);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting suppliers", e);
        }
        return list;
    }

    public String createSupplier(Map<String, String> data) {
        String sql = "INSERT INTO proveedores (Nombre, CIF_NIF, Telefono, Email, Direccion) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, data.get("Nombre"));
            stmt.setString(2, data.get("CIF_NIF"));
            stmt.setString(3, data.get("Telefono"));
            stmt.setString(4, data.get("Email"));
            stmt.setString(5, data.get("Direccion"));

            int rows = stmt.executeUpdate();
            return rows > 0 ? JsonUtil.messageJson("Proveedor creado") : JsonUtil.errorJson("Error al crear");
        } catch (SQLException e) {
            return JsonUtil.errorJson("Error DB: " + e.getMessage());
        }
    }
}
