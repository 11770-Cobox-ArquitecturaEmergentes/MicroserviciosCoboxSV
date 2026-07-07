package org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance;

import java.math.BigDecimal;
import java.util.List;

public record MaintenanceOrderClientResource(
        Long id,
        Long vehicleId,
        String maintenanceType,
        String priority,
        String status,
        String reason,
        Long openingOdometer,
        Long closingOdometer,
        Integer scheduledTimelapseDays,
        List<JobClientResource> jobs,
        List<PartsRequestClientResource> partsRequests,
        BigDecimal totalCostAmount,
        String totalCostCurrency,
        Long technicianId
) {
}
