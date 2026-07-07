package org.upc.aivalidationservice.validation.application.internal.queryservices;

import org.upc.aivalidationservice.validation.domain.model.valueobjects.AlertStatus;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AnalysisStatus;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.AiAlertResource;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.EvidenceAnalysisResource;

import java.util.List;
import java.util.UUID;

public interface AiValidationQueryService {
    EvidenceAnalysisResource getAnalysis(UUID clientEvidenceId);
    List<EvidenceAnalysisResource> getAnalyses(AnalysisStatus status, Long driverId, Long routeId, Long orderId);
    AiAlertResource getAlert(UUID alertId);
    List<AiAlertResource> getAlerts();
    List<AiAlertResource> getAlertsByStatus(AlertStatus status);
}
