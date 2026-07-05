package org.upc.desktopbffservice.desktop.application.internal.queryservices;

import feign.FeignException;
import org.springframework.stereotype.Service;
import org.upc.desktopbffservice.desktop.domain.exceptions.DesktopResourceNotFoundException;
import org.upc.desktopbffservice.desktop.infrastructure.clients.delivery.DeliveryClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.delivery.OrderClientResource;
import org.upc.desktopbffservice.desktop.infrastructure.clients.fleet.*;
import org.upc.desktopbffservice.desktop.infrastructure.clients.incident.IncidentClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.incident.IncidentClientResource;
import org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance.MaintenanceClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance.MaintenanceOrderClientResource;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.*;

import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DesktopAggregationQueryServiceImpl implements DesktopAggregationQueryService {

    private static final List<String> OPEN_MAINTENANCE_STATUSES = List.of("OPEN", "SCHEDULED", "IN_PROGRESS");
    private static final Set<String> OPEN_INCIDENT_STATUSES = Set.of("OPEN", "IN_PROGRESS", "ESCALATED");

    private final FleetClient fleetClient;
    private final DeliveryClient deliveryClient;
    private final IncidentClient incidentClient;
    private final MaintenanceClient maintenanceClient;

    public DesktopAggregationQueryServiceImpl(FleetClient fleetClient,
                                              DeliveryClient deliveryClient,
                                              IncidentClient incidentClient,
                                              MaintenanceClient maintenanceClient) {
        this.fleetClient = fleetClient;
        this.deliveryClient = deliveryClient;
        this.incidentClient = incidentClient;
        this.maintenanceClient = maintenanceClient;
    }

    @Override
    public OperationsDashboardResource getOperationsDashboard() {
        var degraded = new ArrayList<DegradedSectionResource>();
        var fleet = dashboardFleet(degraded);
        var deliveries = dashboardDeliveries(degraded);
        var incidents = dashboardIncidents(degraded);
        var maintenance = dashboardMaintenance(degraded);
        return new OperationsDashboardResource(Instant.now(), fleet, deliveries, incidents, maintenance, degraded);
    }

    @Override
    public RouteOverviewResource getRouteOverview(Long routeId) {
        var route = getRequiredRoute(routeId);
        var degraded = new ArrayList<DegradedSectionResource>();
        var driver = optionalSection("driver", degraded, () -> route.driverId() == null ? null : toDriverSummary(fleetClient.getDriverById(route.driverId())));
        var vehicle = optionalSection("vehicle", degraded, () -> route.vehicleId() == null ? null : toVehicleSummary(fleetClient.getVehicleById(route.vehicleId())));
        var orders = orderIds(route).stream()
                .map(orderId -> optionalSection("orders", degraded, () -> toOrderSummary(deliveryClient.getOrderById(orderId))))
                .filter(Objects::nonNull)
                .toList();
        return new RouteOverviewResource(
                Instant.now(),
                toRouteSummary(route),
                driver,
                vehicle,
                orders,
                finishedOrderIds(route),
                degraded
        );
    }

    @Override
    public VehicleHealthResource getVehicleHealth(Long vehicleId) {
        var vehicle = getRequiredVehicle(vehicleId);
        var degraded = new ArrayList<DegradedSectionResource>();
        var openOrders = optionalSection("maintenance.openOrders", degraded,
                () -> safeList(maintenanceClient.getOpenOrdersByVehicle(vehicleId)).stream().map(this::toMaintenanceSummary).toList());
        var history = optionalSection("maintenance.history", degraded,
                () -> safeList(maintenanceClient.getHistoryByVehicle(vehicleId)).stream().map(this::toMaintenanceSummary).toList());
        degraded.add(new DegradedSectionResource("maintenance.schedules", "Maintenance schedules by vehicle are not exposed by maintenance-service yet"));
        return new VehicleHealthResource(
                Instant.now(),
                toVehicleSummary(vehicle),
                openOrders == null ? List.of() : openOrders,
                history == null ? List.of() : history,
                degraded
        );
    }

    private FleetDashboardResource dashboardFleet(List<DegradedSectionResource> degraded) {
        return optionalSection("fleet", degraded, () -> {
            var vehicles = safeList(fleetClient.getVehicles());
            var drivers = safeList(fleetClient.getDrivers());
            var routes = safeList(fleetClient.getRoutes());
            var activeRoutes = routes.stream()
                    .filter(route -> "IN_PROGRESS".equals(route.routeStatus()))
                    .map(this::toRouteSummary)
                    .toList();
            return new FleetDashboardResource(
                    vehicles.size(),
                    drivers.size(),
                    routes.size(),
                    countBy(vehicles, VehicleClientResource::vehicleStatus),
                    countBy(drivers, DriverClientResource::driverStatus),
                    countBy(routes, RouteClientResource::routeStatus),
                    activeRoutes
            );
        }, new FleetDashboardResource(0, 0, 0, Map.of(), Map.of(), Map.of(), List.of()));
    }

    private DeliveriesDashboardResource dashboardDeliveries(List<DegradedSectionResource> degraded) {
        return optionalSection("deliveries", degraded, () -> {
            var orders = safeList(deliveryClient.getOrders());
            return new DeliveriesDashboardResource(
                    orders.size(),
                    countBy(orders, OrderClientResource::orderStatus),
                    orders.stream().map(this::toOrderSummary).limit(10).toList()
            );
        }, new DeliveriesDashboardResource(0, Map.of(), List.of()));
    }

    private IncidentsDashboardResource dashboardIncidents(List<DegradedSectionResource> degraded) {
        return optionalSection("incidents", degraded, () -> {
            var incidents = safeList(incidentClient.getIncidents());
            return new IncidentsDashboardResource(
                    incidents.size(),
                    countBy(incidents, IncidentClientResource::status),
                    countBy(incidents, IncidentClientResource::severity),
                    incidents.stream()
                            .filter(incident -> OPEN_INCIDENT_STATUSES.contains(incident.status()))
                            .map(this::toIncidentSummary)
                            .limit(10)
                            .toList()
            );
        }, new IncidentsDashboardResource(0, Map.of(), Map.of(), List.of()));
    }

    private MaintenanceDashboardResource dashboardMaintenance(List<DegradedSectionResource> degraded) {
        return optionalSection("maintenance", degraded, () -> {
            var orders = OPEN_MAINTENANCE_STATUSES.stream()
                    .flatMap(status -> safeList(maintenanceClient.getOrdersByStatus(status)).stream())
                    .toList();
            return new MaintenanceDashboardResource(
                    orders.size(),
                    countBy(orders, MaintenanceOrderClientResource::status),
                    orders.stream().map(this::toMaintenanceSummary).limit(10).toList()
            );
        }, new MaintenanceDashboardResource(0, Map.of(), List.of()));
    }

    private RouteClientResource getRequiredRoute(Long routeId) {
        try {
            return fleetClient.getRouteById(routeId);
        } catch (FeignException.NotFound ex) {
            throw new DesktopResourceNotFoundException("Route not found: " + routeId);
        }
    }

    private VehicleClientResource getRequiredVehicle(Long vehicleId) {
        try {
            return fleetClient.getVehicleById(vehicleId);
        } catch (FeignException.NotFound ex) {
            throw new DesktopResourceNotFoundException("Vehicle not found: " + vehicleId);
        }
    }

    private <T> T optionalSection(String section, List<DegradedSectionResource> degraded, SupplierWithException<T> supplier) {
        return optionalSection(section, degraded, supplier, null);
    }

    private <T> T optionalSection(String section, List<DegradedSectionResource> degraded, SupplierWithException<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (Exception ex) {
            degraded.add(new DegradedSectionResource(section, reason(ex)));
            return fallback;
        }
    }

    private String reason(Exception ex) {
        if (ex instanceof FeignException feignException) {
            return "Dependency returned HTTP " + feignException.status();
        }
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }

    private RouteSummaryResource toRouteSummary(RouteClientResource route) {
        return new RouteSummaryResource(
                route.id(),
                route.title(),
                route.vehicleId(),
                route.driverId(),
                orderIds(route),
                finishedOrderIds(route),
                route.routeStatus()
        );
    }

    private DriverSummaryResource toDriverSummary(DriverClientResource driver) {
        return new DriverSummaryResource(driver.id(), driver.email(), driver.licenceNumber(), driver.driverStatus());
    }

    private VehicleSummaryResource toVehicleSummary(VehicleClientResource vehicle) {
        return new VehicleSummaryResource(vehicle.id(), vehicle.plateNumber(), vehicle.capacityKg(), vehicle.vehicleStatus());
    }

    private OrderSummaryResource toOrderSummary(OrderClientResource order) {
        return new OrderSummaryResource(order.id(), order.clientId(), order.city(), order.country(), order.weightKg(), order.orderStatus());
    }

    private IncidentSummaryResource toIncidentSummary(IncidentClientResource incident) {
        return new IncidentSummaryResource(
                incident.id(),
                incident.incidentId(),
                incident.type(),
                incident.severity(),
                incident.status(),
                incident.reportedAt(),
                incident.responsibleUserId()
        );
    }

    private MaintenanceOrderSummaryResource toMaintenanceSummary(MaintenanceOrderClientResource order) {
        return new MaintenanceOrderSummaryResource(
                order.id(),
                order.vehicleId(),
                order.maintenanceType(),
                order.priority(),
                order.status(),
                order.openingOdometer(),
                order.closingOdometer(),
                order.totalCostAmount(),
                order.totalCostCurrency(),
                order.technicianId()
        );
    }

    private List<Long> orderIds(RouteClientResource route) {
        return safeList(route.ordersIds()).stream()
                .map(OrderIdClientResource::orderId)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<Long> finishedOrderIds(RouteClientResource route) {
        return safeSet(route.finishedOrderIds()).stream()
                .map(OrderIdClientResource::orderId)
                .filter(Objects::nonNull)
                .toList();
    }

    private <T> Map<String, Long> countBy(List<T> values, Function<T, String> classifier) {
        return values.stream()
                .map(classifier)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new, Collectors.counting()));
    }

    private <T> List<T> safeList(List<T> value) {
        return value == null ? List.of() : value;
    }

    private <T> Set<T> safeSet(Set<T> value) {
        return value == null ? Set.of() : value;
    }

    @FunctionalInterface
    private interface SupplierWithException<T> {
        T get();
    }
}
