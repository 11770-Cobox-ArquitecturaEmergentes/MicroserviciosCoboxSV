package org.upc.maintenanceservice.maintenance.interfaces.rest.resources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UpdateMaintenanceRulesResource(@NotEmpty @Valid List<MaintenanceRuleResource> rules) {
}
