package org.upc.desktopbffservice.desktop.interfaces.rest.resources;

import java.math.BigDecimal;

public record MaintenanceOrderSummaryResource(
        Long id,
        Long vehicleId,
        String maintenanceType,
        String priority,
        String status,
        Long openingOdometer,
        Long closingOdometer,
        BigDecimal totalCostAmount,
        String totalCostCurrency,
        Long technicianId
) {
}
