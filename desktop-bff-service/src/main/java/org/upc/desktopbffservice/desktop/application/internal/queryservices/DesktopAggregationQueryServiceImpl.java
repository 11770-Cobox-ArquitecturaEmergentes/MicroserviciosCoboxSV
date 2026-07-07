package org.upc.desktopbffservice.desktop.application.internal.queryservices;

import feign.FeignException;
import org.springframework.stereotype.Service;
import org.upc.desktopbffservice.desktop.domain.exceptions.DesktopResourceNotFoundException;
import org.upc.desktopbffservice.desktop.infrastructure.clients.aivalidation.AiAlertClientResource;
import org.upc.desktopbffservice.desktop.infrastructure.clients.aivalidation.AiValidationClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.aivalidation.EvidenceAnalysisClientResource;
import org.upc.desktopbffservice.desktop.infrastructure.clients.delivery.DeliveryClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.delivery.OrderClientResource;
import org.upc.desktopbffservice.desktop.infrastructure.clients.fleet.*;
import org.upc.desktopbffservice.desktop.infrastructure.clients.incident.IncidentClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.incident.IncidentClientResource;
import org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance.MaintenanceClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance.MaintenanceOrderClientResource;
import org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance.MaintenanceScheduleClientResource;
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
    private final AiValidationClient aiValidationClient;

    public DesktopAggregationQueryServiceImpl(FleetClient fleetClient,
                                              DeliveryClient deliveryClient,
                                              IncidentClient incidentClient,
                                              MaintenanceClient maintenanceClient,
                                              AiValidationClient aiValidationClient) {
        this.fleetClient = fleetClient;
        this.deliveryClient = deliveryClient;
        this.incidentClient = incidentClient;
        this.maintenanceClient = maintenanceClient;
        this.aiValidationClient = aiValidationClient;
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
        var schedule = optionalSection("maintenance.schedule", degraded,
                () -> toMaintenanceScheduleSummary(maintenanceClient.getScheduleByVehicle(vehicleId)));
        return new VehicleHealthResource(
                Instant.now(),
                toVehicleSummary(vehicle),
                openOrders == null ? List.of() : openOrders,
                history == null ? List.of() : history,
                schedule,
                degraded
        );
    }

    @Override
    public List<SmartVisionAlertOverviewResource> getSmartVisionAlerts(String status) {
        var alerts = status == null || status.isBlank()
                ? aiValidationClient.getAlerts()
                : aiValidationClient.getAlertsByStatus(status);
        return safeList(alerts).stream()
                .map(this::toSmartVisionAlertOverview)
                .toList();
    }

    @Override
    public List<SmartVisionAnalysisOverviewResource> getSmartVisionAnalyses(String status, Long driverId, Long routeId, Long orderId) {
        var analyses = safeList(aiValidationClient.getAnalyses(status, driverId, routeId, orderId));
        List<AiAlertClientResource> alerts;
        String alertDegradation = null;
        try {
            alerts = safeList(aiValidationClient.getAlerts());
        } catch (Exception ex) {
            alerts = List.of();
            alertDegradation = reason(ex);
        }
        var alertsByEvidence = alerts.stream()
                .filter(alert -> alert.clientEvidenceId() != null)
                .collect(Collectors.groupingBy(AiAlertClientResource::clientEvidenceId));
        var finalAlertDegradation = alertDegradation;
        return analyses.stream()
                .map(analysis -> toSmartVisionAnalysisOverview(
                        analysis,
                        safeList(alertsByEvidence.get(analysis.clientEvidenceId())),
                        finalAlertDegradation))
                .toList();
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

    private MaintenanceScheduleSummaryResource toMaintenanceScheduleSummary(MaintenanceScheduleClientResource schedule) {
        if (schedule == null) {
            return null;
        }
        return new MaintenanceScheduleSummaryResource(
                schedule.id(),
                schedule.vehicleId(),
                schedule.status(),
                safeList(schedule.rules()).stream()
                        .map(rule -> new MaintenanceRuleSummaryResource(rule.name(), rule.thresholdKm(), rule.thresholdDays()))
                        .toList(),
                schedule.lastEvaluationAt(),
                schedule.nextEvaluationAt()
        );
    }

    private SmartVisionAlertOverviewResource toSmartVisionAlertOverview(AiAlertClientResource alert) {
        var degraded = new ArrayList<DegradedSectionResource>();
        var analysis = optionalSection("smartvision.analysis", degraded,
                () -> alert.clientEvidenceId() == null ? null : aiValidationClient.getAnalysis(alert.clientEvidenceId()));
        var route = analysis == null || analysis.routeId() == null
                ? null
                : optionalSection("route", degraded, () -> toRouteSummary(fleetClient.getRouteById(analysis.routeId())));
        var driver = analysis == null || analysis.driverId() == null
                ? null
                : optionalSection("driver", degraded, () -> toDriverSummary(fleetClient.getDriverById(analysis.driverId())));
        var vehicle = route == null || route.vehicleId() == null
                ? null
                : optionalSection("vehicle", degraded, () -> toVehicleSummary(fleetClient.getVehicleById(route.vehicleId())));
        var order = analysis == null || analysis.orderId() == null
                ? null
                : optionalSection("order", degraded, () -> toOrderSummary(deliveryClient.getOrderById(analysis.orderId())));
        return new SmartVisionAlertOverviewResource(
                toSmartVisionAlert(alert),
                toSmartVisionAnalysis(analysis),
                driver,
                route,
                vehicle,
                order,
                degraded
        );
    }

    private SmartVisionAnalysisOverviewResource toSmartVisionAnalysisOverview(
            EvidenceAnalysisClientResource analysis,
            List<AiAlertClientResource> alerts,
            String alertDegradation) {
        var degraded = new ArrayList<DegradedSectionResource>();
        if (alertDegradation != null) {
            degraded.add(new DegradedSectionResource("smartvision.alerts", alertDegradation));
        }
        var previewUrl = previewUrl(analysis, degraded);
        var route = analysis.routeId() == null
                ? null
                : optionalSection("route", degraded, () -> toRouteSummary(fleetClient.getRouteById(analysis.routeId())));
        var driver = analysis.driverId() == null
                ? null
                : optionalSection("driver", degraded, () -> toDriverSummary(fleetClient.getDriverById(analysis.driverId())));
        var vehicle = route == null || route.vehicleId() == null
                ? null
                : optionalSection("vehicle", degraded, () -> toVehicleSummary(fleetClient.getVehicleById(route.vehicleId())));
        var order = analysis.orderId() == null
                ? null
                : optionalSection("order", degraded, () -> toOrderSummary(deliveryClient.getOrderById(analysis.orderId())));
        return new SmartVisionAnalysisOverviewResource(
                toSmartVisionAnalysis(analysis, previewUrl),
                alerts.stream().map(this::toSmartVisionAlert).toList(),
                driver,
                route,
                vehicle,
                order,
                degraded
        );
    }

    private String previewUrl(EvidenceAnalysisClientResource analysis, List<DegradedSectionResource> degraded) {
        if (analysis == null || analysis.clientEvidenceId() == null) {
            return null;
        }
        try {
            var preview = aiValidationClient.getAnalysisPreviewUrl(analysis.clientEvidenceId());
            return preview != null ? preview.previewUrl() : null;
        } catch (Exception ex) {
            degraded.add(new DegradedSectionResource("smartvision.preview", reason(ex)));
            return analysis.previewUrl();
        }
    }

    private SmartVisionAlertResource toSmartVisionAlert(AiAlertClientResource alert) {
        if (alert == null) {
            return null;
        }
        return new SmartVisionAlertResource(
                alert.alertId(),
                alert.clientEvidenceId(),
                alert.type(),
                alert.severity(),
                alert.status(),
                alert.message(),
                alert.createdAt(),
                alert.acknowledgedAt(),
                alert.resolvedAt(),
                alert.resolutionNotes(),
                alert.linkedIncidentId()
        );
    }

    private SmartVisionAnalysisResource toSmartVisionAnalysis(EvidenceAnalysisClientResource analysis) {
        return toSmartVisionAnalysis(analysis, analysis != null ? analysis.previewUrl() : null);
    }

    private SmartVisionAnalysisResource toSmartVisionAnalysis(EvidenceAnalysisClientResource analysis, String previewUrl) {
        if (analysis == null) {
            return null;
        }
        return new SmartVisionAnalysisResource(
                analysis.clientEvidenceId(),
                analysis.objectKey(),
                analysis.driverId(),
                analysis.orderId(),
                analysis.routeId(),
                analysis.evidenceType(),
                analysis.sourceType(),
                analysis.sourceId(),
                analysis.status(),
                analysis.provider(),
                analysis.confidenceScore(),
                analysis.fraudScore(),
                analysis.validationSummary(),
                analysis.failureReason(),
                analysis.reviewStatus(),
                analysis.reviewNotes(),
                analysis.reviewedAt(),
                previewUrl,
                analysis.createdAt(),
                analysis.completedAt()
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
