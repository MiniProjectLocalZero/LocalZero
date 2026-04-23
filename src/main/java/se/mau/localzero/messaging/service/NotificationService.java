package se.mau.localzero.messaging.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.*;
import se.mau.localzero.messaging.exception.NotificationEntityNotFoundException;
import se.mau.localzero.messaging.exception.NotificationValidationException;
import se.mau.localzero.messaging.exception.UnauthorizedNotificationAccessException;
import se.mau.localzero.messaging.repository.NotificationRepository;

import java.util.List;

/**
 * Service for managing notifications in LocalZero.
 * Responsible for retrieval, management, and utility operations on notifications.
 */
@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Generate a navigation link for a notification (for use in Thymeleaf templates).
     * Allows users to click on notification and go directly to the related entity.
     *
     * @param notification The notification
     * @return The URL path to navigate to (e.g., /messages/conversation/123)
     * @throws NotificationValidationException if entity type is unknown
     */
    public String getNotificationLink(Notification notification) {
        try {
            if (notification == null || notification.getEntityType() == null) {
                throw new NotificationValidationException("Notification and entity type cannot be null");
            }

            return switch (notification.getEntityType()) {
                case MESSAGE -> "/messages/conversation/" + notification.getEntityId();
                case INITIATIVE -> "/initiatives/" + notification.getEntityId();
                case POST -> "/posts/" + notification.getEntityId();
                case COMMENT -> "/posts/" + notification.getEntityId() + "#comment-" + notification.getEntityId();
                case LIKE -> "/posts/" + notification.getEntityId();
                case COMMUNITY -> "/communities/" + notification.getEntityId();
                case OTHER -> "/notifications";
            };
        } catch (NotificationValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to generate notification link", e);
            throw new NotificationValidationException("Failed to generate link: " + e.getMessage(), e);
        }
    }

    /**
     * Get the notification inbox for a user (all notifications ordered by creation date).
     *
     * @param recipient The user
     * @return List of all notifications for the user
     * @throws NotificationValidationException if recipient is invalid
     */
    public List<Notification> getNotificationInbox(User recipient) {
        if (recipient == null || recipient.getId() == null) {
            throw new NotificationValidationException("User must be valid");
        }

        return notificationRepository
                .findByRecipientOrderByCreatedAtDesc(recipient.getId())
                .orElse(List.of());
    }

    /**
     * Get all unread notifications for a user, ordered by creation date (newest first).
     *
     * @param recipient The user
     * @return List of unread notifications
     * @throws NotificationValidationException if recipient is invalid
     */
    public List<Notification> getUnreadNotifications(User recipient) {
        if (recipient == null || recipient.getId() == null) {
            throw new NotificationValidationException("Recipient must be valid");
        }

        return notificationRepository
                .findByRecipientAndReadAtIsNull(recipient.getId())
                .orElse(List.of());
    }

    /**
     * Get count of unread notifications for a user.
     *
     * @param recipient The user
     * @return Count of unread notifications
     * @throws NotificationValidationException if recipient is invalid
     */
    public long getUnreadNotificationCount(User recipient) {
        if (recipient == null || recipient.getId() == null) {
            throw new NotificationValidationException("Recipient must be valid");
        }

        List<Notification> unreadNotifications = getUnreadNotifications(recipient);
        return unreadNotifications.size();
    }

    /**
     * Mark a notification as read with ownership verification.
     *
     * @param notificationId The notification ID
     * @param currentUser The user attempting the operation
     * @throws NotificationEntityNotFoundException if notification doesn't exist
     * @throws UnauthorizedNotificationAccessException if user doesn't own the notification
     */
    @Transactional(readOnly = false)
    public void markAsRead(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationEntityNotFoundException("Notification", notificationId));

        if (!notification.getRecipient().equals(currentUser)) {
            throw new UnauthorizedNotificationAccessException(
                    "Cannot mark as read: you do not own this notification"
            );
        }
        notification.markAsRead();
        notificationRepository.save(notification);
        logger.info("✓ Notification marked as read: {}", notificationId);
    }

    /**
     * Mark a notification as unread with ownership verification.
     *
     * @param notificationId The notification ID
     * @param currentUser The user attempting the operation
     * @throws NotificationEntityNotFoundException if notification doesn't exist
     * @throws UnauthorizedNotificationAccessException if user doesn't own the notification
     */
    @Transactional(readOnly = false)
    public void markAsUnread(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationEntityNotFoundException("Notification", notificationId));

        if (!notification.getRecipient().equals(currentUser)) {
            throw new UnauthorizedNotificationAccessException(
                    "Cannot mark as unread: you do not own this notification"
            );
        }

        notification.markAsUnread();
        notificationRepository.save(notification);
        logger.info("✓ Notification marked as unread: {}", notificationId);
    }

    /**
     * Delete a notification with ownership verification.
     *
     * @param notificationId The notification ID
     * @param currentUser The user attempting the operation
     * @throws NotificationEntityNotFoundException if notification doesn't exist
     * @throws UnauthorizedNotificationAccessException if user doesn't own the notification
     */
    @Transactional(readOnly = false)
    public void deleteNotification(Long notificationId, User currentUser) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationEntityNotFoundException("Notification", notificationId));

        if (!notification.getRecipient().equals(currentUser)) {
            throw new UnauthorizedNotificationAccessException(
                    "Cannot delete: you do not own this notification"
            );
        }

        notificationRepository.deleteById(notificationId);
        logger.info("✓ Notification deleted: {}", notificationId);
    }
}
