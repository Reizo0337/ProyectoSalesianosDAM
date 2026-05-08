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
        String sql = "SELECT * FROM proveedores ORDER BY Nombre ASC";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnLabel(i).toLowerCase(), rs.getString(i));
                }
                list.add(row);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting suppliers", e);
        }
        return list;
    }

    public List<Map<String, String>> getSupplierProducts(int idProveedor) {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT p.* FROM productos p JOIN productosProveedores pp ON p.idProducto = pp.idProducto WHERE pp.idProveedor = ?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProveedor);
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(metaData.getColumnLabel(i).toLowerCase(), rs.getString(i));
                    }
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting supplier products", e);
        }
        return list;
    }

    public String createSupplier(Map<String, String> data) {
        String sql = "INSERT INTO proveedores (Nombre, Telefono, Direccion) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, data.get("Nombre"));
            stmt.setString(2, data.get("Telefono"));
            stmt.setString(3, data.get("Direccion"));

            int rows = stmt.executeUpdate();
            return rows > 0 ? JsonUtil.messageJson("Proveedor creado") : JsonUtil.errorJson("Error al crear");
        } catch (SQLException e) {
            return JsonUtil.errorJson("Error DB: " + e.getMessage());
        }
    }

    public String updateSupplier(int id, Map<String, String> data) {
        String sql = "UPDATE proveedores SET Nombre=?, Telefono=?, Direccion=? WHERE idProveedor=?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, data.get("Nombre"));
            stmt.setString(2, data.get("Telefono"));
            stmt.setString(3, data.get("Direccion"));
            stmt.setInt(4, id);

            int rows = stmt.executeUpdate();
            return rows > 0 ? JsonUtil.messageJson("Proveedor actualizado") : JsonUtil.errorJson("No se encontró el proveedor");
        } catch (SQLException e) {
            return JsonUtil.errorJson("Error DB: " + e.getMessage());
        }
    }

    public String deleteSupplier(int id) {
        try (Connection conn = DatabaseManager.getConnection("webapp")) {
            // First delete associations
            String sqlAssoc = "DELETE FROM productosProveedores WHERE idProveedor=?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlAssoc)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
            
            String sql = "DELETE FROM proveedores WHERE idProveedor=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rows = stmt.executeUpdate();
                return rows > 0 ? JsonUtil.messageJson("Proveedor eliminado") : JsonUtil.errorJson("No se encontró el proveedor");
            }
        } catch (SQLException e) {
            return JsonUtil.errorJson("Error DB: " + e.getMessage());
        }
    }

    public String assignProduct(int idProveedor, int idProducto) {
        String sql = "INSERT IGNORE INTO productosProveedores (idProveedor, idProducto) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProveedor);
            stmt.setInt(2, idProducto);
            stmt.executeUpdate();
            return JsonUtil.messageJson("Producto asignado");
        } catch (SQLException e) {
            return JsonUtil.errorJson("Error DB: " + e.getMessage());
        }
    }

    public String removeProduct(int idProveedor, int idProducto) {
        String sql = "DELETE FROM productosProveedores WHERE idProveedor=? AND idProducto=?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProveedor);
            stmt.setInt(2, idProducto);
            stmt.executeUpdate();
            return JsonUtil.messageJson("Producto desvinculado");
        } catch (SQLException e) {
            return JsonUtil.errorJson("Error DB: " + e.getMessage());
        }
    }
}
