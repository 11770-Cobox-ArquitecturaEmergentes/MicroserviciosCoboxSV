package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.util.List;
import java.util.Map;

public record IncidentsDashboardResource(
        long totalIncidents,
        Map<String, Long> incidentsByStatus,
        Map<String, Long> incidentsBySeverity,
        List<IncidentSummaryResource> openIncidents
) {
}
