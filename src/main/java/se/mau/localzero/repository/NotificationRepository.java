package se.mau.localzero.repository;

import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.Notification;

import java.util.List;
import java.util.Optional;

/**
 * Repository for handling Notification data access. Provides methods to retrieve notifications for a user,
 * including unread notifications and all notifications sorted by creation date.
 *
 * @Author Carl Lundholm
 */
@Repository
public interface NotificationRepository {

    Optional<List<Notification>> findByRecipientAndReadAtIsNull(Long recipientId);

    Optional<List<Notification>> findByRecipientOrderByCreatedAtDesc(Long recipientId);

    Optional<List<Notification>> countUnreadByRecipient(Long recipientId);
}
