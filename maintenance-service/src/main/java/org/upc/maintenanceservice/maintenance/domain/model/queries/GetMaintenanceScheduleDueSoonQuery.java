package org.upc.maintenanceservice.maintenance.domain.model.queries;

import java.time.LocalDateTime;

public record GetMaintenanceScheduleDueSoonQuery(LocalDateTime dueBefore) {
}
