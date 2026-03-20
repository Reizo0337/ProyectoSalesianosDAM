package com.salesianos.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Login {

    private static final Logger LOGGER = Logger.getLogger(Login.class.getName());

    public Map<String, String> authenticate(String correo, String password) {
        if (correo == null || password == null) {
            throw new IllegalArgumentException("Email and password cannot be null");
        }

        // Updated query based on user requirement: Nombre, correo, rol, idDepartamento
        // Added WHERE clause and proper aliases to facilitate mapping
        String sql = "SELECT u.Nombre as usuario_nombre, u.correo, r.nombre as rol_nombre, d.Nombre as dep_nombre " +
                     "FROM usuario u " +
                     "JOIN roles r ON u.idrol = r.nombre " +
                     "JOIN departamento d ON u.iddepartamento = d.Nombre " +
                     "WHERE u.correo = ? AND u.contrasena = ?;";

        try (Connection conn = DatabaseManager.getConnection("webapp");
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, correo);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> user = new HashMap<>();
                    user.put("nombre", rs.getString("usuario_nombre"));
                    user.put("correo", rs.getString("correo"));
                    user.put("rol", rs.getString("rol_nombre"));
                    user.put("idDepartamento", rs.getString("dep_nombre"));
                    return user;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error authenticating user: " + correo, e);
        }
        return null;
    }
}
