package org.upc.maintenanceservice.maintenance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record ChecklistItem(String name, Boolean completed) {
    public ChecklistItem() {
        this(null, null);
    }
}
