package org.upc.desktopbffservice.desktop.infrastructure.clients.fleet;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "fleet-service")
public interface FleetClient {

    @GetMapping("/api/v1/routes")
    List<RouteClientResource> getRoutes();

    @GetMapping("/api/v1/routes/{routeId}")
    RouteClientResource getRouteById(@PathVariable Long routeId);

    @GetMapping("/api/v1/drivers")
    List<DriverClientResource> getDrivers();

    @GetMapping("/api/v1/drivers/{driverId}")
    DriverClientResource getDriverById(@PathVariable Long driverId);

    @GetMapping("/api/v1/vehicles")
    List<VehicleClientResource> getVehicles();

    @GetMapping("/api/v1/vehicles/{vehicleId}")
    VehicleClientResource getVehicleById(@PathVariable Long vehicleId);
}
