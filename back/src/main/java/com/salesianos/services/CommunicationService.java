package com.salesianos.services;

import com.salesianos.models.Comment;
import com.salesianos.models.Notification;
import com.salesianos.repositories.CommentRepository;
import com.salesianos.repositories.NotificationRepository;
import com.salesianos.repositories.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommunicationService {
    private static final Logger LOGGER = Logger.getLogger(CommunicationService.class.getName());
    private static final Pattern MENTION_PATTERN = Pattern.compile("@\\[([^\\]]+)\\]");

    private final CommentRepository commentRepository = new CommentRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final UserRepository userRepository = new UserRepository();
    private final com.salesianos.repositories.OrderRepository orderRepository = new com.salesianos.repositories.OrderRepository();

    // Comments
    public List<Comment> getCommentsByOrder(long orderId) {
        return commentRepository.findAllByOrder(orderId);
    }

    public boolean addComment(long orderId, long userId, String text) {
        Comment c = new Comment();
        c.setIdOrden(orderId);
        c.setIdUsuario(userId);
        c.setComentario(text);
        boolean ok = commentRepository.save(c);
        if (ok) {
            notifyOnComment(orderId, userId, text);
        }
        return ok;
    }

    // Notifications
    public List<Notification> getNotificationsByUser(long userId) {
        return notificationRepository.findAllByUser(userId);
    }

    public boolean markNotificationRead(long notifId) {
        return notificationRepository.markAsRead(notifId);
    }

    // Users list for @mention autocomplete
    public List<java.util.Map<String, String>> getAllUserNames() {
        return userRepository.findAllUserNames();
    }

    public List<java.util.Map<String, String>> getRelevantUserNames(long orderId) {
        return userRepository.findRelevantUsersForOrder(orderId);
    }

    /**
     * Lógica de notificación al comentar:
     * 1. Siempre notifica al Jefe de Equipo del departamento de la orden
     * 2. Adicionalmente, notifica a cualquier usuario @mencionado en el texto
     * 3. Nunca notifica al propio autor del comentario
     */
    private void notifyOnComment(long orderId, long senderId, String commentText) {
        String senderName = userRepository.findNameById(senderId);
        com.salesianos.models.Order order = orderRepository.findById(orderId);
        String orderLabel = (order != null && order.getDescripcion() != null && !order.getDescripcion().isEmpty()) 
                            ? order.getDescripcion() 
                            : "Orden " + orderId;
        
        Set<Long> notifiedIds = new HashSet<>();

        // 1. Siempre notificar a los Jefes del departamento de la orden
        List<Integer> heads = userRepository.findHeadsByOrder(orderId);
        for (Integer headId : heads) {
            if (headId != null && headId.longValue() != senderId) {
                notificationRepository.create(headId, senderName + " comentó en: " + orderLabel, orderId);
                notifiedIds.add(headId.longValue());
            }
        }

        // 2. Parsear @menciones del texto y notificar a los mencionados
        Set<String> mentionedNames = parseMentions(commentText);
        for (String name : mentionedNames) {
            Long mentionedId = userRepository.findUserIdByName(name.trim());
            if (mentionedId != null && mentionedId != senderId && !notifiedIds.contains(mentionedId)) {
                notificationRepository.create(mentionedId.intValue(),
                    senderName + " te mencionó en " + orderLabel + ": \"" + truncate(commentText, 40) + "\"",
                    orderId);
                notifiedIds.add(mentionedId);
            }
        }

        LOGGER.log(Level.INFO, "Notifications sent for order {0}: {1} recipients (heads: {2}, mentions: {3})",
            new Object[]{orderId, notifiedIds.size(), heads.size(), mentionedNames.size()});
    }

    /**
     * Extrae los nombres de usuario mencionados con @ en el texto.
     * Soporta nombres con espacios como "@Juan García"
     */
    private Set<String> parseMentions(String text) {
        Set<String> mentions = new HashSet<>();
        if (text == null) return mentions;

        Matcher matcher = MENTION_PATTERN.matcher(text);
        while (matcher.find()) {
            String name = matcher.group(1).trim();
            if (!name.isEmpty()) {
                mentions.add(name);
            }
        }
        return mentions;
    }

    private String truncate(String text, int max) {
        if (text == null) return "";
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }
}
