package se.mau.localzero.messaging.exception;

/**
 * Thrown when notification creation or update fails validation.
 * Examples: invalid recipient, missing entity type, null content, etc.
 */
public class NotificationValidationException extends NotificationException {
    public NotificationValidationException(String message) {
        super(message);
    }

    public NotificationValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

