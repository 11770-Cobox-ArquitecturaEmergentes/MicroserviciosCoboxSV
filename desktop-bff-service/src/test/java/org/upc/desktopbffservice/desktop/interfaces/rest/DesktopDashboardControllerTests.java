package org.upc.desktopbffservice.desktop.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.upc.desktopbffservice.desktop.application.internal.queryservices.DesktopAggregationQueryService;
import org.upc.desktopbffservice.desktop.domain.exceptions.DesktopResourceNotFoundException;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DesktopDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
class DesktopDashboardControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DesktopAggregationQueryService desktopAggregationQueryService;

    @Test
    void operationsDashboardReturnsContract() throws Exception {
        when(desktopAggregationQueryService.getOperationsDashboard()).thenReturn(new OperationsDashboardResource(
                Instant.parse("2026-07-05T10:00:00Z"),
                new FleetDashboardResource(1, 1, 1, Map.of(), Map.of(), Map.of(), List.of()),
                new DeliveriesDashboardResource(1, Map.of("DELIVERED", 1L), List.of()),
                new IncidentsDashboardResource(0, Map.of(), Map.of(), List.of()),
                new MaintenanceDashboardResource(0, Map.of(), List.of()),
                List.of()
        ));

        mockMvc.perform(get("/api/v1/desktop/dashboard/operations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fleet.totalVehicles").value(1))
                .andExpect(jsonPath("$.deliveries.totalOrders").value(1));
    }

    @Test
    void routeOverviewReturnsNotFoundForMissingMainRoute() throws Exception {
        when(desktopAggregationQueryService.getRouteOverview(404L))
                .thenThrow(new DesktopResourceNotFoundException("Route not found: 404"));

        mockMvc.perform(get("/api/v1/desktop/routes/{routeId}/overview", 404L))
                .andExpect(status().isNotFound());
    }

    @Test
    void vehicleHealthReturnsContract() throws Exception {
        when(desktopAggregationQueryService.getVehicleHealth(9L)).thenReturn(new VehicleHealthResource(
                Instant.parse("2026-07-05T10:00:00Z"),
                new VehicleSummaryResource(9L, "ABC-123", 1200.0, "IN_ROUTE"),
                List.of(),
                List.of(),
                null,
                List.of(new DegradedSectionResource("maintenance.schedule", "not exposed"))
        ));

        mockMvc.perform(get("/api/v1/desktop/vehicles/{vehicleId}/health", 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicle.id").value(9))
                .andExpect(jsonPath("$.degradedSections[0].section").value("maintenance.schedule"));
    }
}
