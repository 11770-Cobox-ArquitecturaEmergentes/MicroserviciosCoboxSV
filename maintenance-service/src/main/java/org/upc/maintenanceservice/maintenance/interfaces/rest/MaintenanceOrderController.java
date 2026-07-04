package org.upc.maintenanceservice.maintenance.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.upc.maintenanceservice.maintenance.domain.model.queries.*;
import org.upc.maintenanceservice.maintenance.domain.services.MaintenanceOrderCommandService;
import org.upc.maintenanceservice.maintenance.domain.services.MaintenanceOrderQueryService;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.*;
import org.upc.maintenanceservice.maintenance.interfaces.rest.transform.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/maintenance-orders", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Maintenance Orders", description = "Maintenance Order Management Endpoints")
public class MaintenanceOrderController {

    private final MaintenanceOrderCommandService maintenanceOrderCommandService;
    private final MaintenanceOrderQueryService maintenanceOrderQueryService;

    public MaintenanceOrderController(
            MaintenanceOrderCommandService maintenanceOrderCommandService,
            MaintenanceOrderQueryService maintenanceOrderQueryService) {
        this.maintenanceOrderCommandService = maintenanceOrderCommandService;
        this.maintenanceOrderQueryService = maintenanceOrderQueryService;
    }

    @Operation(summary = "Create a maintenance order",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Order created successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceOrderResource.class)))
            })
    @PostMapping
    public ResponseEntity<MaintenanceOrderResource> createOrder(@Valid @RequestBody CreateMaintenanceOrderResource resource) {
        var orderId = maintenanceOrderCommandService.handle(CreateMaintenanceOrderCommandFromResourceAssembler.toCommandFromResource(resource));
        var order = maintenanceOrderQueryService.handle(new GetMaintenanceOrderByIdQuery(orderId));
        if (order.isEmpty()) return ResponseEntity.badRequest().build();
        return new ResponseEntity<>(MaintenanceOrderResourceFromEntityAssembler.toResourceFromEntity(order.get()), HttpStatus.CREATED);
    }

    @Operation(summary = "Schedule a maintenance order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceOrderResource.class))),
                    @ApiResponse(responseCode = "404", description = "Order not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PostMapping("/{orderId}/schedule")
    public ResponseEntity<MaintenanceOrderResource> scheduleOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody ScheduleMaintenanceOrderResource resource) {
        maintenanceOrderCommandService.handle(ScheduleMaintenanceOrderCommandFromResourceAssembler.toCommandFromResource(orderId, resource));
        var order = maintenanceOrderQueryService.handle(new GetMaintenanceOrderByIdQuery(orderId));
        if (order.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceOrderResourceFromEntityAssembler.toResourceFromEntity(order.get()));
    }

    @Operation(summary = "Start a maintenance order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceOrderResource.class)))
            })
    @PostMapping("/{orderId}/start")
    public ResponseEntity<MaintenanceOrderResource> startOrder(@PathVariable Long orderId, @RequestBody(required = false) StartMaintenanceOrderResource resource) {
        maintenanceOrderCommandService.handle(StartMaintenanceOrderCommandFromResourceAssembler.toCommandFromResource(orderId));
        var order = maintenanceOrderQueryService.handle(new GetMaintenanceOrderByIdQuery(orderId));
        if (order.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceOrderResourceFromEntityAssembler.toResourceFromEntity(order.get()));
    }

    @Operation(summary = "Complete a maintenance order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceOrderResource.class)))
            })
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<MaintenanceOrderResource> completeOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CompleteMaintenanceOrderResource resource) {
        maintenanceOrderCommandService.handle(CompleteMaintenanceOrderCommandFromResourceAssembler.toCommandFromResource(orderId, resource));
        var order = maintenanceOrderQueryService.handle(new GetMaintenanceOrderByIdQuery(orderId));
        if (order.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceOrderResourceFromEntityAssembler.toResourceFromEntity(order.get()));
    }

    @Operation(summary = "Cancel a maintenance order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceOrderResource.class)))
            })
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<MaintenanceOrderResource> cancelOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody CancelMaintenanceOrderResource resource) {
        maintenanceOrderCommandService.handle(CancelMaintenanceOrderCommandFromResourceAssembler.toCommandFromResource(orderId, resource));
        var order = maintenanceOrderQueryService.handle(new GetMaintenanceOrderByIdQuery(orderId));
        if (order.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceOrderResourceFromEntityAssembler.toResourceFromEntity(order.get()));
    }

    @Operation(summary = "Register a job for a maintenance order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceOrderResource.class)))
            })
    @PostMapping("/{orderId}/jobs")
    public ResponseEntity<MaintenanceOrderResource> registerJob(
            @PathVariable Long orderId,
            @Valid @RequestBody RegisterJobResource resource) {
        maintenanceOrderCommandService.handle(RegisterJobCommandFromResourceAssembler.toCommandFromResource(orderId, resource));
        var order = maintenanceOrderQueryService.handle(new GetMaintenanceOrderByIdQuery(orderId));
        if (order.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceOrderResourceFromEntityAssembler.toResourceFromEntity(order.get()));
    }

    @Operation(summary = "Request parts for a maintenance order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceOrderResource.class)))
            })
    @PostMapping("/{orderId}/parts/request")
    public ResponseEntity<MaintenanceOrderResource> requestParts(
            @PathVariable Long orderId,
            @Valid @RequestBody RequestPartsResource resource) {
        maintenanceOrderCommandService.handle(RequestPartsCommandFromResourceAssembler.toCommandFromResource(orderId, resource));
        var order = maintenanceOrderQueryService.handle(new GetMaintenanceOrderByIdQuery(orderId));
        if (order.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceOrderResourceFromEntityAssembler.toResourceFromEntity(order.get()));
    }

    @Operation(summary = "Receive parts for a maintenance order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceOrderResource.class)))
            })
    @PostMapping("/{orderId}/parts/receive")
    public ResponseEntity<MaintenanceOrderResource> receiveParts(
            @PathVariable Long orderId,
            @Valid @RequestBody ReceivePartsResource resource) {
        maintenanceOrderCommandService.handle(ReceivePartsCommandFromResourceAssembler.toCommandFromResource(orderId, resource));
        var order = maintenanceOrderQueryService.handle(new GetMaintenanceOrderByIdQuery(orderId));
        if (order.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceOrderResourceFromEntityAssembler.toResourceFromEntity(order.get()));
    }

    @Operation(summary = "Register a maintenance cost",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceOrderResource.class)))
            })
    @PostMapping("/{orderId}/cost")
    public ResponseEntity<MaintenanceOrderResource> registerCost(
            @PathVariable Long orderId,
            @Valid @RequestBody RegisterCostResource resource) {
        maintenanceOrderCommandService.handle(RegisterCostCommandFromResourceAssembler.toCommandFromResource(orderId, resource));
        var order = maintenanceOrderQueryService.handle(new GetMaintenanceOrderByIdQuery(orderId));
        if (order.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceOrderResourceFromEntityAssembler.toResourceFromEntity(order.get()));
    }

    @Operation(summary = "Get a maintenance order by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Order found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceOrderResource.class)))
            })
    @GetMapping("/{orderId}")
    public ResponseEntity<MaintenanceOrderResource> getOrderById(@PathVariable Long orderId) {
        var order = maintenanceOrderQueryService.handle(new GetMaintenanceOrderByIdQuery(orderId));
        if (order.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceOrderResourceFromEntityAssembler.toResourceFromEntity(order.get()));
    }

    @Operation(summary = "Get maintenance orders by status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orders retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = MaintenanceOrderResource.class))))
            })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<MaintenanceOrderResource>> getOrdersByStatus(@PathVariable String status) {
        var orders = maintenanceOrderQueryService.handle(new GetMaintenanceOrdersByStatusQuery(org.upc.maintenanceservice.maintenance.domain.model.valueobjects.MaintenanceOrderStatus.valueOf(status)));
        return ResponseEntity.ok(orders.stream().map(MaintenanceOrderResourceFromEntityAssembler::toResourceFromEntity).toList());
    }

    @Operation(summary = "Get open maintenance orders by vehicle",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orders retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = MaintenanceOrderResource.class))))
            })
    @GetMapping("/vehicle/{vehicleId}/open")
    public ResponseEntity<List<MaintenanceOrderResource>> getOpenOrdersByVehicle(@PathVariable Long vehicleId) {
        var orders = maintenanceOrderQueryService.handle(new GetOpenMaintenanceOrdersByVehicleIdQuery(vehicleId));
        return ResponseEntity.ok(orders.stream().map(MaintenanceOrderResourceFromEntityAssembler::toResourceFromEntity).toList());
    }

    @Operation(summary = "Check if vehicle has open maintenance order",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Boolean result",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/vehicle/{vehicleId}/has-open")
    public ResponseEntity<Boolean> hasOpenOrder(@PathVariable Long vehicleId) {
        return ResponseEntity.ok(maintenanceOrderQueryService.handle(new HasMaintenanceOpenOrderForVehicleIdQuery(vehicleId)));
    }

    @Operation(summary = "Get maintenance order history for a vehicle",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Orders retrieved",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = MaintenanceOrderResource.class))))
            })
    @GetMapping("/vehicle/{vehicleId}/history")
    public ResponseEntity<List<MaintenanceOrderResource>> getHistory(@PathVariable Long vehicleId) {
        var orders = maintenanceOrderQueryService.handle(new GetMaintenanceOrderHistoryQuery(vehicleId));
        return ResponseEntity.ok(orders.stream().map(MaintenanceOrderResourceFromEntityAssembler::toResourceFromEntity).toList());
    }
}
