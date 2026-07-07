package org.upc.aivalidationservice.validation.domain.exceptions;

import java.util.UUID;

public class AiAlertNotFoundException extends RuntimeException {
    public AiAlertNotFoundException(UUID alertId) {
        super("AI alert not found: " + alertId);
    }
}
