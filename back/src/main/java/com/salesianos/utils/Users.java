package com.salesianos.utils;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilidad para la gestión de usuarios (Registro, etc.)
 */
public class Users {

    private static final Logger LOGGER = Logger.getLogger(Users.class.getName());

    /**
     * Registra un nuevo usuario llamando al procedimiento almacenado 'insertar_usuario'
     */
    public String register(String nombre, String apellidos, String email, String password, String telefono) {
        String res = JsonUtil.errorJson("Error desconocido en el registro");
        try (Connection conn = DatabaseManager.getConnection("webapp");
             CallableStatement stmt = conn.prepareCall("{CALL insertar_usuario(?, ?, ?, ?, ?)}")) {

            stmt.setString(1, nombre);
            stmt.setString(2, apellidos);
            stmt.setString(3, email);
            stmt.setString(4, password);
            stmt.setString(5, telefono);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    String mensaje = rs.getString("mensaje");
                    if ("success".equals(status)) {
                        res = JsonUtil.messageJson(mensaje);
                    } else {
                        res = JsonUtil.errorJson(mensaje);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error registering user: " + email, e);
            res = JsonUtil.errorJson("Error de base de datos: " + e.getMessage());
        }
        return res;
    }
}
