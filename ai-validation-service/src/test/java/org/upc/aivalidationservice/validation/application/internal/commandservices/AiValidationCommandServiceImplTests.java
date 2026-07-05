package org.upc.aivalidationservice.validation.application.internal.commandservices;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.upc.aivalidationservice.validation.application.internal.providers.AiVisionProvider;
import org.upc.aivalidationservice.validation.application.internal.providers.AiVisionResult;
import org.upc.aivalidationservice.validation.application.internal.rules.AiValidationRuleEvaluator;
import org.upc.aivalidationservice.validation.infrastructure.clients.edge.EdgeClient;
import org.upc.aivalidationservice.validation.infrastructure.persistence.jpa.repositories.AiAlertRepository;
import org.upc.aivalidationservice.validation.infrastructure.persistence.jpa.repositories.EvidenceAnalysisRepository;
import org.upc.aivalidationservice.validation.infrastructure.storage.StorageProperties;
import org.upc.aivalidationservice.validation.interfaces.messaging.EvidenceUploadConfirmedEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiValidationCommandServiceImplTests {

    @Test
    void duplicateTerminalEventDoesNotAnalyzeAgain() {
        var event = event();
        var analysisRepository = mock(EvidenceAnalysisRepository.class);
        var alertRepository = mock(AiAlertRepository.class);
        var provider = mock(AiVisionProvider.class);
        var edgeClient = mock(EdgeClient.class);
        var service = new AiValidationCommandServiceImpl(
                analysisRepository,
                alertRepository,
                provider,
                new AiValidationRuleEvaluator(),
                edgeClient,
                new StorageProperties("bucket", "us-east-1"),
                new ObjectMapper()
        );
        var existing = new org.upc.aivalidationservice.validation.domain.model.aggregates.EvidenceAnalysis(
                event.clientEvidenceId(),
                event.objectKey(),
                event.driverId(),
                event.orderId(),
                event.routeId(),
                event.type()
        );
        existing.complete(
                org.upc.aivalidationservice.validation.domain.model.valueobjects.AnalysisStatus.COMPLETED,
                "TEST",
                "text",
                "[]",
                90.0,
                0.0,
                "done"
        );

        when(analysisRepository.findByClientEvidenceId(event.clientEvidenceId())).thenReturn(Optional.of(existing));

        service.handleEvidenceUploadConfirmed(event);

        verify(provider, never()).analyze(org.mockito.ArgumentMatchers.any());
        verify(alertRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void providerIsMockableAndUsedForNewAnalysis() {
        var event = event();
        var analysisRepository = mock(EvidenceAnalysisRepository.class);
        var alertRepository = mock(AiAlertRepository.class);
        var provider = mock(AiVisionProvider.class);
        var edgeClient = mock(EdgeClient.class);
        var service = new AiValidationCommandServiceImpl(
                analysisRepository,
                alertRepository,
                provider,
                new AiValidationRuleEvaluator(),
                edgeClient,
                new StorageProperties("bucket", "us-east-1"),
                new ObjectMapper()
        );

        when(analysisRepository.findByClientEvidenceId(event.clientEvidenceId())).thenReturn(Optional.empty());
        when(analysisRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(edgeClient.getTelemetryByRoute(event.routeId())).thenReturn(List.of());
        when(provider.analyze(org.mockito.ArgumentMatchers.any())).thenReturn(new AiVisionResult(
                "TEST",
                "text",
                List.of("Document:98.0"),
                98.0,
                false,
                false,
                false
        ));
        when(alertRepository.findByClientEvidenceIdAndType(event.clientEvidenceId(), "TELEMETRY_MISSING"))
                .thenReturn(Optional.empty());

        service.handleEvidenceUploadConfirmed(event);

        verify(provider).analyze(org.mockito.ArgumentMatchers.any());
        verify(alertRepository).save(org.mockito.ArgumentMatchers.any());
    }

    private EvidenceUploadConfirmedEvent event() {
        return new EvidenceUploadConfirmedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                10L,
                100L,
                20L,
                "DELIVERY_PHOTO",
                "drivers/10/routes/20/orders/100/evidences/e1",
                "sha",
                "image/jpeg",
                100L,
                Instant.now()
        );
    }
}
