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

        // Nombre, correo, rol, idDepartamento
        String sql = "SELECT u.IdUsuario, u.Nombre as usuario_nombre, u.correo, r.Nombre as rol_nombre, d.Nombre as dep_nombre, d.Codigo as dep_codigo " +
                     "FROM usuario u " +
                     "LEFT JOIN roles r ON u.idRol = r.idRol " +
                     "LEFT JOIN departamento d ON u.idDepartamento = d.idDepartamento " +
                     "WHERE u.Correo = ? AND u.Contrasena = ?";

        System.out.println("Login attempt: " + correo + " / " + password);
        try (Connection conn = DatabaseManager.getConnection("webapp");
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, correo);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, String> user = new HashMap<>();
                    user.put("idUsuario", String.valueOf(rs.getInt("idUsuario")));
                    user.put("nombre", rs.getString("usuario_nombre"));
                    user.put("correo", rs.getString("correo"));
                    user.put("rol", rs.getString("rol_nombre"));
                    user.put("idDepartamento", rs.getString("dep_nombre"));
                    user.put("codigoDepartamento", rs.getString("dep_codigo"));
                    return user;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error authenticating user: " + correo, e);
        }
        return null;
    }
}
