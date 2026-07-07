package org.upc.aivalidationservice.validation.application.internal.commandservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.upc.aivalidationservice.validation.application.internal.providers.AiVisionProvider;
import org.upc.aivalidationservice.validation.application.internal.providers.AiVisionRequest;
import org.upc.aivalidationservice.validation.application.internal.rules.AiValidationRuleEvaluator;
import org.upc.aivalidationservice.validation.domain.exceptions.AiAlertNotFoundException;
import org.upc.aivalidationservice.validation.domain.exceptions.EvidenceAnalysisNotFoundException;
import org.upc.aivalidationservice.validation.domain.exceptions.InvalidAiAlertTransitionException;
import org.upc.aivalidationservice.validation.domain.model.aggregates.EvidenceAnalysis;
import org.upc.aivalidationservice.validation.domain.model.entities.AiAlert;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.EvidenceReviewStatus;
import org.upc.aivalidationservice.validation.infrastructure.clients.incident.CreateIncidentClientResource;
import org.upc.aivalidationservice.validation.infrastructure.clients.incident.IncidentClient;
import org.upc.aivalidationservice.validation.infrastructure.clients.incident.IncidentClientResource;
import org.upc.aivalidationservice.validation.infrastructure.clients.edge.EdgeClient;
import org.upc.aivalidationservice.validation.infrastructure.clients.edge.EdgeEvidenceClientResource;
import org.upc.aivalidationservice.validation.infrastructure.clients.edge.EdgeTelemetryClientResource;
import org.upc.aivalidationservice.validation.infrastructure.persistence.jpa.repositories.AiAlertRepository;
import org.upc.aivalidationservice.validation.infrastructure.persistence.jpa.repositories.EvidenceAnalysisRepository;
import org.upc.aivalidationservice.validation.infrastructure.storage.EvidencePreviewStorageService;
import org.upc.aivalidationservice.validation.infrastructure.storage.StorageProperties;
import org.upc.aivalidationservice.validation.interfaces.messaging.EvidenceUploadConfirmedEvent;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.AiAlertResource;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.CreateIncidentFromAlertResource;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.EvidenceAnalysisResource;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.EvidencePreviewUrlResource;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.IncidentFromAlertResource;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.ReviewEvidenceAnalysisResource;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
public class AiValidationCommandServiceImpl implements AiValidationCommandService {

    private final EvidenceAnalysisRepository evidenceAnalysisRepository;
    private final AiAlertRepository aiAlertRepository;
    private final AiVisionProvider aiVisionProvider;
    private final AiValidationRuleEvaluator ruleEvaluator;
    private final EdgeClient edgeClient;
    private final IncidentClient incidentClient;
    private final StorageProperties storageProperties;
    private final EvidencePreviewStorageService previewStorageService;
    private final ObjectMapper objectMapper;
    private final RabbitTemplate rabbitTemplate;

    public AiValidationCommandServiceImpl(EvidenceAnalysisRepository evidenceAnalysisRepository,
                                          AiAlertRepository aiAlertRepository,
                                          AiVisionProvider aiVisionProvider,
                                          AiValidationRuleEvaluator ruleEvaluator,
                                          EdgeClient edgeClient,
                                          IncidentClient incidentClient,
                                          StorageProperties storageProperties,
                                          EvidencePreviewStorageService previewStorageService,
                                          ObjectMapper objectMapper,
                                          RabbitTemplate rabbitTemplate) {
        this.evidenceAnalysisRepository = evidenceAnalysisRepository;
        this.aiAlertRepository = aiAlertRepository;
        this.aiVisionProvider = aiVisionProvider;
        this.ruleEvaluator = ruleEvaluator;
        this.edgeClient = edgeClient;
        this.incidentClient = incidentClient;
        this.storageProperties = storageProperties;
        this.previewStorageService = previewStorageService;
        this.objectMapper = objectMapper;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    @Transactional(noRollbackFor = RuntimeException.class)
    public void handleEvidenceUploadConfirmed(EvidenceUploadConfirmedEvent event) {
        var analysis = evidenceAnalysisRepository.findByClientEvidenceId(event.clientEvidenceId())
                .orElseGet(() -> evidenceAnalysisRepository.save(new EvidenceAnalysis(
                        event.clientEvidenceId(),
                        event.objectKey(),
                        event.driverId(),
                        event.orderId(),
                        event.routeId(),
                        event.type(),
                        event.sourceType(),
                        event.sourceId()
                )));

        if (analysis.isTerminal()) {
            return;
        }

        try {
            analysis.markProcessing();
            evidenceAnalysisRepository.save(analysis);

            var edgeEvidence = getEdgeEvidence(event);
            var telemetry = getTelemetry(event);
            var visionResult = aiVisionProvider.analyze(new AiVisionRequest(
                    storageProperties.bucket(),
                    event.objectKey(),
                    event.mimeType()
            ));
            var decision = ruleEvaluator.evaluate(event, visionResult, edgeEvidence, telemetry);

            analysis.complete(
                    decision.status(),
                    visionResult.provider(),
                    visionResult.ocrText(),
                    labelsJson(visionResult.labels()),
                    visionResult.confidenceScore(),
                    decision.fraudScore(),
                    decision.summary()
            );
            evidenceAnalysisRepository.save(analysis);
            publishAnalysisEvent("ai-validation.analysis-completed", analysis);
            var openedAlerts = new ArrayList<AiAlert>();
            decision.alerts().forEach(alert -> aiAlertRepository
                    .findByClientEvidenceIdAndType(event.clientEvidenceId(), alert.type())
                    .orElseGet(() -> {
                        var savedAlert = aiAlertRepository.save(new AiAlert(
                                event.clientEvidenceId(),
                                alert.type(),
                                alert.severity(),
                                alert.message()
                        ));
                        openedAlerts.add(savedAlert);
                        return savedAlert;
                    }));
            openedAlerts.forEach(alert -> publishAlertEvent("ai-validation.alert-opened", alert));
        } catch (RuntimeException ex) {
            analysis.fail(ex.getMessage());
            evidenceAnalysisRepository.save(analysis);
            publishAnalysisEvent("ai-validation.analysis-failed", analysis);
            throw ex;
        }
    }

    @Override
    @Transactional
    public AiAlertResource acknowledgeAlert(UUID alertId) {
        var alert = getAlertEntity(alertId);
        try {
            alert.acknowledge();
        } catch (IllegalStateException ex) {
            throw new InvalidAiAlertTransitionException(ex.getMessage());
        }
        var saved = aiAlertRepository.save(alert);
        publishAlertEvent("ai-validation.alert-acknowledged", saved);
        return toResource(saved);
    }

    @Override
    @Transactional
    public AiAlertResource resolveAlert(UUID alertId, String notes) {
        var alert = getAlertEntity(alertId);
        alert.resolve(notes);
        var saved = aiAlertRepository.save(alert);
        publishAlertEvent("ai-validation.alert-resolved", saved);
        return toResource(saved);
    }

    @Override
    @Transactional
    public IncidentFromAlertResource createIncidentFromAlert(UUID alertId, CreateIncidentFromAlertResource resource) {
        var alert = getAlertEntity(alertId);
        if (alert.getStatus() != null && "RESOLVED".equals(alert.getStatus().name())) {
            throw new InvalidAiAlertTransitionException("Resolved alerts cannot be linked to new incidents");
        }
        if (alert.getLinkedIncidentId() != null) {
            return new IncidentFromAlertResource(alert.getAlertId(), alert.getClientEvidenceId(), alert.getLinkedIncidentId(), false);
        }

        IncidentClientResource incident;
        boolean created = false;
        try {
            incident = incidentClient.getIncidentByAiAlertId(alertId);
        } catch (FeignException.NotFound ex) {
            incident = incidentClient.createIncident(toIncidentRequest(alert, resource));
            created = true;
        }

        alert.linkIncident(incident.incidentId());
        aiAlertRepository.save(alert);
        publishAlertEvent("ai-validation.alert-incident-linked", alert);
        return new IncidentFromAlertResource(alert.getAlertId(), alert.getClientEvidenceId(), incident.incidentId(), created);
    }

    @Override
    @Transactional
    public EvidenceAnalysisResource reviewAnalysis(UUID clientEvidenceId, ReviewEvidenceAnalysisResource resource) {
        var analysis = getAnalysisEntity(clientEvidenceId);
        var reviewStatus = parseReviewStatus(resource);
        analysis.review(reviewStatus, resource != null ? resource.notes() : null);
        var saved = evidenceAnalysisRepository.save(analysis);
        publishAnalysisEvent("ai-validation.analysis-reviewed", saved);
        return toEvidenceAnalysisResource(saved, null);
    }

    @Override
    public EvidencePreviewUrlResource createPreviewUrl(UUID clientEvidenceId) {
        var analysis = getAnalysisEntity(clientEvidenceId);
        return previewStorageService.createPreviewUrl(analysis.getObjectKey());
    }

    private EdgeEvidenceClientResource getEdgeEvidence(EvidenceUploadConfirmedEvent event) {
        try {
            return edgeClient.getEvidence(event.clientEvidenceId());
        } catch (FeignException.NotFound ex) {
            return null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private List<EdgeTelemetryClientResource> getTelemetry(EvidenceUploadConfirmedEvent event) {
        if (event.routeId() == null) {
            return List.of();
        }
        try {
            return edgeClient.getTelemetryByRoute(event.routeId());
        } catch (RuntimeException ex) {
            return List.of();
        }
    }

    private String labelsJson(List<String> labels) {
        try {
            return objectMapper.writeValueAsString(labels);
        } catch (JsonProcessingException ex) {
            return "[]";
        }
    }

    private AiAlert getAlertEntity(UUID alertId) {
        return aiAlertRepository.findByAlertId(alertId)
                .orElseThrow(() -> new AiAlertNotFoundException(alertId));
    }

    private EvidenceAnalysis getAnalysisEntity(UUID clientEvidenceId) {
        return evidenceAnalysisRepository.findByClientEvidenceId(clientEvidenceId)
                .orElseThrow(() -> new EvidenceAnalysisNotFoundException(clientEvidenceId));
    }

    private EvidenceReviewStatus parseReviewStatus(ReviewEvidenceAnalysisResource resource) {
        if (resource == null || resource.reviewStatus() == null || resource.reviewStatus().isBlank()) {
            throw new IllegalArgumentException("reviewStatus is required");
        }
        try {
            return EvidenceReviewStatus.valueOf(resource.reviewStatus().trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("reviewStatus must be ACCEPTED or REJECTED");
        }
    }

    private CreateIncidentClientResource toIncidentRequest(AiAlert alert, CreateIncidentFromAlertResource resource) {
        var analysis = evidenceAnalysisRepository.findByClientEvidenceId(alert.getClientEvidenceId())
                .orElse(null);
        var type = resource != null && resource.type() != null && !resource.type().isBlank()
                ? resource.type()
                : "SMARTVISION_" + alert.getType();
        var description = resource != null && resource.description() != null && !resource.description().isBlank()
                ? resource.description()
                : defaultIncidentDescription(alert, analysis);
        var severity = resource != null && resource.severity() != null && !resource.severity().isBlank()
                ? resource.severity()
                : alert.getSeverity().name();
        var responsibleUserId = resource != null ? resource.responsibleUserId() : null;
        return new CreateIncidentClientResource(
                type,
                description,
                severity,
                responsibleUserId,
                "AI_ALERT",
                alert.getAlertId(),
                alert.getClientEvidenceId()
        );
    }

    private String defaultIncidentDescription(AiAlert alert, EvidenceAnalysis analysis) {
        var description = new StringBuilder(alert.getMessage())
                .append("\n\nSmartVision alertId: ").append(alert.getAlertId())
                .append("\nclientEvidenceId: ").append(alert.getClientEvidenceId());
        if (analysis != null) {
            description.append("\nanalysisStatus: ").append(analysis.getStatus());
            if (analysis.getDriverId() != null) description.append("\ndriverId: ").append(analysis.getDriverId());
            if (analysis.getRouteId() != null) description.append("\nrouteId: ").append(analysis.getRouteId());
            if (analysis.getOrderId() != null) description.append("\norderId: ").append(analysis.getOrderId());
            if (analysis.getValidationSummary() != null) description.append("\nsummary: ").append(analysis.getValidationSummary());
        }
        var value = description.toString();
        return value.length() <= 2000 ? value : value.substring(0, 2000);
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

    private EvidenceAnalysisResource toEvidenceAnalysisResource(EvidenceAnalysis analysis, String previewUrl) {
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
                previewUrl,
                analysis.getCreatedAt(),
                analysis.getCompletedAt()
        );
    }

    private void publishAlertEvent(String routingKey, AiAlert alert) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("alertId", alert.getAlertId());
        payload.put("clientEvidenceId", alert.getClientEvidenceId());
        payload.put("type", alert.getType());
        payload.put("severity", alert.getSeverity() != null ? alert.getSeverity().name() : null);
        payload.put("status", alert.getStatus() != null ? alert.getStatus().name() : null);
        payload.put("message", alert.getMessage());
        payload.put("createdAt", alert.getCreatedAt());
        payload.put("acknowledgedAt", alert.getAcknowledgedAt());
        payload.put("resolvedAt", alert.getResolvedAt());
        payload.put("linkedIncidentId", alert.getLinkedIncidentId());
        publishReportEvent(routingKey, payload);
    }

    private void publishAnalysisEvent(String routingKey, EvidenceAnalysis analysis) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("clientEvidenceId", analysis.getClientEvidenceId());
        payload.put("driverId", analysis.getDriverId());
        payload.put("routeId", analysis.getRouteId());
        payload.put("orderId", analysis.getOrderId());
        payload.put("evidenceType", analysis.getEvidenceType());
        payload.put("sourceType", analysis.getSourceType());
        payload.put("sourceId", analysis.getSourceId());
        payload.put("status", analysis.getStatus() != null ? analysis.getStatus().name() : null);
        payload.put("provider", analysis.getProvider());
        payload.put("confidenceScore", analysis.getConfidenceScore());
        payload.put("fraudScore", analysis.getFraudScore());
        payload.put("reviewStatus", analysis.getReviewStatus() != null ? analysis.getReviewStatus().name() : null);
        payload.put("reviewedAt", analysis.getReviewedAt());
        payload.put("createdAt", analysis.getCreatedAt());
        payload.put("completedAt", analysis.getCompletedAt());
        publishReportEvent(routingKey, payload);
    }

    private void publishReportEvent(String routingKey, LinkedHashMap<String, Object> payload) {
        try {
            rabbitTemplate.convertAndSend("report.exchange", routingKey, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException | RuntimeException ex) {
            // Reporting must not block SmartVision workflows.
        }
    }
}
