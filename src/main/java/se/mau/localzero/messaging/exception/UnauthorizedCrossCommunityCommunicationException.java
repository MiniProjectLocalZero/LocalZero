package se.mau.localzero.messaging.exception;

/**
 * Exception thrown when a user attempts to send a message across communities
 * without being participants in a shared initiative.
 */
public class UnauthorizedCrossCommunityCommunicationException extends RuntimeException {
    public UnauthorizedCrossCommunityCommunicationException(String message) {
        super(message);
    }

    public UnauthorizedCrossCommunityCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}

