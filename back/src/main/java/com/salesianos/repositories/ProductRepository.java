package com.salesianos.repositories;

import com.salesianos.models.Product;
import com.salesianos.utils.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProductRepository {
    private static final Logger LOGGER = Logger.getLogger(ProductRepository.class.getName());

    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.*, " +
                     "(SELECT prov.Nombre FROM proveedores prov JOIN productosproveedores pp ON prov.idProveedor = pp.idProveedor WHERE pp.idProducto = p.idProducto LIMIT 1) as proveedorNombre, " +
                     "(SELECT AVG(PrecioUnitario) FROM ordencompraproductos WHERE idProducto = p.idProducto) as precioMedioCalc " +
                     "FROM productos p ORDER BY p.Nombre ASC";

        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all products", e);
        }
        return products;
    }

    public long save(Product p) {
        String sqlProd = "INSERT INTO productos (Nombre, Descripcion) VALUES (?, ?)";
        String sqlAssoc = "INSERT INTO productosproveedores (idProducto, idProveedor) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection("webapp")) {
            long productId = -1;
            try (PreparedStatement stmt = conn.prepareStatement(sqlProd, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, p.getNombre());
                stmt.setString(2, p.getDescripcion() != null ? p.getDescripcion() : "");
                if (stmt.executeUpdate() > 0) {
                    try (ResultSet gk = stmt.getGeneratedKeys()) {
                        if (gk.next()) productId = gk.getLong(1);
                    }
                }
            }
            if (productId > 0 && p.getIdProveedor() > 0) {
                try (PreparedStatement stmt = conn.prepareStatement(sqlAssoc)) {
                    stmt.setLong(1, productId);
                    stmt.setLong(2, p.getIdProveedor());
                    stmt.executeUpdate();
                }
            }
            return productId;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving product", e);
            return -1;
        }
    }

    public boolean update(Product p) {
        String sql = "UPDATE productos SET Nombre=?, Descripcion=? WHERE idProducto=?";
        try (Connection conn = DatabaseManager.getConnection("webapp")) {
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, p.getNombre());
                stmt.setString(2, p.getDescripcion());
                stmt.setLong(3, p.getIdProducto());
                stmt.executeUpdate();
            }
            if (p.getIdProveedor() > 0) {
                // Update association
                String delSql = "DELETE FROM productosproveedores WHERE idProducto=?";
                try (PreparedStatement delStmt = conn.prepareStatement(delSql)) {
                    delStmt.setLong(1, p.getIdProducto());
                    delStmt.executeUpdate();
                }
                String insSql = "INSERT INTO productosproveedores (idProducto, idProveedor) VALUES (?, ?)";
                try (PreparedStatement insStmt = conn.prepareStatement(insSql)) {
                    insStmt.setLong(1, p.getIdProducto());
                    insStmt.setLong(2, p.getIdProveedor());
                    insStmt.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating product", e);
            return false;
        }
    }

    public boolean delete(long id) {
        try (Connection conn = DatabaseManager.getConnection("webapp")) {
            // Delete associations first
            String delSql = "DELETE FROM productosproveedores WHERE idProducto=?";
            try (PreparedStatement delStmt = conn.prepareStatement(delSql)) {
                delStmt.setLong(1, id);
                delStmt.executeUpdate();
            }
            String sql = "DELETE FROM productos WHERE idProducto=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting product", e);
            return false;
        }
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setIdProducto(rs.getLong("idProducto"));
        p.setNombre(rs.getString("Nombre"));
        p.setDescripcion(rs.getString("Descripcion"));
        p.setPrecioMedio(rs.getDouble("precioMedioCalc"));
        p.setNombreProveedor(rs.getString("proveedorNombre"));
        return p;
    }
}
