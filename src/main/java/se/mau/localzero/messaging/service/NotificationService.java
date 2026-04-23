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
 * Provides methods for creating, retrieving, and managing notifications.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Notify receiver of a new message from sender.
     *
     * Creates a new notification for each message without deleting previous ones.
     * All notifications from the same sender are kept in the inbox as unread.
     *
     * Note: The unread count mechanism groups notifications by sender - multiple
     * unread notifications from the same sender count as 1 in the badge/counter.
     *
     * @param sender The user who sent the message
     * @param receiver The user receiving the message
     * @param message The Message entity (for storing entityId)
     * @param messagePreview The message content preview (for display)
     * @param isCrossCommunityCommunication Whether this is a cross-community message
     * @throws NotificationValidationException if sender or receiver is null
     */
    @Transactional
    public void notifyNewMessage(User sender, User receiver, Message message,
                                 String messagePreview, boolean isCrossCommunityCommunication) {
        try {
            // Validation
            if (sender == null || receiver == null) {
                throw new NotificationValidationException("Sender and receiver cannot be null");
            }

            if (message == null || message.getId() == null) {
                throw new NotificationValidationException("Message entity must be persisted before creating notification");
            }

            String notificationTitle = "New message from " + sender.getUsername();

            String notificationMessage;
            if (isCrossCommunityCommunication) {
                notificationMessage = sender.getUsername() + " from a different community sent you a message: \"" + messagePreview + "\"";
            } else {
                notificationMessage = "New message from " + sender.getUsername() + " in your community: \"" + messagePreview + "\"";
            }

            Notification notification = new Notification(
                    notificationTitle,
                    notificationMessage,
                    receiver,
                    NotificationEntityType.MESSAGE,
                    message.getId()
            );

            notificationRepository.save(notification);

            logger.info("✓ Message notification created for {} from {} (Message ID: {})",
                       receiver.getUsername(), sender.getUsername(), message.getId());

        } catch (NotificationValidationException e) {
            logger.error("Validation error creating message notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create message notification for {} from {}",
                        receiver.getUsername(), sender.getUsername(), e);
            throw new NotificationValidationException("Failed to create notification: " + e.getMessage(), e);
        }
    }

    /**
     * Notify users of a new initiative (no consolidation - each event is unique).
     * Creates notifications for multiple recipients (typically community members).
     *
     * @param recipients List of users to notify
     * @param initiative The Initiative entity
     * @throws NotificationValidationException if initiative or recipients are null/empty
     */
    @Transactional
    public void notifyNewInitiative(List<User> recipients, Initiative initiative) {
        try {
            if (initiative == null || initiative.getId() == null) {
                throw new NotificationValidationException("Initiative must be persisted before creating notification");
            }

            if (recipients == null || recipients.isEmpty()) {
                throw new NotificationValidationException("Recipients list cannot be null or empty");
            }

            String notificationTitle = "New Initiative: " + initiative.getTitle();
            String notificationMessage = "A new initiative has been created in your community. Category: "
                    + initiative.getCategory();

            for (User recipient : recipients) {
                if (recipient == null) continue;

                Notification notification = new Notification(
                        notificationTitle,
                        notificationMessage,
                        recipient,
                        NotificationEntityType.INITIATIVE,
                        initiative.getId()
                );

                notificationRepository.save(notification);
                logger.info("✓ Initiative notification created for {}", recipient.getUsername());
            }

            logger.info("✓ Initiative notifications created for {} recipients", recipients.size());

        } catch (NotificationValidationException e) {
            logger.error("Validation error creating initiative notification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create initiative notifications", e);
            throw new NotificationValidationException("Failed to create initiative notifications: " + e.getMessage(), e);
        }
    }

    /**
     * Notify a user of a new comment on their post.
     *
     * @param recipient The post author
     * @param comment The Comment entity
     * @throws NotificationValidationException if recipient or comment is null
     */
    @Transactional
    public void notifyNewComment(User recipient, Comment comment) {
        try {
            if (recipient == null || comment == null || comment.getId() == null) {
                throw new NotificationValidationException("Recipient and comment must be valid");
            }

            String notificationTitle = comment.getAuthor().getUsername() + " commented on your post";
            String notificationMessage = "\"" + truncate(comment.getContent(), 100) + "\"";

            Notification notification = new Notification(
                    notificationTitle,
                    notificationMessage,
                    recipient,
                    NotificationEntityType.COMMENT,
                    comment.getId()
            );

            notificationRepository.save(notification);
            logger.info("✓ Comment notification created for {}", recipient.getUsername());

        } catch (NotificationValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create comment notification", e);
            throw new NotificationValidationException("Failed to create comment notification: " + e.getMessage(), e);
        }
    }

    /**
     * Notify a user that someone liked their post.
     *
     * @param postAuthor The author of the post
     * @param like The Like entity
     * @throws NotificationValidationException if postAuthor or like is null
     */
    @Transactional
    public void notifyNewLike(User postAuthor, Like like) {
        try {
            if (postAuthor == null || like == null || like.getId() == null) {
                throw new NotificationValidationException("Post author and like must be valid");
            }

            String notificationTitle = like.getUser().getUsername() + " liked your post";
            String notificationMessage = "Your post received a like";

            Notification notification = new Notification(
                    notificationTitle,
                    notificationMessage,
                    postAuthor,
                    NotificationEntityType.LIKE,
                    like.getId()
            );

            notificationRepository.save(notification);
            logger.info("✓ Like notification created for {}", postAuthor.getUsername());

        } catch (NotificationValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Failed to create like notification", e);
            throw new NotificationValidationException("Failed to create like notification: " + e.getMessage(), e);
        }
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
     * Get all unread notifications for a user, ordered by creation date (newest first).
     *
     * @param recipient The user
     * @return List of unread notifications
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
     * This counts UNIQUE message senders as ONE notification, even if multiple messages were sent.
     *
     * @param recipient The user
     * @return Count of unread notifications
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
    @Transactional
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
    @Transactional
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
    @Transactional
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

    /**
     * Helper method to truncate text to a maximum length.
     *
     * @param text The text to truncate
     * @param maxLength The maximum length
     * @return Truncated text with "..." appended if truncated
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}
