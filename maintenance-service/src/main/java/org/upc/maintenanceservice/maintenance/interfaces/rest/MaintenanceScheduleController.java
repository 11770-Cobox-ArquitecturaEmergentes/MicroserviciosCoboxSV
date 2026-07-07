package org.upc.maintenanceservice.maintenance.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.upc.maintenanceservice.maintenance.domain.model.queries.GetMaintenanceScheduleByIdQuery;
import org.upc.maintenanceservice.maintenance.domain.model.queries.GetMaintenanceScheduleByVehicleIdQuery;
import org.upc.maintenanceservice.maintenance.domain.services.MaintenanceScheduleCommandService;
import org.upc.maintenanceservice.maintenance.domain.services.MaintenanceScheduleQueryService;
import org.upc.maintenanceservice.maintenance.interfaces.rest.resources.*;
import org.upc.maintenanceservice.maintenance.interfaces.rest.transform.*;

@RestController
@RequestMapping(value = "/api/v1/maintenance-schedules", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Maintenance Schedules", description = "Maintenance Schedule Management Endpoints")
public class MaintenanceScheduleController {

    private final MaintenanceScheduleCommandService maintenanceScheduleCommandService;
    private final MaintenanceScheduleQueryService maintenanceScheduleQueryService;

    public MaintenanceScheduleController(
            MaintenanceScheduleCommandService maintenanceScheduleCommandService,
            MaintenanceScheduleQueryService maintenanceScheduleQueryService) {
        this.maintenanceScheduleCommandService = maintenanceScheduleCommandService;
        this.maintenanceScheduleQueryService = maintenanceScheduleQueryService;
    }

    @Operation(summary = "Create a maintenance schedule",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Schedule created successfully",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceScheduleResource.class)))
            })
    @PostMapping
    public ResponseEntity<MaintenanceScheduleResource> createSchedule(@Valid @RequestBody CreateMaintenanceScheduleResource resource) {
        var scheduleId = maintenanceScheduleCommandService.handle(CreateMaintenanceScheduleCommandFromResourceAssembler.toCommandFromResource(resource));
        var schedule = maintenanceScheduleQueryService.handle(new GetMaintenanceScheduleByIdQuery(scheduleId));
        if (schedule.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<>(MaintenanceScheduleResourceFromEntityAssembler.toResourceFromEntity(schedule.get()), HttpStatus.CREATED);
    }

    @Operation(summary = "Activate a maintenance schedule",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedule updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceScheduleResource.class))),
                    @ApiResponse(responseCode = "404", description = "Schedule not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PostMapping("/{scheduleId}/activate")
    public ResponseEntity<MaintenanceScheduleResource> activateSchedule(@PathVariable Long scheduleId) {
        maintenanceScheduleCommandService.handle(ActivateMaintenanceScheduleCommandFromResourceAssembler.toCommandFromResource(scheduleId));
        var schedule = maintenanceScheduleQueryService.handle(new GetMaintenanceScheduleByIdQuery(scheduleId));
        if (schedule.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceScheduleResourceFromEntityAssembler.toResourceFromEntity(schedule.get()));
    }

    @Operation(summary = "Deactivate a maintenance schedule",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedule updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceScheduleResource.class))),
                    @ApiResponse(responseCode = "404", description = "Schedule not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PostMapping("/{scheduleId}/deactivate")
    public ResponseEntity<MaintenanceScheduleResource> deactivateSchedule(@PathVariable Long scheduleId) {
        maintenanceScheduleCommandService.handle(DeactivateMaintenanceScheduleCommandFromResourceAssembler.toCommandFromResource(scheduleId));
        var schedule = maintenanceScheduleQueryService.handle(new GetMaintenanceScheduleByIdQuery(scheduleId));
        if (schedule.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceScheduleResourceFromEntityAssembler.toResourceFromEntity(schedule.get()));
    }

    @Operation(summary = "Evaluate a maintenance schedule",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedule evaluated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceScheduleResource.class))),
                    @ApiResponse(responseCode = "404", description = "Schedule not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PostMapping("/{scheduleId}/evaluate")
    public ResponseEntity<MaintenanceScheduleResource> evaluateSchedule(@PathVariable Long scheduleId, @RequestBody(required = false) EvaluateMaintenanceScheduleResource resource) {
        maintenanceScheduleCommandService.handle(EvaluateMaintenanceScheduleCommandFromResourceAssembler.toCommandFromResource(scheduleId));
        var schedule = maintenanceScheduleQueryService.handle(new GetMaintenanceScheduleByIdQuery(scheduleId));
        if (schedule.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceScheduleResourceFromEntityAssembler.toResourceFromEntity(schedule.get()));
    }

    @Operation(summary = "Update schedule rules",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedule updated",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceScheduleResource.class))),
                    @ApiResponse(responseCode = "404", description = "Schedule not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @PutMapping("/{scheduleId}/rules")
    public ResponseEntity<MaintenanceScheduleResource> updateRules(
            @PathVariable Long scheduleId,
            @Valid @RequestBody UpdateMaintenanceRulesResource resource) {
        maintenanceScheduleCommandService.handle(UpdateMaintenanceRulesCommandFromResourceAssembler.toCommandFromResource(scheduleId, resource));
        var schedule = maintenanceScheduleQueryService.handle(new GetMaintenanceScheduleByIdQuery(scheduleId));
        if (schedule.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceScheduleResourceFromEntityAssembler.toResourceFromEntity(schedule.get()));
    }

    @Operation(summary = "Get a maintenance schedule by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedule found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceScheduleResource.class))),
                    @ApiResponse(responseCode = "404", description = "Schedule not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/{scheduleId}")
    public ResponseEntity<MaintenanceScheduleResource> getScheduleById(@PathVariable Long scheduleId) {
        var schedule = maintenanceScheduleQueryService.handle(new GetMaintenanceScheduleByIdQuery(scheduleId));
        if (schedule.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceScheduleResourceFromEntityAssembler.toResourceFromEntity(schedule.get()));
    }

    @Operation(summary = "Get a maintenance schedule by vehicle ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Schedule found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MaintenanceScheduleResource.class))),
                    @ApiResponse(responseCode = "404", description = "Schedule not found",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
            })
    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<MaintenanceScheduleResource> getScheduleByVehicleId(@PathVariable Long vehicleId) {
        var schedule = maintenanceScheduleQueryService.handle(new GetMaintenanceScheduleByVehicleIdQuery(vehicleId));
        if (schedule.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(MaintenanceScheduleResourceFromEntityAssembler.toResourceFromEntity(schedule.get()));
    }
}
