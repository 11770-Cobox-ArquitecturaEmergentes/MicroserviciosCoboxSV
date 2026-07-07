package org.upc.maintenanceservice.maintenance.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.upc.maintenanceservice.maintenance.domain.exceptions.MaintenanceScheduleNotFoundException;
import org.upc.maintenanceservice.maintenance.domain.model.aggregates.MaintenanceSchedule;
import org.upc.maintenanceservice.maintenance.domain.model.commands.*;
import org.upc.maintenanceservice.maintenance.domain.services.MaintenanceScheduleCommandService;
import org.upc.maintenanceservice.maintenance.infrastructure.messaging.MaintenanceReportEventPublisher;
import org.upc.maintenanceservice.maintenance.infrastructure.persistence.jpa.repositories.MaintenanceScheduleRepository;

@Service
public class MaintenanceScheduleCommandServiceImpl implements MaintenanceScheduleCommandService {

    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final MaintenanceReportEventPublisher reportEventPublisher;

    public MaintenanceScheduleCommandServiceImpl(MaintenanceScheduleRepository maintenanceScheduleRepository,
                                                 MaintenanceReportEventPublisher reportEventPublisher) {
        this.maintenanceScheduleRepository = maintenanceScheduleRepository;
        this.reportEventPublisher = reportEventPublisher;
    }

    @Override
    public Long handle(CreateMaintenanceScheduleCommand command) {
        var maintenanceSchedule = new MaintenanceSchedule(command);
        maintenanceScheduleRepository.save(maintenanceSchedule);
        reportEventPublisher.publishSchedule("maintenance.schedule-created", maintenanceSchedule);
        return maintenanceSchedule.getId();
    }

    @Override
    public void handle(ActivateMaintenanceScheduleCommand command) {
        maintenanceScheduleRepository.findById(command.scheduleId()).map(schedule -> {
            schedule.activate(command);
            maintenanceScheduleRepository.save(schedule);
            reportEventPublisher.publishSchedule("maintenance.schedule-activated", schedule);
            return schedule.getId();
        }).orElseThrow(() -> new MaintenanceScheduleNotFoundException(command.scheduleId()));
    }

    @Override
    public void handle(DeactivateMaintenanceScheduleCommand command) {
        maintenanceScheduleRepository.findById(command.scheduleId()).map(schedule -> {
            schedule.deactivate(command);
            maintenanceScheduleRepository.save(schedule);
            reportEventPublisher.publishSchedule("maintenance.schedule-deactivated", schedule);
            return schedule.getId();
        }).orElseThrow(() -> new MaintenanceScheduleNotFoundException(command.scheduleId()));
    }

    @Override
    public void handle(EvaluateMaintenanceScheduleCommand command) {
        maintenanceScheduleRepository.findById(command.scheduleId()).map(schedule -> {
            schedule.evaluate(command);
            maintenanceScheduleRepository.save(schedule);
            reportEventPublisher.publishSchedule("maintenance.schedule-evaluated", schedule);
            return schedule.getId();
        }).orElseThrow(() -> new MaintenanceScheduleNotFoundException(command.scheduleId()));
    }

    @Override
    public void handle(UpdateMaintenanceRulesCommand command) {
        maintenanceScheduleRepository.findById(command.scheduleId()).map(schedule -> {
            schedule.updateRules(command);
            maintenanceScheduleRepository.save(schedule);
            reportEventPublisher.publishSchedule("maintenance.rules-updated", schedule);
            return schedule.getId();
        }).orElseThrow(() -> new MaintenanceScheduleNotFoundException(command.scheduleId()));
    }
}
