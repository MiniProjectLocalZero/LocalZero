package se.mau.localzero.messaging.exception;

/**
 * Thrown when attempting to resolve a notification entity (Message, Initiative, Post, Comment, Like)
 * that no longer exists in the database (e.g., deleted entity).
 */
public class NotificationEntityNotFoundException extends NotificationException {
    public NotificationEntityNotFoundException(String entityType, Long entityId) {
        super("Notification entity not found: " + entityType + " with ID " + entityId);
    }

    public NotificationEntityNotFoundException(String message) {
        super(message);
    }
}

