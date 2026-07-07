package org.upc.fleetservice.fleet.application.internal.eventhandlers;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.upc.fleetservice.fleet.domain.model.commands.UpdateDriverStatusOnCompletedRouteCommand;
import org.upc.fleetservice.fleet.domain.model.commands.UpdateVehicleStatusOnCompletedRouteCommand;
import org.upc.fleetservice.fleet.domain.model.events.RouteCompletedEvent;
import org.upc.fleetservice.fleet.domain.model.queries.GetRouteByIdQuery;
import org.upc.fleetservice.fleet.domain.services.DriverCommandService;
import org.upc.fleetservice.fleet.domain.services.RouteQueryService;
import org.upc.fleetservice.fleet.domain.services.VehicleCommandService;
import org.upc.fleetservice.fleet.infrastructure.messaging.FleetReportEventPublisher;

@Service
public class RouteCompletedEventHandler {
    private final VehicleCommandService vehicleCommandService;
    private final DriverCommandService driverCommandService;
    private final RouteQueryService routeQueryService;
    private final FleetReportEventPublisher reportEventPublisher;

    public RouteCompletedEventHandler(VehicleCommandService vehicleCommandService,
                                      DriverCommandService driverCommandService,
                                      RouteQueryService routeQueryService,
                                      FleetReportEventPublisher reportEventPublisher) {
        this.vehicleCommandService = vehicleCommandService;
        this.driverCommandService = driverCommandService;
        this.routeQueryService = routeQueryService;
        this.reportEventPublisher = reportEventPublisher;
    }

    @EventListener(RouteCompletedEvent.class)
    public void on(RouteCompletedEvent event) {
        var getRouteByIdQuery = new GetRouteByIdQuery(event.getRouteId());
        var route = routeQueryService.handle(getRouteByIdQuery);
        if (route.isPresent()) {
            var updateVehicleStatusOnCompletedRouteCommand = new UpdateVehicleStatusOnCompletedRouteCommand(route.get().getVehicle().getId());
            var updateDriverStatusOnCompletedRouteCommand = new UpdateDriverStatusOnCompletedRouteCommand(route.get().getDriver().getId());
            driverCommandService.handle(updateDriverStatusOnCompletedRouteCommand);
            vehicleCommandService.handle(updateVehicleStatusOnCompletedRouteCommand);
            reportEventPublisher.publishRoute("fleet.route-completed", route.get());
        }
        System.out.println("RouteCompletedEventHandler executed");
    }


}
