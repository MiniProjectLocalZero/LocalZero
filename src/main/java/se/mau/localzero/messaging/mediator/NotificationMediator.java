package se.mau.localzero.messaging.mediator;

import se.mau.localzero.domain.*;

import java.util.List;

/**
 * Mediator interface for orchestrating notification creation.
 * Encapsulates the business logic for building, validating, and persisting notifications.
 *
 * Implementations handle the workflow of notification creation, ensuring proper validation
 * and error handling across different notification types.
 */
public interface NotificationMediator {

    /**
     * Create and send a notification for a new message.
     *
     * @param sender The user who sent the message
     * @param receiver The user receiving the message
     * @param message The Message entity (persisted)
     * @param isCrossCommunity Whether this is a cross-community message
     */
    void sendMessageNotification(User sender, User receiver, Message message, boolean isCrossCommunity);

    /**
     * Create and send notifications for a new initiative.
     *
     * @param recipients List of users to notify
     * @param initiative The Initiative entity (persisted)
     */
    void sendInitiativeNotification(List<User> recipients, Initiative initiative);

    /**
     * Create and send a notification for a new comment.
     *
     * @param recipient The post author
     * @param comment The Comment entity (persisted)
     */
    void sendCommentNotification(User recipient, Comment comment);

    /**
     * Create and send a notification for a new like.
     *
     * @param postAuthor The author of the post
     * @param like The Like entity (persisted)
     */
    void sendLikeNotification(User postAuthor, Like like);

    /**
     * Create and send notifications for a new post.
     * Notifies all participants in the post's scope (initiative or community), excluding the post author.
     *
     * @param post The Post entity (persisted) with author, initiative/community set
     */
    void sendPostNotification(Post post);
}
