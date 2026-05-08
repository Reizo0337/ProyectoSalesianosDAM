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
     * Obtiene las órdenes de un departamento filtradas por año.
     */
    public List<Map<String, String>> getOrdersByDept(String depNombre, int year) {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT oc.idOrden, oc.numero_orden, oc.numero_plan, oc.Cantidad, oc.Tipo, oc.Estado, " +
                     "oc.fechaCreacion, oc.descripcion, oc.Inversion, d.Nombre as nombredepartamento, " +
                     "(SELECT COUNT(*) FROM facturas f WHERE f.idOrdenCompra = oc.idOrden) as numFacturas " +
                     "FROM ordencompra oc " +
                     "JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto " +
                     "JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                     "WHERE (d.Nombre = ? OR ? = 'Admin' OR ? = 'Administrador' OR ? = 'Contable') AND YEAR(oc.fechaCreacion) = ? " +
                     "ORDER BY oc.fechaCreacion DESC";
        
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, depNombre);
            stmt.setString(2, depNombre);
            stmt.setString(3, depNombre);
            stmt.setString(4, depNombre);
            stmt.setInt(5, year);

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String col = metaData.getColumnLabel(i);
                        String value = rs.getString(i);
                        row.put(col.toLowerCase(), value == null ? "" : value);
                    }
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting orders for dept: " + depNombre, e);
        }
        return list;
    }

    public List<Map<String, String>> getAllOrders(int year) {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT oc.idOrden, oc.numero_orden, oc.numero_plan, oc.Cantidad, oc.Tipo, oc.Estado, " +
                     "oc.fechaCreacion, oc.descripcion, oc.Inversion, d.Nombre as nombredepartamento, " +
                     "(SELECT COUNT(*) FROM facturas f WHERE f.idOrdenCompra = oc.idOrden) as numFacturas " +
                     "FROM ordencompra oc " +
                     "LEFT JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto " +
                     "LEFT JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                     "WHERE YEAR(oc.fechaCreacion) = ? " +
                     "ORDER BY oc.fechaCreacion DESC";
        
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);

            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        String col = metaData.getColumnLabel(i);
                        String value = rs.getString(i);
                        row.put(col.toLowerCase(), value == null ? "" : value);
                    }
                    list.add(row);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all orders", e);
        }
        return list;
    }

    public List<Integer> getYearsWithOrders() {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(fechaCreacion) as year FROM ordencompra ORDER BY year DESC";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                years.add(rs.getInt("year"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting years", e);
        }
        return years;
    }

    /**
     * Obtiene el detalle completo de una orden: datos base + productos (con proveedor) + facturas.
     * Devuelve un JSON string construido manualmente.
     */
    public String getOrderDetail(int orderId, String deptName, String userRole) {
        StringBuilder json = new StringBuilder();
        json.append("{\"status\":\"success\",");

        // 1) Order base data - Include department check
        String sqlOrder = "SELECT oc.*, p.Codigo as presupuesto_codigo, p.Nombre as presupuesto_nombre, p.type as presupuesto_tipo, " +
                          "d.Nombre as dep_nombre, d.Codigo as dep_codigo " +
                          "FROM ordencompra oc " +
                          "JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto " +
                          "JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                          "WHERE oc.idOrden = ? AND (d.Nombre = ? OR ? = 'Admin' OR ? = 'Administrador' OR ? = 'Contable')";

        try (Connection conn = DatabaseManager.getConnection("webapp")) {

            // Order
            try (PreparedStatement stmt = conn.prepareStatement(sqlOrder)) {
                stmt.setInt(1, orderId);
                stmt.setString(2, deptName);
                stmt.setString(3, userRole);
                stmt.setString(4, userRole);
                stmt.setString(5, userRole);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        json.append("\"order\":{");
                        json.append("\"idorden\":\"").append(rs.getInt("idOrden")).append("\",");
                        json.append("\"numero_orden\":\"").append(esc(rs.getString("numero_orden"))).append("\",");
                        json.append("\"numero_plan\":\"").append(esc(rs.getString("numero_plan"))).append("\",");
                        json.append("\"cantidad\":\"").append(rs.getBigDecimal("Cantidad")).append("\",");
                        json.append("\"inversion\":\"").append(rs.getBoolean("Inversion")).append("\",");
                        json.append("\"tipo\":\"").append(esc(rs.getString("Tipo"))).append("\",");
                        json.append("\"descripcion\":\"").append(esc(rs.getString("descripcion"))).append("\",");
                        json.append("\"estado\":\"").append(esc(rs.getString("Estado"))).append("\",");
                        json.append("\"fechacreacion\":\"").append(esc(rs.getString("fechaCreacion"))).append("\",");
                        json.append("\"idpresupuesto\":\"").append(rs.getInt("idPresupuesto")).append("\",");
                        json.append("\"presupuesto_codigo\":\"").append(esc(rs.getString("presupuesto_codigo"))).append("\",");
                        json.append("\"presupuesto_nombre\":\"").append(esc(rs.getString("presupuesto_nombre"))).append("\",");
                        json.append("\"presupuesto_tipo\":\"").append(esc(rs.getString("presupuesto_tipo"))).append("\",");
                        json.append("\"dep_nombre\":\"").append(esc(rs.getString("dep_nombre"))).append("\",");
                        json.append("\"dep_codigo\":\"").append(esc(rs.getString("dep_codigo"))).append("\"");
                        json.append("},");
                    } else {
                        return JsonUtil.errorJson("Orden no encontrada o acceso denegado");
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
                        json.append("\"idproducto\":\"").append(rs.getInt("idProducto")).append("\",");
                        json.append("\"nombre\":\"").append(esc(rs.getString("producto_nombre"))).append("\",");
                        json.append("\"descripcion\":\"").append(esc(rs.getString("Descripcion"))).append("\",");
                        json.append("\"preciounitario\":\"").append(rs.getBigDecimal("PrecioUnitario")).append("\",");
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
                        json.append("\"idfactura\":\"").append(rs.getInt("idFactura")).append("\",");
                        json.append("\"fechacreacion\":\"").append(esc(rs.getString("fechaCreacion"))).append("\"");
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
        String rawBudget = data.get("idPresupuesto");
        String rawCant = data.get("Cantidad");
        double amount = 0.0;
        try {
            if (rawCant != null && !rawCant.isEmpty()) amount = Double.parseDouble(rawCant);
        } catch (Exception e) {}

        // Validation: Check if there's enough budget
        if (rawBudget != null && !rawBudget.isEmpty()) {
            int budgetId = Integer.parseInt(rawBudget);
            if (!hasEnoughBudget(budgetId, amount)) {
                return -2; // Insufficient budget
            }
        }

        String sql = "INSERT INTO ordencompra (idPresupuesto, numero_orden, numero_plan, Cantidad, Inversion, Tipo, descripcion, Estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, 'Pendiente')";
        
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            if (rawBudget == null || rawBudget.isEmpty()) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, Integer.parseInt(rawBudget));
            }
            String numOrden = data.get("numero_orden");
            if (numOrden == null || numOrden.trim().isEmpty()) {
                stmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(2, numOrden);
            }

            String numPlan = data.get("numero_plan");
            if (numPlan == null || numPlan.trim().isEmpty()) {
                stmt.setNull(3, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(3, numPlan);
            }
            
            stmt.setDouble(4, amount);
            stmt.setBoolean(5, "true".equalsIgnoreCase(data.get("Inversion")) || "1".equals(data.get("Inversion")));
            stmt.setString(6, data.get("Tipo"));
            stmt.setString(7, data.get("descripcion") != null ? data.get("descripcion") : "");

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

    public String updateOrder(int id, Map<String, String> data) {
        // We need to check if the status is "Aprobada" to adjust budget gasto if amount changes
        String sqlCheck = "SELECT Cantidad, Estado, idPresupuesto FROM ordencompra WHERE idOrden = ?";
        double oldAmount = 0;
        String status = "";
        int budgetId = 0;

        try (Connection conn = DatabaseManager.getConnection("webapp")) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        oldAmount = rs.getDouble("Cantidad");
                        status = rs.getString("Estado");
                        budgetId = rs.getInt("idPresupuesto");
                    }
                }
            }

            String sql = "UPDATE ordencompra SET Cantidad=?, numero_plan=?, Tipo=?, Inversion=?, descripcion=?, idPresupuesto=? WHERE idOrden=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                double newAmount = Double.parseDouble(data.get("Cantidad"));
                stmt.setDouble(1, newAmount);
                stmt.setString(2, data.get("numero_plan"));
                stmt.setString(3, data.get("Tipo"));
                stmt.setBoolean(4, "true".equalsIgnoreCase(data.get("Inversion")) || "1".equals(data.get("Inversion")));
                stmt.setString(5, data.get("descripcion"));
                stmt.setInt(6, Integer.parseInt(data.get("idPresupuesto")));
                stmt.setInt(7, id);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    // If approved, adjust budget gasto
                    if ("Aprobada".equalsIgnoreCase(status)) {
                        double diff = newAmount - oldAmount;
                        if (diff != 0) {
                            updateBudgetGasto(budgetId, Math.abs(diff), diff > 0);
                        }
                    }
                    return JsonUtil.messageJson("Orden actualizada correctamente");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating order", e);
            return JsonUtil.errorJson("Error DB: " + e.getMessage());
        }
        return JsonUtil.errorJson("No se pudo actualizar la orden");
    }

    private boolean hasEnoughBudget(int budgetId, double amount) {
        String sql = "SELECT cantidad, gasto FROM presupuesto WHERE idPresupuesto = ?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, budgetId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble("cantidad");
                    double spent = rs.getDouble("gasto");
                    return (total - spent) >= amount;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking budget", e);
        }
        return false;
    }

    public String updateOrderDescription(int orderId, String desc) {
        String sql = "UPDATE ordencompra SET descripcion = ? WHERE idOrden = ?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, desc);
            stmt.setInt(2, orderId);
            int rows = stmt.executeUpdate();
            return rows > 0 ? JsonUtil.messageJson("Descripción actualizada") : JsonUtil.errorJson("No se encontró la orden");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating description", e);
            return JsonUtil.errorJson("Error DB: " + e.getMessage());
        }
    }

    public String updateOrderStatus(int orderId, String newStatus) {
        String sql = "UPDATE ordencompra SET Estado = ? WHERE idOrden = ?";
        try (Connection conn = DatabaseManager.getConnection("webapp")) {
            
            // Get previous status and amount to check if we need to update budget
            String sqlCheck = "SELECT Estado, Cantidad, idPresupuesto FROM ordencompra WHERE idOrden = ?";
            String oldStatus = "";
            double amount = 0;
            int budgetId = 0;

            try (PreparedStatement stmt = conn.prepareStatement(sqlCheck)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        oldStatus = rs.getString("Estado");
                        amount = rs.getDouble("Cantidad");
                        budgetId = rs.getInt("idPresupuesto");
                    } else {
                        return JsonUtil.errorJson("Orden no encontrada");
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, orderId);
                int rows = stmt.executeUpdate();
                
                if (rows > 0) {
                    // If transitioning to "Aprobada", add to budget's 'gasto'
                    if ("Aprobada".equalsIgnoreCase(newStatus) && !"Aprobada".equalsIgnoreCase(oldStatus)) {
                        updateBudgetGasto(budgetId, amount, true);
                    } 
                    // If transitioning FROM "Aprobada" to something else, subtract from budget's 'gasto'
                    else if (!"Aprobada".equalsIgnoreCase(newStatus) && "Aprobada".equalsIgnoreCase(oldStatus)) {
                        updateBudgetGasto(budgetId, amount, false);
                    }

                    // Notify status change
                    notifyStatusChange(orderId, oldStatus, newStatus);
                    
                    return JsonUtil.messageJson("Estado actualizado correctamente");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating status", e);
        }
        return JsonUtil.errorJson("No se pudo actualizar el estado");
    }

    private void notifyStatusChange(int orderId, String oldStatus, String newStatus) {
        String msg = "La orden " + orderId + " ha cambiado de estado: " + oldStatus + " -> " + newStatus;
        // Notify department head
        String sqlHead = "SELECT u.IdUsuario FROM usuario u " +
                         "JOIN departamento d ON u.idDepartamento = d.idDepartamento " +
                         "JOIN presupuesto p ON d.idDepartamento = p.idDepartamento " +
                         "JOIN ordencompra oc ON p.idPresupuesto = oc.idPresupuesto " +
                         "WHERE oc.idOrden = ? AND u.idRol = 3";
        
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sqlHead)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    createNotification(rs.getInt("IdUsuario"), msg, orderId);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error notifying status change", e);
        }
    }

    private void updateBudgetGasto(int budgetId, double amount, boolean add) {
        String sql = add ? 
            "UPDATE presupuesto SET gasto = gasto + ? WHERE idPresupuesto = ?" :
            "UPDATE presupuesto SET gasto = gasto - ? WHERE idPresupuesto = ?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, amount);
            stmt.setInt(2, budgetId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating budget gasto", e);
        }
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
        if (id == -2) return JsonUtil.errorJson("Presupuesto insuficiente para esta operación");
        return id > 0 ? JsonUtil.messageJson("Orden creada correctamente") : JsonUtil.errorJson("No se pudo crear la orden");
    }

    public List<Map<String, String>> getOrderComments(int orderId) {
        String sql = "SELECT c.*, u.Nombre as usuario_nombre FROM comentarios_orden c " +
                     "JOIN usuario u ON c.idUsuario = u.IdUsuario " +
                     "WHERE c.idOrden = ? ORDER BY c.fecha ASC";
        List<Map<String, String>> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> m = new HashMap<>();
                    m.put("idComentario", String.valueOf(rs.getInt("idComentario")));
                    m.put("usuario", rs.getString("usuario_nombre"));
                    m.put("comentario", rs.getString("comentario"));
                    m.put("fecha", rs.getString("fecha"));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting order comments", e);
        }
        return list;
    }

    public boolean addOrderComment(int orderId, int userId, String comment) {
        String sql = "INSERT INTO comentarios_orden (idOrden, idUsuario, comentario) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, userId);
            stmt.setString(3, comment);
            int rows = stmt.executeUpdate();
            
            if (rows > 0) {
                // Auto-notify department head
                notifyDeptHead(orderId, userId, comment);
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding comment", e);
        }
        return false;
    }

    private void notifyDeptHead(int orderId, int senderId, String comment) {
        // Find dept head for this order
        String sqlHead = "SELECT u.IdUsuario FROM usuario u " +
                         "JOIN departamento d ON u.idDepartamento = d.idDepartamento " +
                         "JOIN presupuesto p ON d.idDepartamento = p.idDepartamento " +
                         "JOIN ordencompra oc ON p.idPresupuesto = oc.idPresupuesto " +
                         "WHERE oc.idOrden = ? AND u.idRol = 3"; // Rol 3 is Jefe
        
        String senderSql = "SELECT Nombre FROM usuario WHERE IdUsuario = ?";
        String senderName = "Alguien";
        
        try (Connection conn = DatabaseManager.getConnection("webapp")) {
            try (PreparedStatement sStmt = conn.prepareStatement(senderSql)) {
                sStmt.setInt(1, senderId);
                try (ResultSet sRs = sStmt.executeQuery()) {
                    if (sRs.next()) senderName = sRs.getString("Nombre");
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlHead)) {
                stmt.setInt(1, orderId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int headId = rs.getInt("IdUsuario");
                        if (headId != senderId) { // Don't notify self
                            createNotification(headId, senderName + " añadió un comentario a la orden " + orderId, orderId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error notifying dept head", e);
        }
    }

    public void createNotification(int userIdDest, String message, Integer orderId) {
        String sql = "INSERT INTO notificaciones (idUsuarioDestino, mensaje, idOrden) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userIdDest);
            stmt.setString(2, message);
            if (orderId != null) stmt.setInt(3, orderId); else stmt.setNull(3, java.sql.Types.INTEGER);
            stmt.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating notification", e);
        }
    }

    public List<Map<String, String>> getUserNotifications(int userId) {
        String sql = "SELECT * FROM notificaciones WHERE idUsuarioDestino = ? ORDER BY fecha DESC LIMIT 20";
        List<Map<String, String>> list = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> m = new HashMap<>();
                    m.put("idNotificacion", String.valueOf(rs.getInt("idNotificacion")));
                    m.put("mensaje", rs.getString("mensaje"));
                    m.put("leida", String.valueOf(rs.getBoolean("leida")));
                    m.put("fecha", rs.getString("fecha"));
                    m.put("idOrden", String.valueOf(rs.getInt("idOrden")));
                    list.add(m);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting notifications", e);
        }
        return list;
    }

    public boolean markNotificationAsRead(int notifId) {
        String sql = "UPDATE notificaciones SET leida = 1 WHERE idNotificacion = ?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, notifId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking notification as read", e);
        }
        return false;
    }
}
