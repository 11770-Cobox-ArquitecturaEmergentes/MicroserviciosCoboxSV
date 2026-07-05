package org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "maintenance-service")
public interface MaintenanceClient {

    @GetMapping("/api/v1/maintenance-orders/status/{status}")
    List<MaintenanceOrderClientResource> getOrdersByStatus(@PathVariable String status);

    @GetMapping("/api/v1/maintenance-orders/vehicle/{vehicleId}/open")
    List<MaintenanceOrderClientResource> getOpenOrdersByVehicle(@PathVariable Long vehicleId);

    @GetMapping("/api/v1/maintenance-orders/vehicle/{vehicleId}/history")
    List<MaintenanceOrderClientResource> getHistoryByVehicle(@PathVariable Long vehicleId);
}
