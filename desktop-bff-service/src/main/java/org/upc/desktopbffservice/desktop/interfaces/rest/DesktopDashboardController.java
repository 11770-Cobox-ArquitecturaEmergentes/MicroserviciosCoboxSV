package org.upc.desktopbffservice.desktop.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.upc.desktopbffservice.desktop.application.internal.queryservices.DesktopAggregationQueryService;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.OperationsDashboardResource;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.RouteOverviewResource;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.SmartVisionAlertOverviewResource;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.SmartVisionAnalysisOverviewResource;
import org.upc.desktopbffservice.desktop.interfaces.rest.resources.VehicleHealthResource;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1/desktop", produces = APPLICATION_JSON_VALUE)
@Tag(name = "Desktop BFF", description = "Aggregated read-only views for the desktop client")
public class DesktopDashboardController {

    private final DesktopAggregationQueryService desktopAggregationQueryService;

    public DesktopDashboardController(DesktopAggregationQueryService desktopAggregationQueryService) {
        this.desktopAggregationQueryService = desktopAggregationQueryService;
    }

    @Operation(summary = "Get operational dashboard",
            responses = @ApiResponse(responseCode = "200", description = "Dashboard generated",
                    content = @Content(mediaType = APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = OperationsDashboardResource.class))))
    @GetMapping("/dashboard/operations")
    public ResponseEntity<OperationsDashboardResource> getOperationsDashboard() {
        return ResponseEntity.ok(desktopAggregationQueryService.getOperationsDashboard());
    }

    @Operation(summary = "Get route overview",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Route overview generated",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = RouteOverviewResource.class))),
                    @ApiResponse(responseCode = "404", description = "Route not found")
            })
    @GetMapping("/routes/{routeId}/overview")
    public ResponseEntity<RouteOverviewResource> getRouteOverview(@PathVariable Long routeId) {
        return ResponseEntity.ok(desktopAggregationQueryService.getRouteOverview(routeId));
    }

    @Operation(summary = "Get vehicle health",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Vehicle health generated",
                            content = @Content(mediaType = APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = VehicleHealthResource.class))),
                    @ApiResponse(responseCode = "404", description = "Vehicle not found")
            })
    @GetMapping("/vehicles/{vehicleId}/health")
    public ResponseEntity<VehicleHealthResource> getVehicleHealth(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(desktopAggregationQueryService.getVehicleHealth(vehicleId));
    }

    @Operation(summary = "Get enriched SmartVision alerts")
    @GetMapping("/smartvision/alerts")
    public ResponseEntity<List<SmartVisionAlertOverviewResource>> getSmartVisionAlerts(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(desktopAggregationQueryService.getSmartVisionAlerts(status));
    }

    @Operation(summary = "Get enriched SmartVision evidence analyses")
    @GetMapping("/smartvision/evidence-analyses")
    public ResponseEntity<List<SmartVisionAnalysisOverviewResource>> getSmartVisionAnalyses(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Long orderId) {
        return ResponseEntity.ok(desktopAggregationQueryService.getSmartVisionAnalyses(status, driverId, routeId, orderId));
    }
}
