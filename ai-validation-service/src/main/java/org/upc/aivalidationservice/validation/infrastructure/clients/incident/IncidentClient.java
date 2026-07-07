package org.upc.aivalidationservice.validation.infrastructure.clients.incident;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "incident-service")
public interface IncidentClient {

    @PostMapping("/api/v1/incidents")
    IncidentClientResource createIncident(@RequestBody CreateIncidentClientResource resource);

    @GetMapping("/api/v1/incidents/source/ai-alert/{alertId}")
    IncidentClientResource getIncidentByAiAlertId(@PathVariable UUID alertId);
}
