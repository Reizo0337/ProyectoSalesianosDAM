package com.salesianos.repositories;

import com.salesianos.models.Order;
import com.salesianos.utils.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OrderRepository {
    private static final Logger LOGGER = Logger.getLogger(OrderRepository.class.getName());

    public List<Order> findAllByYear(int year) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT oc.*, d.Nombre as nombredepartamento, " +
                     "(SELECT COUNT(*) FROM facturas WHERE idOrdenCompra = oc.idOrden) as numFacturas, " +
                     "(SELECT COUNT(*) FROM comentarios_orden WHERE idOrden = oc.idOrden) as numComentarios " +
                     "FROM ordencompra oc " +
                     "LEFT JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto " +
                     "LEFT JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                     "WHERE YEAR(oc.fechaCreacion) = ? " +
                     "ORDER BY oc.fechaCreacion DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all orders by year", e);
        }
        return orders;
    }

    public List<Integer> findYears() {
        List<Integer> years = new ArrayList<>();
        String sql = "SELECT DISTINCT YEAR(fechaCreacion) as year FROM ordencompra ORDER BY year DESC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                years.add(rs.getInt("year"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching order years", e);
        }
        return years;
    }

    public Map<String, Object> findByIdDetailed(long id, String deptName, String userRole) {
        Map<String, Object> result = new HashMap<>();
        String sqlOrder = "SELECT oc.*, p.Codigo as presupuesto_codigo, p.Nombre as presupuesto_nombre, p.type as presupuesto_tipo, " +
                          "d.Nombre as dep_nombre, d.Codigo as dep_codigo " +
                          "FROM ordencompra oc " +
                          "JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto " +
                          "JOIN departamento d ON p.idDepartamento = d.idDepartamento " +
                          "WHERE oc.idOrden = ? AND (d.Nombre = ? OR ? IN ('Admin', 'Administrador', 'Contable'))";

        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlOrder)) {
                stmt.setLong(1, id);
                stmt.setString(2, deptName);
                stmt.setString(3, userRole);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        result.put("order", mapResultSetToOrderDetailed(rs));
                    } else return null;
                }
            }

            String sqlProducts = "SELECT ocp.PrecioUnitario, pr.*, prov.Nombre as proveedor_nombre " +
                                 "FROM ordencompraproductos ocp " +
                                 "JOIN productos pr ON ocp.idProducto = pr.idProducto " +
                                 "LEFT JOIN productosproveedores pp ON pr.idProducto = pp.idProducto " +
                                 "LEFT JOIN proveedores prov ON pp.idProveedor = prov.idProveedor " +
                                 "WHERE ocp.idOrdenCompra = ?";
            List<Map<String, String>> products = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlProducts)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, String> p = new HashMap<>();
                        p.put("idproducto", String.valueOf(rs.getInt("idProducto")));
                        p.put("nombre", rs.getString("Nombre"));
                        p.put("descripcion", rs.getString("Descripcion"));
                        p.put("preciounitario", String.valueOf(rs.getBigDecimal("PrecioUnitario")));
                        p.put("proveedor", rs.getString("proveedor_nombre"));
                        products.add(p);
                    }
                }
            }
            result.put("productos", products);

            String sqlFacturas = "SELECT idFactura, fechaCreacion FROM facturas WHERE idOrdenCompra = ?";
            List<Map<String, String>> invoices = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(sqlFacturas)) {
                stmt.setLong(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, String> f = new HashMap<>();
                        f.put("idfactura", String.valueOf(rs.getInt("idFactura")));
                        f.put("fechacreacion", rs.getString("fechaCreacion"));
                        invoices.add(f);
                    }
                }
            }
            result.put("facturas", invoices);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching detailed order " + id, e);
        }
        return result;
    }

    public long save(Order o) {
        String sql = "INSERT INTO ordencompra (idPresupuesto, numero_orden, numero_plan, Cantidad, Inversion, Tipo, descripcion, Estado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, o.getIdPresupuesto());
            
            if (o.getNumeroOrden() == null || o.getNumeroOrden().trim().isEmpty()) {
                stmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(2, o.getNumeroOrden());
            }

            if (o.getNumeroPlan() == null || o.getNumeroPlan().trim().isEmpty()) {
                stmt.setNull(3, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(3, o.getNumeroPlan());
            }
            
            stmt.setDouble(4, o.getCantidad());
            stmt.setBoolean(5, o.isInversion());
            stmt.setString(6, o.getTipo());
            stmt.setString(7, o.getDescripcion());
            stmt.setString(8, o.getEstado() != null ? o.getEstado() : "Pendiente");

            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving order", e);
        }
        return -1;
    }

    public boolean update(Order o) {
        String sql = "UPDATE ordencompra SET Cantidad=?, numero_plan=?, Tipo=?, Inversion=?, descripcion=?, idPresupuesto=? WHERE idOrden=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, o.getCantidad());
            
            if (o.getNumeroPlan() == null || o.getNumeroPlan().trim().isEmpty()) {
                stmt.setNull(2, java.sql.Types.VARCHAR);
            } else {
                stmt.setString(2, o.getNumeroPlan());
            }
            
            stmt.setString(3, o.getTipo());
            stmt.setBoolean(4, o.isInversion());
            stmt.setString(5, o.getDescripcion());
            stmt.setLong(6, o.getIdPresupuesto());
            stmt.setLong(7, o.getIdOrden());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating order", e);
            return false;
        }
    }

    public boolean updateStatus(long id, String status) {
        String sql = "UPDATE ordencompra SET Estado = ? WHERE idOrden = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating status for order " + id, e);
            return false;
        }
    }

    public Order findById(long id) {
        String sql = "SELECT * FROM ordencompra WHERE idOrden = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapResultSetToOrderBasic(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding order " + id, e);
        }
        return null;
    }

    public boolean delete(long id) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM facturas WHERE idOrdenCompra = ?")) {
                    stmt.setLong(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM ordencompraproductos WHERE idOrdenCompra = ?")) {
                    stmt.setLong(1, id);
                    stmt.executeUpdate();
                }
                try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM ordencompra WHERE idOrden = ?")) {
                    stmt.setLong(1, id);
                    stmt.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting order " + id, e);
            return false;
        }
    }

    public String getNextSequence(String dept, String year) {
        String yearShort = year.length() > 2 ? year.substring(year.length() - 2) : year;
        String pattern = dept + "/%/" + yearShort + "/%";
        String sql = "SELECT numero_orden FROM ordencompra WHERE numero_orden LIKE ? ORDER BY idOrden DESC LIMIT 1";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String last = rs.getString("numero_orden");
                    String[] parts = last.split("/");
                    if (parts.length >= 2) {
                        try {
                            return String.format("%03d", Integer.parseInt(parts[1]) + 1);
                        } catch (Exception ignored) {}
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating sequence", e);
        }
        return "001";
    }

    public boolean addProduct(long orderId, long productId, double price) {
        String sql = "INSERT INTO ordencompraproductos (idOrdenCompra, idProducto, PrecioUnitario) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setLong(2, productId);
            stmt.setDouble(3, price);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding product to order", e);
            return false;
        }
    }

    public boolean addInvoice(long orderId, byte[] blob) {
        String sql = "INSERT INTO facturas (idOrdenCompra, blobFactura) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.setBytes(2, blob);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding invoice", e);
            return false;
        }
    }

    public byte[] getInvoiceBlob(long idFactura) {
        String sql = "SELECT blobFactura FROM facturas WHERE idFactura = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idFactura);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("blobFactura");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching invoice blob for ID " + idFactura, e);
        }
        return null;
    }

    // Mapper completo: para queries con JOIN a departamento y subconsultas
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order o = mapResultSetToOrderBasic(rs);
        o.setNombreDepartamento(rs.getString("nombredepartamento"));
        o.setNumFacturas(rs.getInt("numFacturas"));
        o.setNumComentarios(rs.getInt("numComentarios"));
        return o;
    }

    // Mapper básico: para queries sin JOIN (SELECT * FROM ordencompra)
    private Order mapResultSetToOrderBasic(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setIdOrden(rs.getLong("idOrden"));
        o.setIdPresupuesto(rs.getLong("idPresupuesto"));
        o.setNumeroOrden(rs.getString("numero_orden"));
        o.setNumeroPlan(rs.getString("numero_plan"));
        o.setCantidad(rs.getDouble("Cantidad"));
        o.setInversion(rs.getBoolean("Inversion"));
        o.setTipo(rs.getString("Tipo"));
        o.setDescripcion(rs.getString("descripcion"));
        o.setEstado(rs.getString("Estado"));
        o.setFechaCreacion(rs.getTimestamp("fechaCreacion"));
        return o;
    }

    private Map<String, String> mapResultSetToOrderDetailed(ResultSet rs) throws SQLException {
        Map<String, String> m = new HashMap<>();
        m.put("idorden", String.valueOf(rs.getInt("idOrden")));
        m.put("numero_orden", rs.getString("numero_orden"));
        m.put("numero_plan", rs.getString("numero_plan"));
        m.put("cantidad", String.valueOf(rs.getBigDecimal("Cantidad")));
        m.put("inversion", String.valueOf(rs.getBoolean("Inversion")));
        m.put("tipo", rs.getString("Tipo"));
        m.put("descripcion", rs.getString("descripcion"));
        m.put("estado", rs.getString("Estado"));
        m.put("fechacreacion", rs.getString("fechaCreacion"));
        m.put("idpresupuesto", String.valueOf(rs.getInt("idPresupuesto")));
        m.put("presupuesto_codigo", rs.getString("presupuesto_codigo"));
        m.put("presupuesto_nombre", rs.getString("presupuesto_nombre"));
        m.put("presupuesto_tipo", rs.getString("presupuesto_tipo"));
        m.put("dep_nombre", rs.getString("dep_nombre"));
        m.put("dep_codigo", rs.getString("dep_codigo"));
        return m;
    }
}
