package org.upc.aivalidationservice.validation.domain.exceptions;

import java.util.UUID;

public class EvidenceAnalysisNotFoundException extends RuntimeException {
    public EvidenceAnalysisNotFoundException(UUID clientEvidenceId) {
        super("Evidence analysis not found for clientEvidenceId " + clientEvidenceId);
    }
}
