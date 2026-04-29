package se.mau.localzero.messaging.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.Message;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Message entity, providing methods to retrieve messages based on receiver and conversation between users.
 * This interface abstracts the data access layer for messages, allowing for easy retrieval of unread messages, all messages for a receiver, and conversations between two users.
 *
 * Note: All queries automatically exclude soft-deleted messages (where deletedAt is not null).
 *
 * @Author Carl Lundholm
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    /**
     * Find all unread messages for a specific receiver.
     * Excludes soft-deleted messages.
     *
     * @param receiverId The ID of the receiver
     * @return Optional list of unread messages
     */
    Optional<List<Message>> findByReceiverAndReadAtIsNull(Long receiverId);

    /**
     * Find all messages for a specific receiver, ordered by creation date (newest first).
     * Excludes soft-deleted messages.
     *
     * @param receiverId The ID of the receiver
     * @return Optional list of messages
     */
    Optional<List<Message>> findByReceiverOrderByCreatedAtDesc(Long receiverId);

    /**
     * Find all messages in a conversation between two users.
     * Excludes soft-deleted messages.
     * Returns messages ordered by creation date (oldest first for conversation flow).
     *
     * @param senderId The ID of the first user
     * @param receiverId The ID of the second user
     * @return Optional list of messages in the conversation (both directions)
     */
    @Query("SELECT m FROM Message m WHERE " +
            "((m.sender.id = :senderId AND m.receiver.id = :receiverId) OR " +
            "(m.sender.id = :receiverId AND m.receiver.id = :senderId)) " +
            "AND m.deletedAt IS NULL " +
            "ORDER BY m.createdAt ASC")
    Optional<List<Message>> findConversationBetween(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}
