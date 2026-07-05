package org.upc.aivalidationservice.validation.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AnalysisStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_evidence_analysis_client_evidence_id", columnNames = "client_evidence_id"))
public class EvidenceAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_evidence_id", nullable = false, updatable = false)
    private UUID clientEvidenceId;

    @Column(nullable = false, length = 1000)
    private String objectKey;

    private Long driverId;
    private Long orderId;
    private Long routeId;
    private String evidenceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnalysisStatus status;

    @Column(length = 20000)
    private String ocrText;

    @Column(length = 20000)
    private String labelsJson;

    private String provider;
    private Double confidenceScore;
    private Double fraudScore;

    @Column(length = 2000)
    private String validationSummary;

    @Column(length = 2000)
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant completedAt;

    public EvidenceAnalysis(UUID clientEvidenceId, String objectKey, Long driverId, Long orderId, Long routeId, String evidenceType) {
        this.clientEvidenceId = clientEvidenceId;
        this.objectKey = objectKey;
        this.driverId = driverId;
        this.orderId = orderId;
        this.routeId = routeId;
        this.evidenceType = evidenceType;
        this.status = AnalysisStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void markProcessing() {
        this.status = AnalysisStatus.PROCESSING;
        this.failureReason = null;
    }

    public void complete(AnalysisStatus status, String provider, String ocrText, String labelsJson,
                         Double confidenceScore, Double fraudScore, String validationSummary) {
        this.status = status;
        this.provider = provider;
        this.ocrText = ocrText;
        this.labelsJson = labelsJson;
        this.confidenceScore = confidenceScore;
        this.fraudScore = fraudScore;
        this.validationSummary = truncate(validationSummary);
        this.failureReason = null;
        this.completedAt = Instant.now();
    }

    public void fail(String failureReason) {
        this.status = AnalysisStatus.FAILED;
        this.failureReason = truncate(failureReason);
        this.completedAt = Instant.now();
    }

    public boolean isTerminal() {
        return switch (this.status) {
            case COMPLETED, REVIEW_REQUIRED, RECAPTURE_REQUIRED, FRAUD_SUSPECTED, DEGRADED -> true;
            default -> false;
        };
    }

    private String truncate(String value) {
        if (value == null) return null;
        return value.length() <= 2000 ? value : value.substring(0, 2000);
    }
}
