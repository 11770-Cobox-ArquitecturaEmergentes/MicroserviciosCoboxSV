package org.upc.fleetservice.fleet.domain.services;

import org.upc.fleetservice.fleet.domain.model.aggregates.Driver;
import org.upc.fleetservice.fleet.domain.model.queries.GetAllDriversQuery;
import org.upc.fleetservice.fleet.domain.model.queries.GetDriverByEmailQuery;
import org.upc.fleetservice.fleet.domain.model.queries.GetDriverByIdQuery;

import java.util.List;
import java.util.Optional;

public interface DriverQueryService {

    List<Driver> handle(GetAllDriversQuery query);
    Optional<Driver> handle(GetDriverByIdQuery query);
    Optional<Driver> handle(GetDriverByEmailQuery query);
}
