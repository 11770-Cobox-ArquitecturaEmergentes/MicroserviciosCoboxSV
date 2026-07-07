package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.List;

public record MaintenanceScheduleSummaryResource(
        Long id,
        Long vehicleId,
        String status,
        List<MaintenanceRuleSummaryResource> rules,
        LocalDateTime lastEvaluationAt,
        LocalDateTime nextEvaluationAt
) {
}
