package org.upc.maintenanceservice.maintenance.interfaces.rest.resources;

import java.math.BigDecimal;
import java.util.List;

public record MaintenanceOrderResource(
        Long id,
        Long vehicleId,
        String maintenanceType,
        String priority,
        String status,
        String reason,
        Long openingOdometer,
        Long closingOdometer,
        Integer scheduledTimelapseDays,
        List<JobResource> jobs,
        List<PartsRequestResource> partsRequests,
        BigDecimal totalCostAmount,
        String totalCostCurrency,
        Long technicianId
) {
}
