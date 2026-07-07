package org.upc.aivalidationservice.validation.interfaces.rest.advice;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.upc.aivalidationservice.shared.interfaces.rest.resources.ErrorResponseResource;
import org.upc.aivalidationservice.validation.domain.exceptions.AiAlertNotFoundException;
import org.upc.aivalidationservice.validation.domain.exceptions.EvidenceAnalysisNotFoundException;
import org.upc.aivalidationservice.validation.domain.exceptions.InvalidAiAlertTransitionException;

import java.time.Instant;

@RestControllerAdvice
public class AiValidationControllerAdvice {

    @ExceptionHandler(EvidenceAnalysisNotFoundException.class)
    public ResponseEntity<ErrorResponseResource> handleNotFound(EvidenceAnalysisNotFoundException ex,
                                                                HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(AiAlertNotFoundException.class)
    public ResponseEntity<ErrorResponseResource> handleAlertNotFound(AiAlertNotFoundException ex,
                                                                     HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidAiAlertTransitionException.class)
    public ResponseEntity<ErrorResponseResource> handleInvalidTransition(InvalidAiAlertTransitionException ex,
                                                                        HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseResource> handleGeneric(Exception ex, HttpServletRequest request) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponseResource> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponseResource(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()
        ));
    }
}
