package com.salesianos.repositories;

import com.salesianos.models.Notification;
import com.salesianos.utils.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationRepository {
    private static final Logger LOGGER = Logger.getLogger(NotificationRepository.class.getName());

    public List<Notification> findAllByUser(long userId) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM notificaciones WHERE idUsuarioDestino = ? ORDER BY fecha DESC LIMIT 20";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notification n = new Notification();
                    n.setIdNotificacion(rs.getLong("idNotificacion"));
                    n.setIdUsuarioDestino(rs.getLong("idUsuarioDestino"));
                    n.setMensaje(rs.getString("mensaje"));
                    n.setLeida(rs.getBoolean("leida"));
                    n.setFecha(rs.getString("fecha"));
                    long orderId = rs.getLong("idOrden");
                    if (!rs.wasNull()) n.setIdOrden(orderId);
                    list.add(n);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching notifications", e);
        }
        return list;
    }

    public boolean create(long userId, String msg, Long orderId) {
        String sql = "INSERT INTO notificaciones (idUsuarioDestino, mensaje, idOrden) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, msg);
            if (orderId != null) stmt.setLong(3, orderId); else stmt.setNull(3, Types.INTEGER);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating notification", e);
            return false;
        }
    }

    public boolean markAsRead(long notifId) {
        String sql = "UPDATE notificaciones SET leida = 1 WHERE idNotificacion = ?";
        try (Connection conn = DatabaseManager.getConnection("webapp");
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, notifId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking notification as read", e);
            return false;
        }
    }

    public void deleteByOrder(long orderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM notificaciones WHERE idOrden = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.executeUpdate();
        }
    }
}
