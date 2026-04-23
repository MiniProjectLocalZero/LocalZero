package se.mau.localzero.messaging.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.Notification;
import se.mau.localzero.domain.NotificationEntityType;

import java.util.List;
import java.util.Optional;

/**
 * Repository for handling Notification data access.
 * Provides methods for querying, filtering, and managing notifications.
 *
 * @Author Carl Lundholm
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all unread notifications for a recipient, ordered by creation date (newest first).
     */
    Optional<List<Notification>> findByRecipientAndReadAtIsNull(Long recipientId);

    /**
     * Find all notifications for a recipient, ordered by creation date (newest first).
     */
    Optional<List<Notification>> findByRecipientOrderByCreatedAtDesc(Long recipientId);

    /**
     * Count unread notifications for a recipient.
     */
    Optional<List<Notification>> countUnreadByRecipient(Long recipientId);

    /**
     * Find notification by entity type and entity ID.
     * Useful for checking if a notification exists for a specific entity.
     */
    Optional<Notification> findByEntityTypeAndEntityId(NotificationEntityType entityType, Long entityId);

    /**
     * Find notifications by recipient and entity type (e.g., all MESSAGE notifications).
     */
    Optional<List<Notification>> findByRecipientAndEntityType(Long recipientId, NotificationEntityType entityType);

    /**
     * Save a new notification or update an existing one.
     */
    Notification save(@NonNull Notification notification);

    /**
     * Delete a notification by ID.
     * Used for removing old consolidated notifications.
     */
    void deleteById(@NonNull Long notificationId);

    /**
     * Find notification by ID.
     */
    Optional<Notification> findById(@NonNull Long notificationId);
}
