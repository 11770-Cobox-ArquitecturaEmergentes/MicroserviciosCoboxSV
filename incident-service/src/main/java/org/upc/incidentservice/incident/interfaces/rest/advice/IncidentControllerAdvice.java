package org.upc.incidentservice.incident.interfaces.rest.advice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.upc.incidentservice.incident.domain.exceptions.IncidentNotFoundException;
import org.upc.incidentservice.incident.domain.exceptions.InvalidIncidentStatusTransitionException;
import org.upc.incidentservice.shared.interfaces.rest.resources.ErrorResponseResource;

import java.time.LocalDateTime;

@RestControllerAdvice
public class IncidentControllerAdvice {

    @ExceptionHandler({
            IncidentNotFoundException.class
    })
    public ResponseEntity<ErrorResponseResource> handleNotFoundException(RuntimeException ex, WebRequest request) {
        var errorResponse = new ErrorResponseResource(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
            InvalidIncidentStatusTransitionException.class
    })
    public ResponseEntity<ErrorResponseResource> handleBusinessStateConflictException(RuntimeException ex, WebRequest request) {
        var errorResponse = new ErrorResponseResource(
                LocalDateTime.now(),
                ex.getMessage(),
                request.getDescription(false)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseResource> handleGlobalException(Exception ex, WebRequest request) {
        var errorResponse = new ErrorResponseResource(
                LocalDateTime.now(),
                "Unexpected error in incident context",
                request.getDescription(false) + " | Details: " + ex.getMessage()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
