package org.upc.aivalidationservice.validation.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.upc.aivalidationservice.validation.domain.exceptions.AiAlertNotFoundException;
import org.upc.aivalidationservice.validation.domain.exceptions.EvidenceAnalysisNotFoundException;
import org.upc.aivalidationservice.validation.domain.model.aggregates.EvidenceAnalysis;
import org.upc.aivalidationservice.validation.domain.model.entities.AiAlert;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AlertStatus;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AnalysisStatus;
import org.upc.aivalidationservice.validation.infrastructure.persistence.jpa.repositories.AiAlertRepository;
import org.upc.aivalidationservice.validation.infrastructure.persistence.jpa.repositories.EvidenceAnalysisRepository;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.AiAlertResource;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.EvidenceAnalysisResource;

import java.util.List;
import java.util.UUID;

@Service
public class AiValidationQueryServiceImpl implements AiValidationQueryService {

    private final EvidenceAnalysisRepository evidenceAnalysisRepository;
    private final AiAlertRepository aiAlertRepository;

    public AiValidationQueryServiceImpl(EvidenceAnalysisRepository evidenceAnalysisRepository,
                                        AiAlertRepository aiAlertRepository) {
        this.evidenceAnalysisRepository = evidenceAnalysisRepository;
        this.aiAlertRepository = aiAlertRepository;
    }

    @Override
    public EvidenceAnalysisResource getAnalysis(UUID clientEvidenceId) {
        return evidenceAnalysisRepository.findByClientEvidenceId(clientEvidenceId)
                .map(this::toResource)
                .orElseThrow(() -> new EvidenceAnalysisNotFoundException(clientEvidenceId));
    }

    @Override
    public List<EvidenceAnalysisResource> getAnalyses(AnalysisStatus status, Long driverId, Long routeId, Long orderId) {
        return evidenceAnalysisRepository.findAll().stream()
                .filter(analysis -> status == null || analysis.getStatus() == status)
                .filter(analysis -> driverId == null || driverId.equals(analysis.getDriverId()))
                .filter(analysis -> routeId == null || routeId.equals(analysis.getRouteId()))
                .filter(analysis -> orderId == null || orderId.equals(analysis.getOrderId()))
                .map(this::toResource)
                .toList();
    }

    @Override
    public AiAlertResource getAlert(UUID alertId) {
        return aiAlertRepository.findByAlertId(alertId)
                .map(this::toResource)
                .orElseThrow(() -> new AiAlertNotFoundException(alertId));
    }

    @Override
    public List<AiAlertResource> getAlerts() {
        return aiAlertRepository.findAll().stream().map(this::toResource).toList();
    }

    @Override
    public List<AiAlertResource> getAlertsByStatus(AlertStatus status) {
        return aiAlertRepository.findByStatusOrderByCreatedAtDesc(status).stream().map(this::toResource).toList();
    }

    private EvidenceAnalysisResource toResource(EvidenceAnalysis analysis) {
        return new EvidenceAnalysisResource(
                analysis.getClientEvidenceId(),
                analysis.getObjectKey(),
                analysis.getDriverId(),
                analysis.getOrderId(),
                analysis.getRouteId(),
                analysis.getEvidenceType(),
                analysis.getSourceType(),
                analysis.getSourceId(),
                analysis.getStatus(),
                analysis.getProvider(),
                analysis.getConfidenceScore(),
                analysis.getFraudScore(),
                analysis.getValidationSummary(),
                analysis.getFailureReason(),
                analysis.getReviewStatus() != null ? analysis.getReviewStatus().name() : null,
                analysis.getReviewNotes(),
                analysis.getReviewedAt(),
                null,
                analysis.getCreatedAt(),
                analysis.getCompletedAt()
        );
    }

    private AiAlertResource toResource(AiAlert alert) {
        return new AiAlertResource(
                alert.getAlertId(),
                alert.getClientEvidenceId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getMessage(),
                alert.getCreatedAt(),
                alert.getAcknowledgedAt(),
                alert.getResolvedAt(),
                alert.getResolutionNotes(),
                alert.getLinkedIncidentId()
        );
    }
}
