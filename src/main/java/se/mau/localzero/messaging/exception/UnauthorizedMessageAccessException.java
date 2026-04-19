package se.mau.localzero.messaging.exception;

public class UnauthorizedMessageAccessException extends RuntimeException {
    public UnauthorizedMessageAccessException(String message) {
        super(message);
    }
}
