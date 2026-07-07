package org.upc.iamservice.shared.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.upc.iamservice.shared.interfaces.rest.resources.ErrorResponseResource;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseResource> handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                "Invalid request payload"
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseResource> handleRuntimeException(
            RuntimeException exception
    ) {
        var message = exception.getMessage() == null || exception.getMessage().isBlank()
                ? "Unexpected runtime error"
                : exception.getMessage();

        if (message.equalsIgnoreCase("Email already exists")) {
            return buildResponse(HttpStatus.CONFLICT, message, "User registration");
        }

        if (message.equalsIgnoreCase("Role not found")) {
            return buildResponse(HttpStatus.BAD_REQUEST, message, "Role assignment");
        }

        if (message.equalsIgnoreCase("User not found")) {
            return buildResponse(HttpStatus.NOT_FOUND, message, "User profile");
        }

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                message,
                "Unhandled application exception"
        );
    }

    private ResponseEntity<ErrorResponseResource> buildResponse(
            HttpStatus status,
            String message,
            String details
    ) {
        return ResponseEntity.status(status).body(
                new ErrorResponseResource(
                        LocalDateTime.now(),
                        message,
                        details
                )
        );
    }
}
