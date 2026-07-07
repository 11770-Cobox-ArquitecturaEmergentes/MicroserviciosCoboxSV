package org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance;

import java.time.LocalDateTime;
import java.util.List;

public record MaintenanceScheduleClientResource(
        Long id,
        Long vehicleId,
        String status,
        List<MaintenanceRuleClientResource> rules,
        LocalDateTime lastEvaluationAt,
        LocalDateTime nextEvaluationAt
) {
}
