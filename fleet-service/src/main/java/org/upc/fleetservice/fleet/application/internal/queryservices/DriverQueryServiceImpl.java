package org.upc.fleetservice.fleet.application.internal.queryservices;

import org.springframework.stereotype.Service;
import org.upc.fleetservice.fleet.domain.model.aggregates.Driver;
import org.upc.fleetservice.fleet.domain.model.queries.GetAllDriversQuery;
import org.upc.fleetservice.fleet.domain.model.queries.GetDriverByEmailQuery;
import org.upc.fleetservice.fleet.domain.model.queries.GetDriverByIdQuery;
import org.upc.fleetservice.fleet.domain.services.DriverQueryService;
import org.upc.fleetservice.fleet.infrastructure.persistence.jpa.repositories.DriverRepository;

import java.util.List;
import java.util.Optional;

@Service
public class DriverQueryServiceImpl implements DriverQueryService {

    private final DriverRepository driverRepository;

    public DriverQueryServiceImpl(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public List<Driver> handle(GetAllDriversQuery query) {
        return driverRepository.findAll();
    }
    @Override
    public Optional<Driver> handle(GetDriverByIdQuery query) {
        return driverRepository.findById(query.driverId());
    }

    @Override
    public Optional<Driver> handle(GetDriverByEmailQuery query) {
        return driverRepository.findByEmail(query.email());
    }

}
