package com.salesianos.repositories;

import com.salesianos.models.Supplier;
import com.salesianos.utils.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SupplierRepository {
    private static final Logger LOGGER = Logger.getLogger(SupplierRepository.class.getName());

    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM proveedores ORDER BY Nombre ASC";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all suppliers", e);
        }
        return suppliers;
    }

    public long save(Supplier s) {
        String sql = "INSERT INTO proveedores (Nombre, Telefono, Direccion) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, s.getNombre());
            stmt.setString(2, s.getTelefono());
            stmt.setString(3, s.getDireccion());
            if (stmt.executeUpdate() > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving supplier", e);
        }
        return -1;
    }

    public boolean update(Supplier s) {
        String sql = "UPDATE proveedores SET Nombre=?, Telefono=?, Direccion=? WHERE idProveedor=?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, s.getNombre());
            stmt.setString(2, s.getTelefono());
            stmt.setString(3, s.getDireccion());
            stmt.setLong(4, s.getIdProveedor());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating supplier", e);
            return false;
        }
    }

    public boolean delete(long id) {
        try (Connection conn = DatabaseManager.getConnection("webapp")) {
            // Delete associations first
            String sqlAssoc = "DELETE FROM productosproveedores WHERE idProveedor=?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlAssoc)) {
                stmt.setLong(1, id);
                stmt.executeUpdate();
            }
            String sql = "DELETE FROM proveedores WHERE idProveedor=?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting supplier", e);
            return false;
        }
    }

    public List<Product> getSupplierProducts(long idProveedor) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT p.* FROM productos p JOIN productosproveedores pp ON p.idProducto = pp.idProducto WHERE pp.idProveedor = ?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProveedor);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setIdProducto(rs.getLong("idProducto"));
                    p.setNombre(rs.getString("Nombre"));
                    p.setDescripcion(rs.getString("Descripcion"));
                    products.add(p);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching supplier products", e);
        }
        return products;
    }

    public boolean assignProduct(long idProveedor, long idProducto) {
        String sql = "INSERT IGNORE INTO productosproveedores (idProveedor, idProducto) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProveedor);
            stmt.setLong(2, idProducto);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error assigning product to supplier", e);
            return false;
        }
    }

    public boolean removeProduct(long idProveedor, long idProducto) {
        String sql = "DELETE FROM productosproveedores WHERE idProveedor=? AND idProducto=?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idProveedor);
            stmt.setLong(2, idProducto);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing product from supplier", e);
            return false;
        }
    }

    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setIdProveedor(rs.getLong("idProveedor"));
        s.setNombre(rs.getString("Nombre"));
        s.setTelefono(rs.getString("Telefono"));
        s.setDireccion(rs.getString("Direccion"));
        return s;
    }
}
