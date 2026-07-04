package org.upc.maintenanceservice.maintenance.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record MaintenanceRule(
        @Column(name = "rule_name") String name,
        @Column(name = "threshold_km") Long thresholdKm,
        @Column(name = "threshold_days") Integer thresholdDays
) {
    public MaintenanceRule() {
        this(null, null, null);
    }
}
