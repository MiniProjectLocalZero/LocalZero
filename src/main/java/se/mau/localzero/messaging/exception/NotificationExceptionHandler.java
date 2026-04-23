package se.mau.localzero.messaging.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import se.mau.localzero.exception.ErrorResponse;
import se.mau.localzero.exception.GlobalExceptionHandler;

import java.time.LocalDateTime;

/**
 * Global exception handler for notification-related exceptions.
 * Handles all exceptions thrown from the notification domain and returns
 * appropriate HTTP status codes and error responses.
 */
@ControllerAdvice
public class NotificationExceptionHandler extends GlobalExceptionHandler {

    @Override
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle base NotificationException - indicates a general notification error.
     * Returns 400 BAD_REQUEST.
     */
    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ErrorResponse> handleNotificationException(NotificationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Notification error",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle validation errors - thrown when notification data fails validation.
     * Returns 400 BAD_REQUEST.
     */
    @ExceptionHandler(NotificationValidationException.class)
    public ResponseEntity<ErrorResponse> handleNotificationValidationException(NotificationValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Notification validation failed",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle entity not found errors - thrown when referenced entity (Message, Post, etc.) doesn't exist.
     * Returns 404 NOT_FOUND.
     */
    @ExceptionHandler(NotificationEntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationEntityNotFoundException(NotificationEntityNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Notification entity not found",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle unauthorized access - thrown when user tries to access/modify notifications they don't own.
     * Returns 401 UNAUTHORIZED.
     */
    @ExceptionHandler(UnauthorizedNotificationAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedNotificationAccessException(UnauthorizedNotificationAccessException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized notification access",
                ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}

