package se.mau.localzero.messaging.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.mau.localzero.domain.Notification;
import se.mau.localzero.domain.User;
import se.mau.localzero.messaging.repository.NotificationRepository;

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
     * @param sender The user who sent the message
     * @param receiver The user receiving the message
     * @param content The message content
     * @param isCrossCommunityCommunication Whether this is a cross-community message
     */
    public void notifyNewMessage(User sender, User receiver, String content, boolean isCrossCommunityCommunication) {
        try {
            String notificationTitle = "New message from " + sender.getUsername();
            
            String notificationMessage;
            if (isCrossCommunityCommunication) {
                notificationMessage = sender.getUsername() + " from a different community sent you a message: \"" + content + "\"";
            } else {
                notificationMessage = "New message from " + sender.getUsername() + " in your community: \"" + content + "\"";
            }

            Notification notification = new Notification(notificationTitle, notificationMessage, receiver);
            notificationRepository.save(notification);
            
            logger.info("Notification created for {} from {}", receiver.getUsername(), sender.getUsername());
        } catch (Exception e) {
            logger.error("Failed to create notification for {} from {}", receiver.getUsername(), sender.getUsername(), e);
        }
    }
}
