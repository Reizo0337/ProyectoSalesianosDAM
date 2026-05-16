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

public class Products {

    private static final Logger LOGGER = Logger.getLogger(Products.class.getName());

    public List<Map<String, String>> getAllProducts() {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT p.idProducto, p.Nombre, p.Descripcion, " +
                     "(SELECT prov.Nombre FROM proveedores prov JOIN productosProveedores pp ON prov.idProveedor = pp.idProveedor WHERE pp.idProducto = p.idProducto LIMIT 1) as proveedor, " +
                     "(SELECT AVG(PrecioUnitario) FROM ordencompraproductos WHERE idProducto = p.idProducto) as precio_medio " +
                     "FROM productos p " +
                     "ORDER BY p.Nombre ASC";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String col = metaData.getColumnLabel(i);
                    row.put(col.toLowerCase(), rs.getString(i));
                }
                list.add(row);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting products", e);
        }
        return list;
    }

    public String createProduct(Map<String, String> data) {
        String sqlProd = "INSERT INTO productos (Nombre, Descripcion) VALUES (?, ?)";
        String sqlAssoc = "INSERT INTO productosProveedores (idProducto, idProveedor) VALUES (?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection("webapp")) {
             long productId = -1;
             try (PreparedStatement stmt = conn.prepareStatement(sqlProd, PreparedStatement.RETURN_GENERATED_KEYS)) {
                 stmt.setString(1, data.get("nombre"));
                 stmt.setString(2, data.get("descripcion") != null ? data.get("descripcion") : "");
                 int rows = stmt.executeUpdate();
                 if (rows > 0) {
                     try (ResultSet gk = stmt.getGeneratedKeys()) {
                         if (gk.next()) productId = gk.getLong(1);
                     }
                 }
             }
             
             if (productId > 0) {
                 String idProv = data.get("idProveedor");
                 if (idProv != null && !idProv.isEmpty()) {
                     try (PreparedStatement stmt = conn.prepareStatement(sqlAssoc)) {
                         stmt.setLong(1, productId);
                         stmt.setInt(2, Integer.parseInt(idProv));
                         stmt.executeUpdate();
                     }
                 }
                 return "{\"status\":\"success\",\"message\":\"Producto creado\",\"idProducto\":\"" + productId + "\"}";
             }
             return JsonUtil.errorJson("No se pudo crear el producto");
        } catch (SQLException e) {
            return JsonUtil.errorJson("Error DB: " + e.getMessage());
        }
    }

    public String updateProduct(int id, Map<String, String> data) {
        String sql = "UPDATE productos SET Nombre=?, Descripcion=? WHERE idProducto=?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, data.get("nombre"));
            stmt.setString(2, data.get("descripcion"));
            stmt.setInt(3, id);

            int rows = stmt.executeUpdate();
            
            // Update association if idProveedor is provided
            String idProv = data.get("idProveedor");
            if (idProv != null && !idProv.isEmpty()) {
                // Remove old associations first
                String delSql = "DELETE FROM productosProveedores WHERE idProducto=?";
                try (PreparedStatement delStmt = conn.prepareStatement(delSql)) {
                    delStmt.setInt(1, id);
                    delStmt.executeUpdate();
                }
                // Add new one
                String insSql = "INSERT INTO productosProveedores (idProducto, idProveedor) VALUES (?, ?)";
                try (PreparedStatement insStmt = conn.prepareStatement(insSql)) {
                    insStmt.setInt(1, id);
                    insStmt.setInt(2, Integer.parseInt(idProv));
                    insStmt.executeUpdate();
                }
            }
            
            return rows > 0 ? JsonUtil.messageJson("Producto actualizado") : JsonUtil.errorJson("No se encontró el producto");
        } catch (SQLException e) {
            return JsonUtil.errorJson("Error DB: " + e.getMessage());
        }
    }

    public String deleteProduct(int id) {
        try (Connection conn = DatabaseManager.getConnection("webapp")) {
            // First delete associations
            String delSql = "DELETE FROM productosProveedores WHERE idProducto=?";
            try (PreparedStatement delStmt = conn.prepareStatement(delSql)) {
                delStmt.setInt(1, id);
                delStmt.executeUpdate();
            }
            
            // Delete product
            String sql = "DELETE FROM productos WHERE idProducto=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                int rows = stmt.executeUpdate();
                return rows > 0 ? JsonUtil.messageJson("Producto eliminado") : JsonUtil.errorJson("No se encontró el producto");
            }
        } catch (SQLException e) {
            return JsonUtil.errorJson("Error DB: " + e.getMessage());
        }
    }
}
