package org.upc.mobilebffservice.mobile.interfaces.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.upc.mobilebffservice.mobile.domain.exceptions.InvalidUploadIntentException;
import org.upc.mobilebffservice.mobile.domain.exceptions.UploadConfirmationException;
import org.upc.mobilebffservice.mobile.domain.exceptions.UploadIntentNotFoundException;
import org.upc.mobilebffservice.shared.interfaces.rest.resources.ErrorResponseResource;

import java.time.LocalDateTime;

@RestControllerAdvice
public class MobileBffControllerAdvice {

    @ExceptionHandler(InvalidUploadIntentException.class)
    public ResponseEntity<ErrorResponseResource> handleBadRequest(RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(error(ex, request), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UploadIntentNotFoundException.class)
    public ResponseEntity<ErrorResponseResource> handleNotFound(RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(error(ex, request), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UploadConfirmationException.class)
    public ResponseEntity<ErrorResponseResource> handleConflict(RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(error(ex, request), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseResource> handleGlobalException(Exception ex, WebRequest request) {
        var errorResponse = new ErrorResponseResource(
                LocalDateTime.now(),
                "Ocurrió un error inesperado en el contexto mobile-bff",
                request.getDescription(false) + " | Details: " + ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponseResource error(RuntimeException ex, WebRequest request) {
        return new ErrorResponseResource(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
    }
}
