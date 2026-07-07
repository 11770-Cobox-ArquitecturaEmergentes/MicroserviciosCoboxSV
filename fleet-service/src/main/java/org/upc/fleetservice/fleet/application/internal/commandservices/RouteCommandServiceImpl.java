package org.upc.fleetservice.fleet.application.internal.commandservices;


import org.springframework.stereotype.Service;

import org.upc.fleetservice.fleet.application.internal.outboundservice.ExternalOrderService;
import org.upc.fleetservice.fleet.domain.exceptions.*;
import org.upc.fleetservice.fleet.domain.model.aggregates.Driver;
import org.upc.fleetservice.fleet.domain.model.aggregates.Route;
import org.upc.fleetservice.fleet.domain.model.aggregates.Vehicle;
import org.upc.fleetservice.fleet.domain.model.commands.*;
import org.upc.fleetservice.fleet.domain.model.valueobjects.OrderId;
import org.upc.fleetservice.fleet.domain.services.RouteCommandService;
import org.upc.fleetservice.fleet.infrastructure.messaging.FleetReportEventPublisher;
import org.upc.fleetservice.fleet.infrastructure.persistence.jpa.repositories.DriverRepository;
import org.upc.fleetservice.fleet.infrastructure.persistence.jpa.repositories.RouteRepository;
import org.upc.fleetservice.fleet.infrastructure.persistence.jpa.repositories.VehicleRepository;


@Service
public class RouteCommandServiceImpl implements RouteCommandService {

    private final RouteRepository routeRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final ExternalOrderService externalOrderService;
    private final FleetReportEventPublisher reportEventPublisher;
    public RouteCommandServiceImpl(RouteRepository routeRepository, DriverRepository driverRepository, VehicleRepository vehicleRepository, ExternalOrderService externalOrderService, FleetReportEventPublisher reportEventPublisher) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.routeRepository = routeRepository;
        this.externalOrderService=externalOrderService;
        this.reportEventPublisher = reportEventPublisher;
    }

    @Override
    public Long handle(CreateRouteCommand command) {
        var route = new Route(command);
        routeRepository.save(route);
        reportEventPublisher.publishRoute("fleet.route-created", route);
        return route.getId();
    }
    @Override
    public void handle(AddOrderToRouteCommand command) {
        Route route = routeRepository.findById(command.routeId())
                .orElseThrow(() -> new RouteNotFoundException(command.routeId()));
        var orderExists= externalOrderService.fetchOrderById(command.orderId());
        if (orderExists==null) {
            throw new OrderNotFoundException(command.orderId());
        }
        var orderId= new OrderId(command.orderId());
        route.addOrder(orderId);
        externalOrderService.markAsInTransitOrderById(command.orderId());
        routeRepository.save(route);
        reportEventPublisher.publishRoute("fleet.route-order-added", route);
    }

    @Override
    public void handle(AddDeliveredOrderToRouteCommand command) {
        Route route = routeRepository.findById(command.routeId())
                .orElseThrow(() -> new RouteNotFoundException(command.routeId()));

        var orderExists= externalOrderService.fetchOrderById(command.orderId());
        if (orderExists==null) {
            throw new OrderNotFoundException(command.orderId());
        }
        var orderId= new OrderId(command.orderId());
        route.addDeliveredOrder(orderId);
        routeRepository.save(route);
        reportEventPublisher.publishRoute("fleet.route-order-delivered", route);
    }
    @Override
    public void handle(AssignDriverToRouteCommand command) {

        Route route = routeRepository.findById(command.routeId())
                .orElseThrow(() -> new RouteNotFoundException(command.routeId()));

        if(route.getDriver()!=null){
            throw new RouteAlreadyAssignedDriverException(route.getDriver().getId());
        }

        Driver driver = driverRepository.findById(command.driverId())
                .orElseThrow(() -> new DriverNotFoundException(command.driverId()));
        if (!driver.hasAvailable()) {
            throw new DriverNotAvailableException(driver.getDriverStatus());
        }

        driver.markAsInRoute();
        route.assignDriver(driver);
        routeRepository.save(route);
        reportEventPublisher.publishRoute("fleet.route-driver-assigned", route);
    }

    @Override
    public void handle(AssignVehicleToRouteCommand command) {
        Route route = routeRepository.findById(command.routeId())
                .orElseThrow(() -> new RouteNotFoundException(command.routeId()));

        if(route.getVehicle()!=null){
            throw new RouteAlreadyAssignedVehicleException(route.getVehicle().getId());
        }

        Vehicle vehicle = vehicleRepository.findById(command.vehicleId())
                .orElseThrow(() -> new VehicleNotFoundException(command.vehicleId()));

        double totalWeightKg = route.getOrderIds().stream()
                .mapToDouble(orderId -> externalOrderService.fetchOrderById(orderId.orderId()).weightKg())
                .sum();

        if (!vehicle.hasCapacityFor(totalWeightKg)) {
            throw new VehicleCapacityExceededException(totalWeightKg,vehicle.getCapacityKg());
        }

        vehicle.markAsInRoute();
        route.assignVehicle(vehicle);
        routeRepository.save(route);
        reportEventPublisher.publishRoute("fleet.route-vehicle-assigned", route);
    }

    @Override
    public void handle(MarkAsInProgressRouteCommand command) {
        routeRepository.findById(command.routeId()).map(route -> {
            route.markAsInProgress();
            routeRepository.save(route);
            reportEventPublisher.publishRoute("fleet.route-started", route);
            return route.getId();
        }).orElseThrow(() -> new RouteNotFoundException(command.routeId()));
    }


}
