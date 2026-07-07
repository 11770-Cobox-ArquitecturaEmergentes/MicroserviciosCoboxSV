package org.upc.aivalidationservice.validation.application.internal.rules;

import org.upc.aivalidationservice.validation.domain.model.valueobjects.AnalysisStatus;

import java.util.List;

public record ValidationDecision(
        AnalysisStatus status,
        double fraudScore,
        String summary,
        List<ValidationAlertDecision> alerts
) {
}
