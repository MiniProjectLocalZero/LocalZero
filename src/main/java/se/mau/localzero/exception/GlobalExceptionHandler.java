package se.mau.localzero.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public abstract class GlobalExceptionHandler extends RuntimeException {

    @ExceptionHandler(Exception.class)
    public abstract ResponseEntity<ErrorResponse> handleGlobalException(Exception ex);
}
