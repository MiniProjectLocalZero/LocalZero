package se.mau.localzero.messaging.exception;

/**
 * Thrown when a user attempts to access or modify notifications they don't own.
 * Examples: trying to mark another user's notification as read, delete another user's notification.
 */
public class UnauthorizedNotificationAccessException extends NotificationException {
    public UnauthorizedNotificationAccessException(String message) {
        super(message);
    }
}
