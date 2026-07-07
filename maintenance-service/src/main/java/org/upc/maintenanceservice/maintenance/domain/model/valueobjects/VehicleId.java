package org.upc.maintenanceservice.maintenance.domain.model.valueobjects;

import jakarta.persistence.Embeddable;

@Embeddable
public record VehicleId(Long vehicleId) {
    public VehicleId() {
        this(null);
    }
}
