package org.upc.maintenanceservice.maintenance.interfaces.rest.resources;

import java.time.LocalDateTime;
import java.util.List;

public record MaintenanceScheduleResource(
        Long id,
        Long vehicleId,
        String status,
        List<MaintenanceRuleResource> rules,
        LocalDateTime lastEvaluationAt,
        LocalDateTime nextEvaluationAt
) {
}
