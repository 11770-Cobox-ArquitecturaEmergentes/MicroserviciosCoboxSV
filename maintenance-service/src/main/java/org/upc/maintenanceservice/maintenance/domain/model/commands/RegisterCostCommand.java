package org.upc.maintenanceservice.maintenance.domain.model.commands;

import java.math.BigDecimal;

public record RegisterCostCommand(Long orderId, BigDecimal amount, String currency) {
}
