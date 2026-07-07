package org.upc.aivalidationservice.validation.domain.exceptions;

public class InvalidAiAlertTransitionException extends RuntimeException {
    public InvalidAiAlertTransitionException(String message) {
        super(message);
    }
}
