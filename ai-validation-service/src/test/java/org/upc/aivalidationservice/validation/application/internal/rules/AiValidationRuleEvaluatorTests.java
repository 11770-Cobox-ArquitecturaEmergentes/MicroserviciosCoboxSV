package org.upc.aivalidationservice.validation.application.internal.rules;

import org.junit.jupiter.api.Test;
import org.upc.aivalidationservice.validation.application.internal.providers.AiVisionResult;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AnalysisStatus;
import org.upc.aivalidationservice.validation.infrastructure.clients.edge.EdgeEvidenceClientResource;
import org.upc.aivalidationservice.validation.infrastructure.clients.edge.EdgeTelemetryClientResource;
import org.upc.aivalidationservice.validation.interfaces.messaging.EvidenceUploadConfirmedEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AiValidationRuleEvaluatorTests {

    private final AiValidationRuleEvaluator evaluator = new AiValidationRuleEvaluator();

    @Test
    void lowQualityGeneratesRecaptureRequired() {
        var decision = evaluator.evaluate(event(), result(true, false, false), null, telemetry());

        assertThat(decision.status()).isEqualTo(AnalysisStatus.RECAPTURE_REQUIRED);
        assertThat(decision.alerts()).extracting(ValidationAlertDecision::type)
                .containsExactly("EVIDENCE_RECAPTURE_REQUIRED");
    }

    @Test
    void ambiguousResultGeneratesReviewRequired() {
        var decision = evaluator.evaluate(event(), result(false, true, false), null, telemetry());

        assertThat(decision.status()).isEqualTo(AnalysisStatus.REVIEW_REQUIRED);
        assertThat(decision.alerts()).extracting(ValidationAlertDecision::type)
                .containsExactly("EVIDENCE_REVIEW_REQUIRED");
    }

    @Test
    void missingTelemetryGeneratesDegradedNotFraud() {
        var decision = evaluator.evaluate(event(), result(false, false, false), null, List.of());

        assertThat(decision.status()).isEqualTo(AnalysisStatus.DEGRADED);
        assertThat(decision.fraudScore()).isZero();
    }

    @Test
    void strongMetadataMismatchGeneratesFraudSuspected() {
        var edgeEvidence = new EdgeEvidenceClientResource(
                UUID.randomUUID(),
                999L,
                20L,
                "DELIVERY_PHOTO",
                "different-key",
                "different-sha",
                "image/jpeg",
                100L,
                Instant.now(),
                "RECORDED"
        );

        var decision = evaluator.evaluate(event(), result(false, false, false), edgeEvidence, telemetry());

        assertThat(decision.status()).isEqualTo(AnalysisStatus.FRAUD_SUSPECTED);
        assertThat(decision.alerts()).extracting(ValidationAlertDecision::type)
                .containsExactly("EVIDENCE_CONTEXT_MISMATCH");
    }

    private EvidenceUploadConfirmedEvent event() {
        return new EvidenceUploadConfirmedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                10L,
                100L,
                20L,
                "DELIVERY_PHOTO",
                null,
                null,
                "drivers/10/routes/20/orders/100/evidences/e1",
                "sha",
                "image/jpeg",
                100L,
                Instant.now()
        );
    }

    private AiVisionResult result(boolean lowQuality, boolean ambiguous, boolean illegible) {
        return new AiVisionResult(
                "TEST",
                illegible ? "" : "readable text",
                List.of("Truck:95.0"),
                95.0,
                lowQuality,
                ambiguous,
                illegible
        );
    }

    private List<EdgeTelemetryClientResource> telemetry() {
        return List.of(new EdgeTelemetryClientResource(
                UUID.randomUUID(),
                20L,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.TEN,
                80,
                Instant.now()
        ));
    }
}
