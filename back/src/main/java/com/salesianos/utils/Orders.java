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
 * Utilidad para la gestión de órdenes de compra.
 */
public class Orders {

    private static final Logger LOGGER = Logger.getLogger(Orders.class.getName());

    /**
     * Obtiene las órdenes de un departamento con todos sus detalles requeridos.
     */
    public List<Map<String, String>> getOrdersByDept(String depNombre) {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT oc.idOrden, oc.numero_orden, oc.numero_plan, oc.Cantidad, oc.Tipo, oc.Estado, " +
                     "oc.fechaCreacion, oc.Observaciones, oc.Inversion, " +
                     "(SELECT COUNT(*) FROM facturas f WHERE f.idOrdenCompra = oc.idOrden) as numFacturas " +
                     "FROM ordencompra oc " +
                     "JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto " +
                     "JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                     "WHERE d.Nombre = ? OR ? = 'Admin'";
        
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, depNombre);
            stmt.setString(2, depNombre);

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i);
                        row.put(metaData.getColumnLabel(i), value == null ? "" : value);
                    }
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting orders for dept: " + depNombre, e);
        }
        return list;
    }

    public List<Map<String, String>> getAllOrders() {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT oc.idOrden, oc.numero_orden, oc.numero_plan, oc.Cantidad, oc.Tipo, oc.Estado, " +
                     "oc.fechaCreacion, oc.Observaciones, oc.Inversion, " +
                     "(SELECT COUNT(*) FROM facturas f WHERE f.idOrdenCompra = oc.idOrden) as numFacturas " +
                     "FROM ordencompra oc";
        
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String value = rs.getString(i);
                        row.put(metaData.getColumnLabel(i), value == null ? "" : value);
                    }
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all orders", e);
        }
        return list;
    }

    /**
     * Obtiene el detalle completo de una orden: datos base + productos (con proveedor) + facturas.
     * Devuelve un JSON string construido manualmente.
     */
    public String getOrderDetail(int orderId) {
        StringBuilder json = new StringBuilder();
        json.append("{\"status\":\"success\",");

        // 1) Order base data
        String sqlOrder = "SELECT oc.*, p.Codigo as presupuesto_codigo, p.Nombre as presupuesto_nombre, " +
                          "d.Nombre as dep_nombre, d.Codigo as dep_codigo " +
                          "FROM ordencompra oc " +
                          "JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto " +
                          "JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                          "WHERE oc.idOrden = ?";

        try (Connection conn = DatabaseManager.getConnection("webapp")) {

            // Order
            try (PreparedStatement stmt = conn.prepareStatement(sqlOrder)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        json.append("\"order\":{");
                        json.append("\"idOrden\":\"").append(rs.getInt("idOrden")).append("\",");
                        json.append("\"numero_orden\":\"").append(esc(rs.getString("numero_orden"))).append("\",");
                        json.append("\"numero_plan\":\"").append(esc(rs.getString("numero_plan"))).append("\",");
                        json.append("\"Cantidad\":\"").append(rs.getBigDecimal("Cantidad")).append("\",");
                        json.append("\"Inversion\":\"").append(rs.getBoolean("Inversion")).append("\",");
                        json.append("\"Tipo\":\"").append(esc(rs.getString("Tipo"))).append("\",");
                        json.append("\"Observaciones\":\"").append(esc(rs.getString("Observaciones"))).append("\",");
                        json.append("\"Estado\":\"").append(esc(rs.getString("Estado"))).append("\",");
                        json.append("\"fechaCreacion\":\"").append(esc(rs.getString("fechaCreacion"))).append("\",");
                        json.append("\"presupuesto_codigo\":\"").append(esc(rs.getString("presupuesto_codigo"))).append("\",");
                        json.append("\"presupuesto_nombre\":\"").append(esc(rs.getString("presupuesto_nombre"))).append("\",");
                        json.append("\"dep_nombre\":\"").append(esc(rs.getString("dep_nombre"))).append("\",");
                        json.append("\"dep_codigo\":\"").append(esc(rs.getString("dep_codigo"))).append("\"");
                        json.append("},");
                    } else {
                        return JsonUtil.errorJson("Orden no encontrada");
                    }
                }
            }

            // 2) Products with their suppliers
            String sqlProducts = "SELECT ocp.PrecioUnitario, pr.idProducto, pr.Nombre as producto_nombre, pr.Descripcion, " +
                                 "prov.idProveedor, prov.Nombre as proveedor_nombre " +
                                 "FROM ordencompraproductos ocp " +
                                 "JOIN productos pr ON ocp.idProducto = pr.idProducto " +
                                 "LEFT JOIN productosproveedores pp ON pr.idProducto = pp.idProducto " +
                                 "LEFT JOIN proveedores prov ON pp.idProveedor = prov.idProveedor " +
                                 "WHERE ocp.idOrdenCompra = ?";

            json.append("\"productos\":[");
            try (PreparedStatement stmt = conn.prepareStatement(sqlProducts)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) json.append(",");
                        json.append("{");
                        json.append("\"idProducto\":\"").append(rs.getInt("idProducto")).append("\",");
                        json.append("\"nombre\":\"").append(esc(rs.getString("producto_nombre"))).append("\",");
                        json.append("\"descripcion\":\"").append(esc(rs.getString("Descripcion"))).append("\",");
                        json.append("\"precioUnitario\":\"").append(rs.getBigDecimal("PrecioUnitario")).append("\",");
                        json.append("\"proveedor\":\"").append(esc(rs.getString("proveedor_nombre"))).append("\"");
                        json.append("}");
                        first = false;
                    }
                }
            }
            json.append("],");

            // 3) Invoices (just metadata, not the blob)
            String sqlFacturas = "SELECT idFactura, fechaCreacion FROM facturas WHERE idOrdenCompra = ?";
            json.append("\"facturas\":[");
            try (PreparedStatement stmt = conn.prepareStatement(sqlFacturas)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    boolean first = true;
                    while (rs.next()) {
                        if (!first) json.append(",");
                        json.append("{");
                        json.append("\"idFactura\":\"").append(rs.getInt("idFactura")).append("\",");
                        json.append("\"fechaCreacion\":\"").append(esc(rs.getString("fechaCreacion"))).append("\"");
                        json.append("}");
                        first = false;
                    }
                }
            }
            json.append("]}");

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting order detail for id: " + orderId, e);
            return JsonUtil.errorJson("Error: " + e.getMessage());
        }

        return json.toString();
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    public long createOrderWithId(Map<String, String> data) {
        String sql = "INSERT INTO ordencompra (idPresupuesto, numero_orden, numero_plan, Cantidad, Inversion, Tipo, Observaciones, Estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'Pendiente')";
        
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            String rawBudget = data.get("idPresupuesto");
            if (rawBudget == null || rawBudget.isEmpty()) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, Integer.parseInt(rawBudget));
            }
            stmt.setString(2, data.get("numero_orden") != null ? data.get("numero_orden") : "");
            stmt.setString(3, data.get("numero_plan") != null ? data.get("numero_plan") : "");
            
            String rawCant = data.get("Cantidad");
            double cantidad = 0.0;
            try {
                if (rawCant != null && !rawCant.isEmpty()) cantidad = Double.parseDouble(rawCant);
            } catch (Exception e) {
                LOGGER.warning("Could not parse Quantity: " + rawCant);
            }
            stmt.setDouble(4, cantidad);
            
            stmt.setBoolean(5, "true".equalsIgnoreCase(data.get("Inversion")) || "1".equals(data.get("Inversion")));
            stmt.setString(6, data.get("Tipo"));
            stmt.setString(7, data.get("Observaciones") != null ? data.get("Observaciones") : "");

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getLong(1);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating order", e);
        }
        return -1;
    }

    public void addProductToOrder(long orderId, String productId, String price) {
        String sql = "INSERT INTO ordencompraproductos (idOrdenCompra, idProducto, PrecioUnitario) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setInt(2, Integer.parseInt(productId));
            double priceVal = 0.0;
            try { if (price != null && !price.isEmpty()) priceVal = Double.parseDouble(price); } catch (Exception e) {}
            stmt.setDouble(3, priceVal);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding product to order", e);
        }
    }

    public void addInvoice(long orderId, byte[] fileData) {
        String sql = "INSERT INTO facturas (idOrdenCompra, blobFactura) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setBytes(2, fileData);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding invoice to order", e);
        }
    }

    public String getNextOrderSequence(String dept, String year) {
        String yearShort = year.length() > 2 ? year.substring(year.length() - 2) : year;
        String pattern = dept + "/%/" + yearShort + "/%";
        String sql = "SELECT numero_orden FROM ordencompra WHERE numero_orden LIKE ? ORDER BY idOrden DESC LIMIT 1";
        
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String lastOrder = rs.getString("numero_orden");
                    String[] parts = lastOrder.split("/");
                    if (parts.length >= 2) {
                        try {
                            int nextVal = Integer.parseInt(parts[1]) + 1;
                            return String.format("%03d", nextVal);
                        } catch (Exception e) {
                            LOGGER.warning("Could not parse sequence from: " + lastOrder);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting next sequence", e);
        }
        return "001";
    }

    public String createOrder(Map<String, String> data) {
        long id = createOrderWithId(data);
        return id > 0 ? JsonUtil.messageJson("Orden creada correctamente") : JsonUtil.errorJson("No se pudo crear la orden");
    }
}
