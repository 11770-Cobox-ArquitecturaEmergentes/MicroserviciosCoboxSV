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
import org.upc.fleetservice.fleet.domain.model.queries.GetAllDriversQuery;
import org.upc.fleetservice.fleet.domain.model.queries.GetDriverByEmailQuery;
import org.upc.fleetservice.fleet.domain.model.queries.GetDriverByIdQuery;
import org.upc.fleetservice.fleet.domain.services.DriverCommandService;
import org.upc.fleetservice.fleet.domain.services.DriverQueryService;
import org.upc.fleetservice.fleet.interfaces.rest.resources.CreateDriverResource;
import org.upc.fleetservice.fleet.interfaces.rest.resources.DriverResource;
import org.upc.fleetservice.fleet.interfaces.rest.transform.CreateDriverCommandFromResourceAssembler;
import org.upc.fleetservice.fleet.interfaces.rest.transform.DriverResourceFromEntityAssembler;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/drivers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Drivers", description = "Drivers Management Endpoints") // Corrected to plural for consistency
public class DriverController {
    private final DriverCommandService driverCommandService;
    private final DriverQueryService driverQueryService;

    public DriverController(DriverCommandService driverCommandService, DriverQueryService driverQueryService) {
        this.driverCommandService = driverCommandService;
        this.driverQueryService = driverQueryService;
    }

    @Operation(summary = "Create a new driver",
            description = "Creates a new driver with personal details like name and license number, and returns the created driver's information.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Driver created successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DriverResource.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data provided",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PostMapping
    public ResponseEntity<DriverResource> createDriver(@Valid @RequestBody CreateDriverResource resource) {
        var createDriverCommand = CreateDriverCommandFromResourceAssembler.toCommandFromResource(resource);
        var driverId = driverCommandService.handle(createDriverCommand);
        if (driverId == 0L) {
            return ResponseEntity.badRequest().build();
        }
        var getDriverByIdQuery = new GetDriverByIdQuery(driverId);
        var driver = driverQueryService.handle(getDriverByIdQuery);
        if (driver.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var driverResource = DriverResourceFromEntityAssembler.toResourceFromEntity(driver.get());
        return new ResponseEntity<>(driverResource, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a driver by their ID",
            description = "Retrieves the details of a single driver by their unique identifier.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Driver found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = DriverResource.class))),
                    @ApiResponse(responseCode = "404", description = "Driver not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/{driverId}")
    public ResponseEntity<DriverResource> getDriverById(@PathVariable Long driverId) {
        var getDriverByIdQuery = new GetDriverByIdQuery(driverId);
        var driver = driverQueryService.handle(getDriverByIdQuery);
        if (driver.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var driverResource = DriverResourceFromEntityAssembler.toResourceFromEntity(driver.get());
        return ResponseEntity.ok(driverResource);
    }

    @Operation(summary = "Get all drivers",
            description = "Retrieves a list of all existing drivers.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of drivers retrieved successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = DriverResource.class))))
            })
    @GetMapping
    public ResponseEntity<List<DriverResource>> getAllDrivers() {
        var getAllDriversQuery = new GetAllDriversQuery();
        var drivers = driverQueryService.handle(getAllDriversQuery);
        var driverResources = drivers.stream()
                .map(DriverResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(driverResources);
    }

        @Operation(summary = "Get a driver by email",
                        description = "Retrieves the details of a driver using their email address.",
                        responses = {
                                        @ApiResponse(responseCode = "200", description = "Driver found",
                                                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                                                        schema = @Schema(implementation = DriverResource.class))),
                                        @ApiResponse(responseCode = "404", description = "Driver not found",
                                                        content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
                        })
        @GetMapping("/search")
        public ResponseEntity<DriverResource> getDriverByEmail(@RequestParam String email) {
                var driver = driverQueryService.handle(new GetDriverByEmailQuery(email));
                if (driver.isEmpty()) {
                        return ResponseEntity.notFound().build();
                }
                return ResponseEntity.ok(DriverResourceFromEntityAssembler.toResourceFromEntity(driver.get()));
        }
}