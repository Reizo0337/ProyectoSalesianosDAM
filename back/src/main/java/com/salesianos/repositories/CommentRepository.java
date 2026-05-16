package com.salesianos.repositories;

import com.salesianos.models.Comment;
import com.salesianos.utils.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CommentRepository {
    private static final Logger LOGGER = Logger.getLogger(CommentRepository.class.getName());

    public List<Comment> findAllByOrder(long orderId) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.*, u.Nombre as usuario_nombre FROM comentarios_orden c " +
                     "JOIN usuario u ON c.idUsuario = u.IdUsuario " +
                     "WHERE c.idOrden = ? ORDER BY c.fecha ASC";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Comment c = new Comment();
                    c.setIdComentario(rs.getLong("idComentario"));
                    c.setIdOrden(rs.getLong("idOrden"));
                    c.setIdUsuario(rs.getLong("idUsuario"));
                    c.setUsuarioNombre(rs.getString("usuario_nombre"));
                    c.setComentario(rs.getString("comentario"));
                    c.setFecha(rs.getString("fecha"));
                    comments.add(c);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching comments for order " + orderId, e);
        }
        return comments;
    }

    public boolean save(Comment c) {
        String sql = "INSERT INTO comentarios_orden (idOrden, idUsuario, comentario) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, c.getIdOrden());
            stmt.setLong(2, c.getIdUsuario());
            stmt.setString(3, c.getComentario());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving comment", e);
            return false;
        }
    }

    public void deleteByOrder(long orderId, Connection conn) throws SQLException {
        String sql = "DELETE FROM comentarios_orden WHERE idOrden = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, orderId);
            stmt.executeUpdate();
        }
    }
}
