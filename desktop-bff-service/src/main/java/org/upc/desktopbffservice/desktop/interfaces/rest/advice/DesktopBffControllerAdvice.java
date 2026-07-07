package org.upc.desktopbffservice.desktop.interfaces.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.upc.desktopbffservice.desktop.domain.exceptions.DesktopResourceNotFoundException;
import org.upc.desktopbffservice.shared.interfaces.rest.resources.ErrorResponseResource;

import java.time.LocalDateTime;

@RestControllerAdvice
public class DesktopBffControllerAdvice {

    @ExceptionHandler(DesktopResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseResource> handleNotFound(RuntimeException ex, WebRequest request) {
        return new ResponseEntity<>(error(ex, request), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseResource> handleGlobalException(Exception ex, WebRequest request) {
        var errorResponse = new ErrorResponseResource(
                LocalDateTime.now(),
                "Ocurrió un error inesperado en el contexto desktop-bff",
                request.getDescription(false) + " | Details: " + ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ErrorResponseResource error(RuntimeException ex, WebRequest request) {
        return new ErrorResponseResource(LocalDateTime.now(), ex.getMessage(), request.getDescription(false));
    }
}
