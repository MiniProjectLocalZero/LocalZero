package se.mau.localzero.messaging.validator;


import org.springframework.stereotype.Component;
import se.mau.localzero.domain.Message;
import se.mau.localzero.domain.Notification;
import se.mau.localzero.domain.NotificationEntityType;
import se.mau.localzero.messaging.exception.InvalidMessageException;
import se.mau.localzero.messaging.exception.NotificationValidationException;

@Component
public class ContentValidator extends Validator {

    @Override
    public boolean validate(Message message) {
        String content = message.getContent();
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidMessageException("Message content cannot be empty");
        }

        if (content.length() > 5000) {
            throw new InvalidMessageException("Message content cannot be longer than 5000 characters");
        }

        return true;
    }

    @Override
    public boolean validate(Notification notification) {
        String title = notification.getTitle();

        if (title == null || title.trim().isEmpty()) {
            throw new NotificationValidationException("Notification title cannot be empty");
        }

        if (title.length() > 200) {
            throw new NotificationValidationException("Notification title cannot be longer than 200 characters");
        }

        String content = notification.getMessage();
        if (content == null || content.trim().isEmpty()) {
            throw new NotificationValidationException("Notification content cannot be empty");
        }

        if (content.length() > 500) {
            throw new NotificationValidationException("Notification content cannot be longer than 500 characters");
        }

        NotificationEntityType entityType = notification.getEntityType();



        if (entityType == null) {
            throw new NotificationValidationException("Notification entity type cannot be null");
        }


        switch (entityType) {
            case MESSAGE, INITIATIVE, COMMUNITY -> {
                if (notification.getEntityId() == null) {
                    throw new NotificationValidationException(
                            "Notification entity ID cannot be null for " + entityType.name().toLowerCase() + " notifications"
                    );
                }
            }
            case OTHER -> {
                if (notification.getEntityId() != null) {
                    throw new NotificationValidationException("Notification entity ID must be null for other notifications");
                }
            }
        }

        return true;
    }
}
