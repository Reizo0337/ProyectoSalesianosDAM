package com.salesianos.repositories;

import com.salesianos.models.User;
import com.salesianos.utils.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserRepository {
    private static final Logger LOGGER = Logger.getLogger(UserRepository.class.getName());

    // Devuelve los Jefes de Equipo del departamento al que pertenece la orden
    public List<Integer> findHeadsByOrder(long orderId) {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT DISTINCT u.IdUsuario FROM usuario u " +
                     "JOIN departamento d ON u.idDepartamento = d.idDepartamento " +
                     "JOIN presupuesto p ON d.idDepartamento = p.idDepartamento " +
                     "JOIN ordencompra oc ON p.idPresupuesto = oc.idPresupuesto " +
                     "JOIN roles r ON u.idRol = r.idRol " +
                     "WHERE oc.idOrden = ? AND r.Nombre = 'Jefe de Equipo'";
        try (Connection conn = DatabaseManager.getConnection();
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

    // Lista todos los nombres de usuario para autocompletado de @menciones
    public List<java.util.Map<String, String>> findAllUserNames() {
        List<java.util.Map<String, String>> users = new ArrayList<>();
        String sql = "SELECT u.IdUsuario, u.Nombre, r.Nombre as rol FROM usuario u " +
                     "LEFT JOIN roles r ON u.idRol = r.idRol ORDER BY u.Nombre";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("id", String.valueOf(rs.getInt("IdUsuario")));
                m.put("nombre", rs.getString("Nombre"));
                m.put("rol", rs.getString("rol") != null ? rs.getString("rol") : "");
                users.add(m);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching user names", e);
        }
        return users;
    }

    // Busca el ID de un usuario por su nombre exacto (para resolver @menciones)
    public Long findUserIdByName(String name) {
        String sql = "SELECT IdUsuario FROM usuario WHERE Nombre = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getLong("IdUsuario");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user by name: " + name, e);
        }
        return null;
    }

    // Devuelve solo los usuarios que tienen acceso a ver esta orden específica
    public List<java.util.Map<String, String>> findRelevantUsersForOrder(long orderId) {
        List<java.util.Map<String, String>> users = new ArrayList<>();
        String sql = "SELECT DISTINCT u.IdUsuario, u.Nombre, r.Nombre as rol FROM usuario u " +
                     "JOIN roles r ON u.idRol = r.idRol " +
                     "LEFT JOIN departamento d ON u.idDepartamento = d.idDepartamento " +
                     "WHERE r.Nombre IN ('Administrador', 'Contable') " +
                     "OR (r.Nombre = 'Jefe de Equipo' AND d.idDepartamento = (" +
                     "  SELECT p.idDepartamento FROM ordencompra oc " +
                     "  JOIN presupuesto p ON oc.idPresupuesto = p.idPresupuesto " +
                     "  WHERE oc.idOrden = ?" +
                     ")) ORDER BY u.Nombre";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    java.util.Map<String, String> m = new java.util.HashMap<>();
                    m.put("id", String.valueOf(rs.getInt("IdUsuario")));
                    m.put("nombre", rs.getString("Nombre"));
                    m.put("rol", rs.getString("rol") != null ? rs.getString("rol") : "");
                    users.add(m);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching relevant users for order " + orderId, e);
        }
        return users;
    }

    public String findNameById(long id) {
        String sql = "SELECT Nombre FROM usuario WHERE IdUsuario = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("Nombre");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding user name " + id, e);
        }
        return "Usuario Desconocido";
    }

    public User authenticate(String correo, String password) {
        String sql = "SELECT u.IdUsuario, u.Nombre as usuario_nombre, u.correo, u.Contrasena, u.isVerified, " +
                     "r.Nombre as rol_nombre, d.Nombre as dep_nombre, d.Codigo as dep_codigo, u.idDepartamento " +
                     "FROM usuario u " +
                     "LEFT JOIN roles r ON u.idRol = r.idRol " +
                     "LEFT JOIN departamento d ON u.idDepartamento = d.idDepartamento " +
                     "WHERE u.Correo = ?";
        try (Connection conn = DatabaseManager.getConnection();
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
                        u.setVerified(rs.getBoolean("isVerified"));
                        u.setIdDepartamento(rs.getLong("idDepartamento"));
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
        try (Connection conn = DatabaseManager.getConnection();
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

    // ── Métodos de Administración ──

    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.IdUsuario, u.Nombre, u.Apellidos, u.Correo, u.isVerified, " +
                     "r.Nombre as rol_nombre, d.Nombre as dep_nombre, u.idDepartamento " +
                     "FROM usuario u " +
                     "LEFT JOIN roles r ON u.idRol = r.idRol " +
                     "LEFT JOIN departamento d ON u.idDepartamento = d.idDepartamento " +
                     "ORDER BY u.isVerified ASC, u.Nombre ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setIdUsuario(rs.getLong("IdUsuario"));
                u.setNombre(rs.getString("Nombre"));
                u.setApellidos(rs.getString("Apellidos"));
                u.setCorreo(rs.getString("Correo"));
                u.setRol(rs.getString("rol_nombre"));
                u.setNombreDepartamento(rs.getString("dep_nombre"));
                u.setVerified(rs.getBoolean("isVerified"));
                u.setIdDepartamento(rs.getLong("idDepartamento"));
                users.add(u);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all users", e);
        }
        return users;
    }

    public boolean deleteUser(long id) {
        String sql = "DELETE FROM usuario WHERE IdUsuario = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting user", e);
            return false;
        }
    }

    public boolean verifyUser(long id, long idRol, Long idDepartamento) {
        String sql = "UPDATE usuario SET isVerified = 1, idRol = ?, idDepartamento = ? WHERE idusuario = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, idRol);
            if (idDepartamento != null) {
                stmt.setLong(2, idDepartamento);
            } else {
                stmt.setNull(2, java.sql.Types.BIGINT);
            }
            stmt.setLong(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error verifying user", e);
            return false;
        }
    }

    public boolean updateUser(long id, String nombre, String apellidos, String correo, Long idRol, Long idDepartamento) {
        String sql = "UPDATE usuario SET Nombre = ?, Apellidos = ?, Correo = ?, idRol = ?, idDepartamento = ? WHERE IdUsuario = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            stmt.setString(2, apellidos);
            stmt.setString(3, correo);
            if (idRol != null) stmt.setLong(4, idRol); else stmt.setNull(4, Types.BIGINT);
            if (idDepartamento != null) stmt.setLong(5, idDepartamento); else stmt.setNull(5, Types.BIGINT);
            stmt.setLong(6, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating user", e);
            return false;
        }
    }

    public boolean updatePassword(long id, String newPassword) {
        String sql = "UPDATE usuario SET Contrasena = ? WHERE IdUsuario = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, com.salesianos.utils.PasswordUtil.hashPassword(newPassword));
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating password", e);
            return false;
        }
    }

    // Para obtener listas de roles y departamentos para los selectores del admin
    public List<java.util.Map<String, String>> findAllRoles() {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT idRol, Nombre FROM roles";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("id", String.valueOf(rs.getLong("idRol")));
                m.put("nombre", rs.getString("Nombre"));
                list.add(m);
            }
        } catch (SQLException e) { }
        return list;
    }

    public List<java.util.Map<String, String>> findAllDepartments() {
        List<java.util.Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT idDepartamento, Nombre FROM departamento";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                java.util.Map<String, String> m = new java.util.HashMap<>();
                m.put("id", String.valueOf(rs.getLong("idDepartamento")));
                m.put("nombre", rs.getString("Nombre"));
                list.add(m);
            }
        } catch (SQLException e) { }
        return list;
    }
}
