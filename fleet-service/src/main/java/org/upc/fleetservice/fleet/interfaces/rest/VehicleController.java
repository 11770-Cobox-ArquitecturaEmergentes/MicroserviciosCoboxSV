package org.upc.fleetservice.fleet.interfaces.rest;

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
import org.upc.fleetservice.fleet.domain.model.queries.GetAllVehiclesQuery;
import org.upc.fleetservice.fleet.domain.model.queries.GetVehicleByIdQuery;
import org.upc.fleetservice.fleet.domain.services.VehicleCommandService;
import org.upc.fleetservice.fleet.domain.services.VehicleQueryService;
import org.upc.fleetservice.fleet.interfaces.rest.resources.CreateVehicleResource;
import org.upc.fleetservice.fleet.interfaces.rest.resources.UpdateVehicleStatusResource;
import org.upc.fleetservice.fleet.interfaces.rest.resources.VehicleResource;
import org.upc.fleetservice.fleet.interfaces.rest.transform.CreateVehicleCommandFromResourceAssembler;
import org.upc.fleetservice.fleet.interfaces.rest.transform.UpdateVehicleStatusCommandFromResourceAssembler;
import org.upc.fleetservice.fleet.interfaces.rest.transform.VehicleResourceFromEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Vehicles", description = "Vehicles Management Endpoints") // Corregido a plural para consistencia
public class VehicleController {

    private final VehicleCommandService vehicleCommandService;
    private final VehicleQueryService vehicleQueryService;

    public VehicleController(VehicleCommandService vehicleCommandService, VehicleQueryService vehicleQueryService) {
        this.vehicleCommandService = vehicleCommandService;
        this.vehicleQueryService = vehicleQueryService;
    }

    @Operation(summary = "Create a new vehicle",
            description = "Creates a new vehicle with the provided license plate, brand, and capacity, and returns the created vehicle's details.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Vehicle created successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = VehicleResource.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PostMapping
    public ResponseEntity<VehicleResource> createVehicle(@Valid @RequestBody CreateVehicleResource resource) {
        var createVehicleCommand = CreateVehicleCommandFromResourceAssembler.toCommandFromResource(resource);
        var vehicleId = vehicleCommandService.handle(createVehicleCommand);
        if (vehicleId == 0L) {
            return ResponseEntity.badRequest().build();
        }
        var getVehicleByIdQuery = new GetVehicleByIdQuery(vehicleId);
        var vehicle = vehicleQueryService.handle(getVehicleByIdQuery);

        if (vehicle.isEmpty()) return ResponseEntity.badRequest().build();

        var vehicleResource = VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle.get());
        return new ResponseEntity<>(vehicleResource, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a vehicle by its ID",
            description = "Retrieves the details of a single vehicle by its unique identifier.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Vehicle found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = VehicleResource.class))),
                    @ApiResponse(responseCode = "404", description = "Vehicle not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleResource> getVehicleById(@PathVariable Long vehicleId) {
        var getVehicleByIdQuery = new GetVehicleByIdQuery(vehicleId);
        var vehicle = vehicleQueryService.handle(getVehicleByIdQuery);
        if (vehicle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var vehicleResource = VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle.get());
        return ResponseEntity.ok(vehicleResource);
    }

    @Operation(summary = "Get all vehicles",
            description = "Retrieves a list of all existing vehicles.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of vehicles retrieved successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = VehicleResource.class))))
            })
    @GetMapping
    public ResponseEntity<List<VehicleResource>> getAllVehicles() {
        var getAllVehiclesQuery = new GetAllVehiclesQuery();
        var vehicles = vehicleQueryService.handle(getAllVehiclesQuery);
        var vehicleResources = vehicles.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(vehicleResources);
    }

    @Operation(summary = "Update a vehicle's status",
            description = "Updates the status of a vehicle. Allowed target statuses: OPERATIONAL, IN_MAINTENANCE, OUT_OF_SERVICE.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Vehicle status updated successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = VehicleResource.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "404", description = "Vehicle not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
                    @ApiResponse(responseCode = "409", description = "Invalid status transition for the current vehicle state",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @RequestMapping(value = "/{vehicleId}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<VehicleResource> updateVehicleStatus(@PathVariable Long vehicleId,
                                                               @Valid @RequestBody UpdateVehicleStatusResource resource) {
        var updateVehicleStatusCommand = UpdateVehicleStatusCommandFromResourceAssembler.toCommandFromResource(vehicleId, resource);
        var updatedVehicleId = vehicleCommandService.handle(updateVehicleStatusCommand);
        if (updatedVehicleId == 0L) {
            return ResponseEntity.badRequest().build();
        }
        var vehicle = vehicleQueryService.handle(new GetVehicleByIdQuery(updatedVehicleId));
        if (vehicle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var vehicleResource = VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle.get());
        return ResponseEntity.ok(vehicleResource);
    }
}