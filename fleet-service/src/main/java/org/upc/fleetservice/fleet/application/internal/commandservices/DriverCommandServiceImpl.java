package org.upc.fleetservice.fleet.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.upc.fleetservice.fleet.domain.exceptions.DriverAlreadyExistsException;
import org.upc.fleetservice.fleet.domain.exceptions.DriverNotFoundException;
import org.upc.fleetservice.fleet.domain.model.aggregates.Driver;
import org.upc.fleetservice.fleet.domain.model.commands.CreateDriverCommand;
import org.upc.fleetservice.fleet.domain.services.DriverCommandService;
import org.upc.fleetservice.fleet.infrastructure.messaging.FleetReportEventPublisher;
import org.upc.fleetservice.fleet.infrastructure.persistence.jpa.repositories.DriverRepository;
import org.upc.fleetservice.fleet.domain.model.commands.UpdateDriverStatusOnCompletedRouteCommand;

@Service
public class DriverCommandServiceImpl implements DriverCommandService {

    private final DriverRepository driverRepository;
    private final FleetReportEventPublisher reportEventPublisher;
    public DriverCommandServiceImpl(DriverRepository driverRepository, FleetReportEventPublisher reportEventPublisher) {
        this.driverRepository = driverRepository;
        this.reportEventPublisher = reportEventPublisher;
    }

    @Override
    public Long handle(CreateDriverCommand command) {
        if (driverRepository.existsByEmail(command.email())) {
            throw new DriverAlreadyExistsException("email", command.email());
        }
        if (driverRepository.existsByLicenceNumber(command.licenceNumber())) {
            throw new DriverAlreadyExistsException("licenceNumber", command.licenceNumber());
        }
        var driver = new Driver(command);
        driverRepository.save(driver);
        reportEventPublisher.publishDriver("fleet.driver-created", driver);
        return driver.getId();
    }

    @Override
    public void handle(UpdateDriverStatusOnCompletedRouteCommand command) {
        driverRepository.findById(command.driverId()).map(driver -> {
            driver.returnFromRoute();
            driverRepository.save(driver);
            reportEventPublisher.publishDriver("fleet.driver-status-updated", driver);
            return driver.getId();
        }).orElseThrow(() -> new DriverNotFoundException(command.driverId()));
    }
}
