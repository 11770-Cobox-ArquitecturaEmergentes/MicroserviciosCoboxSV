package org.upc.edgeservice.edge.interfaces.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.upc.edgeservice.shared.interfaces.rest.resources.ErrorResponseResource;

import java.time.LocalDateTime;

@RestControllerAdvice
public class EdgeControllerAdvice {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseResource> handleGlobalException(Exception ex, WebRequest request) {
        var errorResponse = new ErrorResponseResource(
                LocalDateTime.now(),
                "Ocurrió un error inesperado en el contexto edge",
                request.getDescription(false) + " | Details: " + ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
