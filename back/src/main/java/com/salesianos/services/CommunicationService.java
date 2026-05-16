package com.salesianos.services;

import com.salesianos.models.Comment;
import com.salesianos.models.Notification;
import com.salesianos.repositories.CommentRepository;
import com.salesianos.repositories.NotificationRepository;
import com.salesianos.repositories.UserRepository;
import java.util.List;

public class CommunicationService {
    private final CommentRepository commentRepository = new CommentRepository();
    private final NotificationRepository notificationRepository = new NotificationRepository();
    private final UserRepository userRepository = new UserRepository();

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
            notifyHeads(orderId, userId, text);
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

    private void notifyHeads(long orderId, long senderId, String comment) {
        String senderName = userRepository.findNameById(senderId);
        List<Integer> heads = userRepository.findHeadsByOrder(orderId);
        for (Integer headId : heads) {
            if (headId != senderId) {
                notificationRepository.create(headId, senderName + " comentó en la orden " + orderId, orderId);
            }
        }
    }
}
