package org.upc.desktopbffservice.desktop.infrastructure.clients.incident;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "incident-service")
public interface IncidentClient {

    @GetMapping("/api/v1/incidents")
    List<IncidentClientResource> getIncidents();
}
