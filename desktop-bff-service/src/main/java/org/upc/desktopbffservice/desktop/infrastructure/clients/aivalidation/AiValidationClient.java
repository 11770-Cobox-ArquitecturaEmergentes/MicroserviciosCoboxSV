package org.upc.desktopbffservice.desktop.infrastructure.clients.aivalidation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "ai-validation-service")
public interface AiValidationClient {

    @GetMapping("/api/v1/ai-validation/alerts")
    List<AiAlertClientResource> getAlerts();

    @GetMapping("/api/v1/ai-validation/alerts/status/{status}")
    List<AiAlertClientResource> getAlertsByStatus(@PathVariable String status);

    @GetMapping("/api/v1/ai-validation/evidence-analyses/{clientEvidenceId}")
    EvidenceAnalysisClientResource getAnalysis(@PathVariable UUID clientEvidenceId);

    @GetMapping("/api/v1/ai-validation/evidence-analyses")
    List<EvidenceAnalysisClientResource> getAnalyses(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Long orderId);
}
