package org.upc.aivalidationservice.validation.application.internal.commandservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.upc.aivalidationservice.validation.application.internal.providers.AiVisionProvider;
import org.upc.aivalidationservice.validation.application.internal.providers.AiVisionRequest;
import org.upc.aivalidationservice.validation.application.internal.rules.AiValidationRuleEvaluator;
import org.upc.aivalidationservice.validation.domain.model.aggregates.EvidenceAnalysis;
import org.upc.aivalidationservice.validation.domain.model.entities.AiAlert;
import org.upc.aivalidationservice.validation.infrastructure.clients.edge.EdgeClient;
import org.upc.aivalidationservice.validation.infrastructure.clients.edge.EdgeEvidenceClientResource;
import org.upc.aivalidationservice.validation.infrastructure.clients.edge.EdgeTelemetryClientResource;
import org.upc.aivalidationservice.validation.infrastructure.persistence.jpa.repositories.AiAlertRepository;
import org.upc.aivalidationservice.validation.infrastructure.persistence.jpa.repositories.EvidenceAnalysisRepository;
import org.upc.aivalidationservice.validation.infrastructure.storage.StorageProperties;
import org.upc.aivalidationservice.validation.interfaces.messaging.EvidenceUploadConfirmedEvent;

import java.util.List;

@Service
public class AiValidationCommandServiceImpl implements AiValidationCommandService {

    private final EvidenceAnalysisRepository evidenceAnalysisRepository;
    private final AiAlertRepository aiAlertRepository;
    private final AiVisionProvider aiVisionProvider;
    private final AiValidationRuleEvaluator ruleEvaluator;
    private final EdgeClient edgeClient;
    private final StorageProperties storageProperties;
    private final ObjectMapper objectMapper;

    public AiValidationCommandServiceImpl(EvidenceAnalysisRepository evidenceAnalysisRepository,
                                          AiAlertRepository aiAlertRepository,
                                          AiVisionProvider aiVisionProvider,
                                          AiValidationRuleEvaluator ruleEvaluator,
                                          EdgeClient edgeClient,
                                          StorageProperties storageProperties,
                                          ObjectMapper objectMapper) {
        this.evidenceAnalysisRepository = evidenceAnalysisRepository;
        this.aiAlertRepository = aiAlertRepository;
        this.aiVisionProvider = aiVisionProvider;
        this.ruleEvaluator = ruleEvaluator;
        this.edgeClient = edgeClient;
        this.storageProperties = storageProperties;
        this.objectMapper = objectMapper;
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
                        event.type()
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
            decision.alerts().forEach(alert -> aiAlertRepository
                    .findByClientEvidenceIdAndType(event.clientEvidenceId(), alert.type())
                    .orElseGet(() -> aiAlertRepository.save(new AiAlert(
                            event.clientEvidenceId(),
                            alert.type(),
                            alert.severity(),
                            alert.message()
                    ))));
        } catch (RuntimeException ex) {
            analysis.fail(ex.getMessage());
            evidenceAnalysisRepository.save(analysis);
            throw ex;
        }
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
}
