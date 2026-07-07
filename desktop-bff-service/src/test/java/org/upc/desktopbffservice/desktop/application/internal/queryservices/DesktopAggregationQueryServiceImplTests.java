package org.upc.desktopbffservice.desktop.application.internal.queryservices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.upc.desktopbffservice.desktop.infrastructure.clients.aivalidation.AiValidationClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.delivery.DeliveryClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.delivery.OrderClientResource;
import org.upc.desktopbffservice.desktop.infrastructure.clients.fleet.*;
import org.upc.desktopbffservice.desktop.infrastructure.clients.incident.IncidentClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.incident.IncidentClientResource;
import org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance.MaintenanceClient;
import org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance.MaintenanceOrderClientResource;
import org.upc.desktopbffservice.desktop.infrastructure.clients.maintenance.MaintenanceScheduleClientResource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DesktopAggregationQueryServiceImplTests {

    private FleetClient fleetClient;
    private DeliveryClient deliveryClient;
    private IncidentClient incidentClient;
    private MaintenanceClient maintenanceClient;
    private AiValidationClient aiValidationClient;
    private DesktopAggregationQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        fleetClient = mock(FleetClient.class);
        deliveryClient = mock(DeliveryClient.class);
        incidentClient = mock(IncidentClient.class);
        maintenanceClient = mock(MaintenanceClient.class);
        aiValidationClient = mock(AiValidationClient.class);
        service = new DesktopAggregationQueryServiceImpl(fleetClient, deliveryClient, incidentClient, maintenanceClient, aiValidationClient);
    }

    @Test
    void dashboardComposesAvailableSections() {
        when(fleetClient.getVehicles()).thenReturn(List.of(vehicle()));
        when(fleetClient.getDrivers()).thenReturn(List.of(driver()));
        when(fleetClient.getRoutes()).thenReturn(List.of(route()));
        when(deliveryClient.getOrders()).thenReturn(List.of(order(100L, "DELIVERED")));
        when(incidentClient.getIncidents()).thenReturn(List.of(incident("OPEN", "CRITICAL")));
        when(maintenanceClient.getOrdersByStatus("OPEN")).thenReturn(List.of(maintenanceOrder("OPEN")));
        when(maintenanceClient.getOrdersByStatus("SCHEDULED")).thenReturn(List.of());
        when(maintenanceClient.getOrdersByStatus("IN_PROGRESS")).thenReturn(List.of());

        var dashboard = service.getOperationsDashboard();

        assertThat(dashboard.degradedSections()).isEmpty();
        assertThat(dashboard.fleet().totalRoutes()).isEqualTo(1);
        assertThat(dashboard.deliveries().totalOrders()).isEqualTo(1);
        assertThat(dashboard.incidents().totalIncidents()).isEqualTo(1);
        assertThat(dashboard.maintenance().totalOpenWork()).isEqualTo(1);
    }

    @Test
    void dashboardReturnsPartialResponseWhenDependencyFails() {
        when(fleetClient.getVehicles()).thenReturn(List.of(vehicle()));
        when(fleetClient.getDrivers()).thenReturn(List.of(driver()));
        when(fleetClient.getRoutes()).thenReturn(List.of(route()));
        when(deliveryClient.getOrders()).thenThrow(new RuntimeException("delivery down"));
        when(incidentClient.getIncidents()).thenReturn(List.of());
        when(maintenanceClient.getOrdersByStatus(anyString())).thenReturn(List.of());

        var dashboard = service.getOperationsDashboard();

        assertThat(dashboard.deliveries().totalOrders()).isZero();
        assertThat(dashboard.degradedSections()).anyMatch(section -> section.section().equals("deliveries"));
    }

    @Test
    void routeOverviewResolvesRouteDriverVehicleAndOrders() {
        when(fleetClient.getRouteById(1L)).thenReturn(route());
        when(fleetClient.getDriverById(7L)).thenReturn(driver());
        when(fleetClient.getVehicleById(9L)).thenReturn(vehicle());
        when(deliveryClient.getOrderById(100L)).thenReturn(order(100L, "DELIVERED"));
        when(deliveryClient.getOrderById(101L)).thenReturn(order(101L, "IN_TRANSIT"));

        var overview = service.getRouteOverview(1L);

        assertThat(overview.route().id()).isEqualTo(1L);
        assertThat(overview.driver().id()).isEqualTo(7L);
        assertThat(overview.vehicle().id()).isEqualTo(9L);
        assertThat(overview.orders()).hasSize(2);
        assertThat(overview.finishedOrderIds()).containsExactly(100L);
        assertThat(overview.degradedSections()).isEmpty();
    }

    @Test
    void vehicleHealthResolvesVehicleMaintenanceAndSchedule() {
        when(fleetClient.getVehicleById(9L)).thenReturn(vehicle());
        when(maintenanceClient.getOpenOrdersByVehicle(9L)).thenReturn(List.of(maintenanceOrder("OPEN")));
        when(maintenanceClient.getHistoryByVehicle(9L)).thenReturn(List.of(maintenanceOrder("COMPLETED")));
        when(maintenanceClient.getScheduleByVehicle(9L)).thenReturn(new MaintenanceScheduleClientResource(
                5L, 9L, "ACTIVE", List.of(), null, null
        ));

        var health = service.getVehicleHealth(9L);

        assertThat(health.vehicle().id()).isEqualTo(9L);
        assertThat(health.openMaintenanceOrders()).hasSize(1);
        assertThat(health.maintenanceHistory()).hasSize(1);
        assertThat(health.maintenanceSchedule().id()).isEqualTo(5L);
        assertThat(health.degradedSections()).isEmpty();
    }

    private RouteClientResource route() {
        return new RouteClientResource(
                1L,
                "North Route",
                9L,
                7L,
                List.of(new OrderIdClientResource(100L), new OrderIdClientResource(101L)),
                Set.of(new OrderIdClientResource(100L)),
                "IN_PROGRESS"
        );
    }

    private DriverClientResource driver() {
        return new DriverClientResource(7L, "driver@cobox.test", "A12345678", "ON_ROUTE");
    }

    private VehicleClientResource vehicle() {
        return new VehicleClientResource(9L, "ABC-123", 1200.0, "IN_ROUTE");
    }

    private OrderClientResource order(Long id, String status) {
        return new OrderClientResource(id, 55L, "Av. Lima", "Lima", "PE", "15001", -12.0, -77.0, "notes", 10.5, status);
    }

    private IncidentClientResource incident(String status, String severity) {
        return new IncidentClientResource(1L, UUID.randomUUID(), "DELAY", "Delay on route", LocalDateTime.now(), severity, status, 99L);
    }

    private MaintenanceOrderClientResource maintenanceOrder(String status) {
        return new MaintenanceOrderClientResource(1L, 9L, "PREVENTIVE", "HIGH", status, "SCHEDULED", 1000L, null, 3, List.of(), List.of(), BigDecimal.TEN, "PEN", 77L);
    }
}
