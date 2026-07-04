package org.upc.maintenanceservice.maintenance.interfaces.rest.transform;

import org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceRule;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.MaintenanceRuleResource;

import java.util.List;

public class MaintenanceRuleFromResourceAssembler {
    public static MaintenanceRule toEntityFromResource(MaintenanceRuleResource resource) {
        return new MaintenanceRule(resource.name(), resource.thresholdKm(), resource.thresholdDays());
    }

    public static List<MaintenanceRule> toEntitiesFromResources(List<MaintenanceRuleResource> resources) {
        return resources.stream().map(MaintenanceRuleFromResourceAssembler::toEntityFromResource).toList();
    }
}
