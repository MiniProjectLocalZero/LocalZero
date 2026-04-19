package se.mau.localzero.messaging.repository;

import org.springframework.stereotype.Repository;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.User;

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
public interface MessageRepository {
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
     *
     * @param sender The sender user
     * @param receiver The receiver user
     * @return Optional list of messages in the conversation
     */
    Optional<List<Message>> findConversationBetween(User sender, User receiver);

    /**
     * Save or update a message.
     *
     * @param message The message to save
     * @return The saved message with generated ID if applicable
     */
    Message save(Message message);

    /**
     * Hard delete a message (physically remove from database).
     * Note: For soft deletes, use Message.markAsDeleted() and save() instead.
     *
     * @param message The message to delete
     */
    void delete(Message message);

    /**
     * Update an existing message.
     *
     * @param message The message to update
     * @return The updated message
     */
    Message update(Message message);
}
