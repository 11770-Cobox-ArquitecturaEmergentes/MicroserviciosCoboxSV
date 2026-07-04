package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceRule;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.MaintenanceRuleResource;

public class MaintenanceRuleResourceFromEntityAssembler {
    public static MaintenanceRuleResource toResourceFromEntity(MaintenanceRule entity) {
        return new MaintenanceRuleResource(entity.name(), entity.thresholdKm(), entity.thresholdDays());
    }
}
