package org.upc.aivalidationservice.validation.application.internal.rules;

import org.springframework.stereotype.Service;
import org.upc.aivalidationservice.validation.application.internal.providers.AiVisionResult;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AlertSeverity;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AnalysisStatus;
import org.upc.aivalidationservice.validation.infrastructure.clients.edge.EdgeEvidenceClientResource;
import org.upc.aivalidationservice.validation.infrastructure.clients.edge.EdgeTelemetryClientResource;
import org.upc.aivalidationservice.validation.interfaces.messaging.EvidenceUploadConfirmedEvent;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiValidationRuleEvaluator {

    public ValidationDecision evaluate(EvidenceUploadConfirmedEvent event,
                                       AiVisionResult visionResult,
                                       EdgeEvidenceClientResource edgeEvidence,
                                       List<EdgeTelemetryClientResource> telemetry) {
        var alerts = new ArrayList<ValidationAlertDecision>();

        if (hasStrongInconsistency(event, edgeEvidence)) {
            alerts.add(new ValidationAlertDecision(
                    "EVIDENCE_CONTEXT_MISMATCH",
                    AlertSeverity.HIGH,
                    "Evidence metadata conflicts with offline edge metadata"
            ));
            return new ValidationDecision(
                    AnalysisStatus.FRAUD_SUSPECTED,
                    0.95,
                    "Strong inconsistency between upload intent and edge evidence metadata",
                    alerts
            );
        }

        if (visionResult.lowQuality() || visionResult.illegible()) {
            alerts.add(new ValidationAlertDecision(
                    "EVIDENCE_RECAPTURE_REQUIRED",
                    AlertSeverity.MEDIUM,
                    "Evidence appears blurry, low-light or illegible"
            ));
            return new ValidationDecision(
                    AnalysisStatus.RECAPTURE_REQUIRED,
                    0.10,
                    "Evidence quality is insufficient for automated validation",
                    alerts
            );
        }

        if (visionResult.ambiguous()) {
            alerts.add(new ValidationAlertDecision(
                    "EVIDENCE_REVIEW_REQUIRED",
                    AlertSeverity.MEDIUM,
                    "AI result is ambiguous and needs manual review"
            ));
            return new ValidationDecision(
                    AnalysisStatus.REVIEW_REQUIRED,
                    0.20,
                    "AI result is ambiguous",
                    alerts
            );
        }

        if (telemetry == null || telemetry.isEmpty()) {
            alerts.add(new ValidationAlertDecision(
                    "TELEMETRY_MISSING",
                    AlertSeverity.LOW,
                    "Route telemetry is unavailable; evidence is degraded but not fraudulent by default"
            ));
            return new ValidationDecision(
                    AnalysisStatus.DEGRADED,
                    0.0,
                    "Telemetry is absent, so validation is degraded",
                    alerts
            );
        }

        return new ValidationDecision(
                AnalysisStatus.COMPLETED,
                0.0,
                "Evidence validated with AI result and route telemetry context",
                List.of()
        );
    }

    private boolean hasStrongInconsistency(EvidenceUploadConfirmedEvent event, EdgeEvidenceClientResource edgeEvidence) {
        if (edgeEvidence == null) {
            return false;
        }
        return mismatches(event.orderId(), edgeEvidence.orderId())
                || mismatches(event.routeId(), edgeEvidence.routeId())
                || mismatches(event.objectKey(), edgeEvidence.objectKey())
                || mismatches(event.sha256(), edgeEvidence.sha256());
    }

    private boolean mismatches(Long left, Long right) {
        return left != null && right != null && !left.equals(right);
    }

    private boolean mismatches(String left, String right) {
        return left != null && right != null && !left.equals(right);
    }
}
