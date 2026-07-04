package org.upc.fleetservice.fleet.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.upc.fleetservice.fleet.domain.exceptions.DriverNotFoundException;
import org.upc.fleetservice.fleet.domain.model.aggregates.Route;
import org.upc.fleetservice.fleet.domain.model.queries.GetAllRoutesByDriverId;
import org.upc.fleetservice.fleet.domain.model.queries.GetAllRoutesQuery;
import org.upc.fleetservice.fleet.domain.model.queries.GetRouteByIdQuery;
import org.upc.fleetservice.fleet.domain.services.RouteQueryService;
import org.upc.fleetservice.fleet.infrastructure.persistence.jpa.repositories.DriverRepository;
import org.upc.fleetservice.fleet.infrastructure.persistence.jpa.repositories.RouteRepository;

import java.util.List;
import java.util.Optional;


@Service
public class RouteQueryServiceImpl   implements RouteQueryService {

    private final RouteRepository routeRepository;
    private final DriverRepository driverRepository;

    public RouteQueryServiceImpl(RouteRepository routeRepository, DriverRepository driverRepository) {
        this.routeRepository = routeRepository;
        this.driverRepository=driverRepository;
    }

    @Override
    public List<Route> handle(GetAllRoutesQuery query) {
        return routeRepository.findAll();
    }
    @Override
    public Optional<Route> handle(GetRouteByIdQuery query) {
        return routeRepository.findById(query.routeId());
    }

    @Override
    public List<Route> handle(GetAllRoutesByDriverId query) {
        var driver=driverRepository.findById(query.driverId());
        if(driver.isEmpty()) throw new DriverNotFoundException(query.driverId());

        return routeRepository.findAllByDriver(driver.get());
    }
}
