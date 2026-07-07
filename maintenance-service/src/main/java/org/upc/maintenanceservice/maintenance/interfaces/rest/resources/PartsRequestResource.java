package org.upc.maintenanceservice.maintenance.interfaces.rest.resources;

public record PartsRequestResource(Long id, String partName, Integer quantity, String status) {
}
