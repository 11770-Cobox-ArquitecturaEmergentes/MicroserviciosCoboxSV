package org.upc.reportservice.report.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.upc.reportservice.report.domain.model.entities.GoldIncidentMetrics;
import org.upc.reportservice.report.domain.model.entities.GoldOperationalMetric;
import org.upc.reportservice.report.infrastructure.persistence.jpa.repositories.GoldIncidentMetricsRepository;
import org.upc.reportservice.report.infrastructure.persistence.jpa.repositories.GoldOperationalMetricRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final GoldIncidentMetricsRepository goldRepo;
    private final GoldOperationalMetricRepository operationalMetricRepo;

    public ReportController(GoldIncidentMetricsRepository goldRepo,
                            GoldOperationalMetricRepository operationalMetricRepo) {
        this.goldRepo = goldRepo;
        this.operationalMetricRepo = operationalMetricRepo;
    }

    @GetMapping("/incidents")
    public ResponseEntity<List<GoldIncidentMetrics>> getIncidentMetrics() {
        return ResponseEntity.ok(goldRepo.findAll());
    }

    @GetMapping("/operations")
    public ResponseEntity<List<GoldOperationalMetric>> getOperationsMetrics() {
        return ResponseEntity.ok(operationalMetricRepo.findAll());
    }

    @GetMapping("/smartvision")
    public ResponseEntity<List<GoldOperationalMetric>> getSmartVisionMetrics() {
        return ResponseEntity.ok(operationalMetricRepo.findByDomainOrderByMetricNameAsc("smartvision"));
    }

    @GetMapping("/maintenance")
    public ResponseEntity<List<GoldOperationalMetric>> getMaintenanceMetrics() {
        return ResponseEntity.ok(operationalMetricRepo.findByDomainOrderByMetricNameAsc("maintenance"));
    }

    @GetMapping("/fleet")
    public ResponseEntity<List<GoldOperationalMetric>> getFleetMetrics() {
        return ResponseEntity.ok(operationalMetricRepo.findByDomainOrderByMetricNameAsc("fleet"));
    }

    @GetMapping("/deliveries")
    public ResponseEntity<List<GoldOperationalMetric>> getDeliveryMetrics() {
        return ResponseEntity.ok(operationalMetricRepo.findByDomainOrderByMetricNameAsc("delivery"));
    }
}
