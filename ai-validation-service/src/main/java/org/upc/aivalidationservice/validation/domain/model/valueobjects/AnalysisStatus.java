package org.upc.aivalidationservice.validation.domain.model.valueobjects;

public enum AnalysisStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    REVIEW_REQUIRED,
    RECAPTURE_REQUIRED,
    FRAUD_SUSPECTED,
    DEGRADED
}
