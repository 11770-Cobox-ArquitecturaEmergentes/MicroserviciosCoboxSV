package org.upc.aivalidationservice.validation.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.upc.aivalidationservice.validation.application.internal.commandservices.AiValidationCommandService;
import org.upc.aivalidationservice.validation.application.internal.queryservices.AiValidationQueryService;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AlertStatus;
import org.upc.aivalidationservice.validation.domain.model.valueobjects.AnalysisStatus;
import org.upc.aivalidationservice.validation.interfaces.rest.resources.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ai-validation")
public class AiValidationController {

    private final AiValidationQueryService queryService;
    private final AiValidationCommandService commandService;

    public AiValidationController(AiValidationQueryService queryService, AiValidationCommandService commandService) {
        this.queryService = queryService;
        this.commandService = commandService;
    }

    @GetMapping("/evidence-analyses")
    public ResponseEntity<List<EvidenceAnalysisResource>> getAnalyses(
            @RequestParam(required = false) AnalysisStatus status,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Long orderId) {
        return ResponseEntity.ok(queryService.getAnalyses(status, driverId, routeId, orderId));
    }

    @GetMapping("/evidence-analyses/{clientEvidenceId}")
    public ResponseEntity<EvidenceAnalysisResource> getAnalysis(@PathVariable UUID clientEvidenceId) {
        return ResponseEntity.ok(queryService.getAnalysis(clientEvidenceId));
    }

    @GetMapping("/evidence-analyses/{clientEvidenceId}/preview-url")
    public ResponseEntity<EvidencePreviewUrlResource> getAnalysisPreviewUrl(@PathVariable UUID clientEvidenceId) {
        return ResponseEntity.ok(commandService.createPreviewUrl(clientEvidenceId));
    }

    @PatchMapping("/evidence-analyses/{clientEvidenceId}/review")
    public ResponseEntity<EvidenceAnalysisResource> reviewAnalysis(
            @PathVariable UUID clientEvidenceId,
            @RequestBody ReviewEvidenceAnalysisResource resource) {
        return ResponseEntity.ok(commandService.reviewAnalysis(clientEvidenceId, resource));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<AiAlertResource>> getAlerts() {
        return ResponseEntity.ok(queryService.getAlerts());
    }

    @GetMapping("/alerts/status/{status}")
    public ResponseEntity<List<AiAlertResource>> getAlertsByStatus(@PathVariable AlertStatus status) {
        return ResponseEntity.ok(queryService.getAlertsByStatus(status));
    }

    @GetMapping("/alerts/{alertId}")
    public ResponseEntity<AiAlertResource> getAlert(@PathVariable UUID alertId) {
        return ResponseEntity.ok(queryService.getAlert(alertId));
    }

    @PatchMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<AiAlertResource> acknowledgeAlert(@PathVariable UUID alertId) {
        return ResponseEntity.ok(commandService.acknowledgeAlert(alertId));
    }

    @PatchMapping("/alerts/{alertId}/resolve")
    public ResponseEntity<AiAlertResource> resolveAlert(
            @PathVariable UUID alertId,
            @RequestBody(required = false) ResolveAlertResource resource) {
        return ResponseEntity.ok(commandService.resolveAlert(alertId, resource == null ? null : resource.notes()));
    }

    @PostMapping("/alerts/{alertId}/incident")
    public ResponseEntity<IncidentFromAlertResource> createIncidentFromAlert(
            @PathVariable UUID alertId,
            @RequestBody(required = false) CreateIncidentFromAlertResource resource) {
        return ResponseEntity.ok(commandService.createIncidentFromAlert(alertId, resource));
    }
}
