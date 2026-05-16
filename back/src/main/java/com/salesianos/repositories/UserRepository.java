package com.salesianos.repositories;

import com.salesianos.utils.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserRepository {
    private static final Logger LOGGER = Logger.getLogger(UserRepository.class.getName());

    public List<Integer> findHeadsByOrder(long orderId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT u.IdUsuario FROM usuario u " +
                     "JOIN departamento d ON u.idDepartamento = d.idDepartamento " +
                     "JOIN presupuesto p ON d.idDepartamento = p.idDepartamento " +
                     "JOIN ordencompra oc ON p.idPresupuesto = oc.idPresupuesto " +
                     "WHERE oc.idOrden = ? AND u.idRol = (SELECT idRol FROM roles WHERE Nombre = 'Jefe de Equipo' LIMIT 1)";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("IdUsuario"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding heads by order", e);
        }
        return ids;
    }

    public User authenticate(String correo, String password) {
        String sql = "SELECT u.IdUsuario, u.Nombre as usuario_nombre, u.correo, u.Contrasena, r.Nombre as rol_nombre, d.Nombre as dep_nombre, d.Codigo as dep_codigo " +
                     "FROM usuario u " +
                     "LEFT JOIN roles r ON u.idRol = r.idRol " +
                     "LEFT JOIN departamento d ON u.idDepartamento = d.idDepartamento " +
                     "WHERE u.Correo = ?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    if (com.salesianos.utils.PasswordUtil.checkPassword(password, rs.getString("Contrasena"))) {
                        User u = new User();
                        u.setIdUsuario(rs.getLong("IdUsuario"));
                        u.setNombre(rs.getString("usuario_nombre"));
                        u.setCorreo(rs.getString("correo"));
                        u.setRol(rs.getString("rol_nombre"));
                        u.setNombreDepartamento(rs.getString("dep_nombre"));
                        u.setCodigoDepartamento(rs.getString("dep_codigo"));
                        return u;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Auth error", e);
        }
        return null;
    }

    public String register(String nombre, String apellidos, String email, String password, String telefono) {
        try (Connection conn = DatabaseManager.getConnection("webapp");
             CallableStatement stmt = conn.prepareCall("{CALL insertar_usuario(?, ?, ?, ?, ?)}")) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellidos);
            stmt.setString(3, email);
            stmt.setString(4, com.salesianos.utils.PasswordUtil.hashPassword(password));
            stmt.setString(5, telefono);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("mensaje");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Register error", e);
        }
        return "Error en el registro";
    }
}
