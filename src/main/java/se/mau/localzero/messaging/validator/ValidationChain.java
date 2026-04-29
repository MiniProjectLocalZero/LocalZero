package se.mau.localzero.messaging.validator;

import org.springframework.stereotype.Service;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.Notification;
import se.mau.localzero.messaging.exception.InvalidMessageException;
import se.mau.localzero.messaging.exception.NotificationValidationException;

/**
 * Chain of responsibility pattern for message validation.
 * This class manages the validation chain, ensuring that messages are validated in the correct order.
 */
@Service
public class ValidationChain {

    private final ContentValidator contentValidator;
    private final UserAccessValidator userAccessValidator;
    private final CommunityValidator communityValidator;

    public ValidationChain(
            ContentValidator contentValidator,
            UserAccessValidator userAccessValidator,
            CommunityValidator communityValidator
    ) {
        this.contentValidator = contentValidator;
        this.userAccessValidator = userAccessValidator;
        this.communityValidator = communityValidator;
    }

    private void buildMessageChain() {
        contentValidator.setNext(userAccessValidator);
        userAccessValidator.setNext(communityValidator);
    }

    private void buildNotificationChain() {
        contentValidator.setNext(userAccessValidator);
    }
    
    /**
     * Execute the entire validation chain for messages
     * @param message The message to validate
     * @return true if all validators pass
     * @throws InvalidMessageException if any validator fails
     */
    public boolean validateMessage(Message message) {
        buildMessageChain();
        return contentValidator.checkAndPassMessageToNext(message);
    }

    /**
     * Execute the entire validation chain for notifications
     * @param notification The notification to validate
     * @return true if all validators pass
     * @throws NotificationValidationException if any validator fails
     */
    public boolean validateNotification(Notification notification) {
        buildNotificationChain();
        return contentValidator.checkAndPassNotificationToNext(notification);
    }
}