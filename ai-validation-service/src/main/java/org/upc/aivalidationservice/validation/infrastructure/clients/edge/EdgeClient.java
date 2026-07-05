package org.upc.aivalidationservice.validation.infrastructure.clients.edge;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "edge-service")
public interface EdgeClient {

    @GetMapping("/api/v1/edge/evidences/{clientEvidenceId}")
    EdgeEvidenceClientResource getEvidence(@PathVariable UUID clientEvidenceId);

    @GetMapping("/api/v1/edge/routes/{routeId}/telemetry")
    List<EdgeTelemetryClientResource> getTelemetryByRoute(@PathVariable Long routeId);
}
