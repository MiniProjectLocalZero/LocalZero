package se.mau.localzero.messaging.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.Notification;
import se.mau.localzero.domain.NotificationEntityType;
import se.mau.localzero.domain.User;

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

    Optional<List<Notification>> findByRecipientIdAndReadAtIsNull(Long recipientId);

    Optional<List<Notification>> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    Optional<List<Notification>> findByRecipientAndEntityTypeOrderByCreatedAtDesc(User recipient, NotificationEntityType entityType);
}
