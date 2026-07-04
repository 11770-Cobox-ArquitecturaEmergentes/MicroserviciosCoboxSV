package org.upc.maintenanceservice.maintenance.domain.model.events;

public record ThresholdReachedEvent(Long scheduleId, Long vehicleId) {
}
