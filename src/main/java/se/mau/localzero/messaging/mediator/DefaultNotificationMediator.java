package se.mau.localzero.messaging.mediator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.domain.*;
import se.mau.localzero.messaging.exception.NotificationValidationException;
import se.mau.localzero.messaging.repository.NotificationRepository;
import se.mau.localzero.messaging.validator.ValidationChain;

import java.util.List;

/**
 * Default implementation of NotificationMediator.
 * Orchestrates the workflow for creating, validating, and persisting notifications.
 */
@Service
@Transactional
public class DefaultNotificationMediator implements NotificationMediator {

    private static final Logger logger = LoggerFactory.getLogger(DefaultNotificationMediator.class);

    private final NotificationRepository notificationRepository;
    private final ValidationChain validationChain;

    public DefaultNotificationMediator(
            NotificationRepository notificationRepository,
            ValidationChain validationChain
    ) {
        this.notificationRepository = notificationRepository;
        this.validationChain = validationChain;
    }

    /**
     * Create a notification for a new message.
     * @param sender The user who sent the message
     * @param receiver The user receiving the message
     * @param message The Message entity (persisted)
     * @param isCrossCommunity Whether this is a cross-community message
     * @throws NotificationValidationException if validation fails
     */
    @Override
    public void sendMessageNotification(User sender, User receiver, Message message, boolean isCrossCommunity) {
        logger.debug("Step 1: Building message notification");

        Notification notification = buildMessageNotification(sender, receiver, message, isCrossCommunity);

        logger.debug("Step 2: Validating message notification");
        if (!validationChain.validateNotification(notification)) {
            throw new NotificationValidationException("Message notification validation failed");
        }

        logger.debug("Step 3: Persisting message notification");
        notificationRepository.save(notification);

        logger.info("✓ Message notification created for {} from {} (Message ID: {})",
                   receiver.getUsername(), sender.getUsername(), message.getId());
    }

    /**
     * Create notifications for new initiatives.
     * @param recipients List of users to notify
     * @param initiative The Initiative entity (persisted)
     * @throws NotificationValidationException if validation fails
     */
    @Override
    public void sendInitiativeNotification(List<User> recipients, Initiative initiative) {
        logger.debug("Step 1: Validating initiative and recipients");

        if (initiative == null || initiative.getId() == null) {
            throw new NotificationValidationException("Initiative must be persisted before creating notification");
        }

        if (recipients == null || recipients.isEmpty()) {
            throw new NotificationValidationException("Recipients list cannot be null or empty");
        }

        logger.debug("Step 2: Building initiative notifications for {} recipients", recipients.size());

        for (User recipient : recipients) {
            if (recipient == null) continue;

            Notification notification = buildInitiativeNotification(recipient, initiative);

            logger.debug("Step 3: Validating initiative notification for {}", recipient.getUsername());
            if (!validationChain.validateNotification(notification)) {
                throw new NotificationValidationException("Initiative notification validation failed for " + recipient.getUsername());
            }

            logger.debug("Step 4: Persisting initiative notification for {}", recipient.getUsername());
            notificationRepository.save(notification);
            logger.info("✓ Initiative notification created for {}", recipient.getUsername());
        }

        logger.info("✓ Initiative notifications created for {} recipients", recipients.size());
    }

    /**
     * Create a notification for a new comment.
     * @param recipient The post author
     * @param comment The Comment entity (persisted)
     * @throws NotificationValidationException if validation fails
     */
    @Override
    public void sendCommentNotification(User recipient, Comment comment) {
        logger.debug("Step 1: Building comment notification");

        Notification notification = buildCommentNotification(recipient, comment);

        logger.debug("Step 2: Validating comment notification");
        if (!validationChain.validateNotification(notification)) {
            throw new NotificationValidationException("Comment notification validation failed");
        }

        logger.debug("Step 3: Persisting comment notification");
        notificationRepository.save(notification);

        logger.info("✓ Comment notification created for {}", recipient.getUsername());
    }

    /**
     * Create a notification for a new like.
     * @param postAuthor The author of the post
     * @param like The Like entity (persisted)
     * @throws NotificationValidationException if validation fails
     */
    @Override
    public void sendLikeNotification(User postAuthor, Like like) {
        logger.debug("Step 1: Building like notification");

        Notification notification = buildLikeNotification(postAuthor, like);

        logger.debug("Step 2: Validating like notification");
        if (!validationChain.validateNotification(notification)) {
            throw new NotificationValidationException("Like notification validation failed");
        }

        logger.debug("Step 3: Persisting like notification");
        notificationRepository.save(notification);

        logger.info("✓ Like notification created for {}", postAuthor.getUsername());
    }

    /**
     * Create notifications for a new post.
     * Notifies all participants in the post's scope (initiative or community), excluding the post author.
     *
     * @param post The Post entity (persisted) with author, initiative/community set
     * @throws NotificationValidationException if validation fails
     */
    @Override
    public void sendPostNotification(Post post) {
        logger.debug("Step 1: Validating post and scope");

        if (post == null || post.getId() == null) {
            throw new NotificationValidationException("Post must be persisted before creating notification");
        }

        if (post.getAuthor() == null) {
            throw new NotificationValidationException("Post author cannot be null");
        }

        post.validateScope();

        List<User> recipients;
        if (post.isInInitiative()) {
            logger.debug("Step 2: Collecting initiative participants for post {}", post.getId());
            recipients = post.getInitiative().getParticipants()
                    .stream()
                    .filter(user -> !user.equals(post.getAuthor()))
                    .toList();
            logger.debug("Found {} initiative participants (excluding author)", recipients.size());
        } else {
            logger.debug("Step 2: Collecting community members for post {}", post.getId());
            recipients = post.getCommunity().getMembers()
                    .stream()
                    .filter(user -> !user.equals(post.getAuthor()))
                    .toList();
            logger.debug("Found {} community members (excluding author)", recipients.size());
        }

        if (recipients.isEmpty()) {
            logger.info("No recipients to notify for post {} (only author)", post.getId());
            return;
        }

        logger.debug("Step 3: Building post notifications for {} recipients", recipients.size());

        for (User recipient : recipients) {
            Notification notification = buildPostNotification(post, recipient);

            logger.debug("Step 4: Validating post notification for {}", recipient.getUsername());
            if (!validationChain.validateNotification(notification)) {
                throw new NotificationValidationException("Post notification validation failed for " + recipient.getUsername());
            }

            logger.debug("Step 5: Persisting post notification for {}", recipient.getUsername());
            notificationRepository.save(notification);
        }

        logger.info("✓ Post notifications created for {} recipients", recipients.size());
    }

    /**
     * Build a message notification with appropriate title and content.
     * @param sender The user who sent the message
     * @param receiver The user receiving the message
     * @param message The Message entity (persisted)
     * @param isCrossCommunity Whether this is a cross-community message
     * @return A Notification object ready for validation and persistence
     */
    private Notification buildMessageNotification(User sender, User receiver, Message message, boolean isCrossCommunity) {
        String title = "New message from " + sender.getUsername();
        String messagePreview = truncate(message.getContent(), 100);

        String content;
        if (isCrossCommunity) {
            content = sender.getUsername() + " from a different community sent you a message: \"" + messagePreview + "\"";
        } else {
            content = "New message from " + sender.getUsername() + " in your community: \"" + messagePreview + "\"";
        }

        return new Notification(title, content, receiver, NotificationEntityType.MESSAGE, message.getId());
    }

    /**
     * Build an initiative notification.
     * @param recipient The user to notify
     * @param initiative The Initiative entity (persisted)
     * @return A Notification object ready for validation and persistence
     */
    private Notification buildInitiativeNotification(User recipient, Initiative initiative) {
        String title = "New Initiative: " + initiative.getTitle();
        String content = "A new initiative has been created in your community. Category: " + initiative.getCategory();

        return new Notification(title, content, recipient, NotificationEntityType.INITIATIVE, initiative.getId());
    }

    /**
     * Build a comment notification.
     * @param recipient The user to notify (post author)
     * @param comment The Comment entity (persisted)
     * @return A Notification object ready for validation and persistence
     */
    private Notification buildCommentNotification(User recipient, Comment comment) {
        String title = comment.getAuthor().getUsername() + " commented on your post";
        String content = "\"" + truncate(comment.getContent(), 100) + "\"";

        return new Notification(title, content, recipient, NotificationEntityType.COMMENT, comment.getId());
    }

    /**
     * Build a like notification.
     * @param postAuthor The user who received the like
     * @param like The Like entity (persisted)
     * @return A Notification object ready for validation and persistence
     */
    private Notification buildLikeNotification(User postAuthor, Like like) {
        String title = like.getUser().getUsername() + " liked your post";
        String content = "Your post received a like";

        return new Notification(title, content, postAuthor, NotificationEntityType.LIKE, like.getId());
    }

    /**
     * Build a post notification.
     * @param post The Post entity (persisted)
     * @param recipient The user to notify
     * @return A Notification object ready for validation and persistence
     */
    private Notification buildPostNotification(Post post, User recipient) {
        String title = "New post in " + (post.isInInitiative() ? post.getInitiative().getTitle() : "Community");
        String content = post.getAuthor().getUsername() + " has made a new post: \"" + truncate(post.getContent(), 100) + "\"";

        return new Notification(title, content, recipient, NotificationEntityType.POST, post.getId());
    }

    /**
     * Truncate text to a maximum length with "..." suffix if needed.
     * @param text The text to truncate
     * @param maxLength The maximum length of the text before truncation
     * @return The truncated text with "..." if it was too long, or the original text if it was within the limit
     */
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...";
    }
}

