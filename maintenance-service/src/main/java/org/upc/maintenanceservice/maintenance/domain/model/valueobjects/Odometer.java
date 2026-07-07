package org.upc.maintenanceservice.maintenance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record Odometer(Long odometer) {
    public Odometer() {
        this(null);
    }
}
