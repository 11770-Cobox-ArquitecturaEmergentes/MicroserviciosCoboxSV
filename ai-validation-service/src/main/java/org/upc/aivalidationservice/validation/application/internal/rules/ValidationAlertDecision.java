package org.upc.aivalidationservice.validation.application.internal.rules;

import org.upc.aivalidationservice.validation.domain.model.valueobjects.AlertSeverity;

public record ValidationAlertDecision(
        String type,
        AlertSeverity severity,
        String message
) {
}
