package org.upc.aivalidationservice.validation.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.upc.aivalidationservice.validation.application.internal.queryservices.AiValidationQueryService;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AlertStatus;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.AiAlertResource;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.EvidenceAnalysisResource;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai-validation")
public class AiValidationController {

    private final AiValidationQueryService queryService;

    public AiValidationController(AiValidationQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/evidence-analyses/{clientEvidenceId}")
    public ResponseEntity<EvidenceAnalysisResource> getAnalysis(@PathVariable UUID clientEvidenceId) {
        return ResponseEntity.ok(queryService.getAnalysis(clientEvidenceId));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AiAlertResource>> getAlerts() {
        return ResponseEntity.ok(queryService.getAlerts());
    }

    @GetMapping("/alerts/status/{status}")
    public ResponseEntity<List<AiAlertResource>> getAlertsByStatus(@PathVariable AlertStatus status) {
        return ResponseEntity.ok(queryService.getAlertsByStatus(status));
    }
}
