package org.upc.reportservice.report.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.upc.reportservice.report.domain.model.entities.GoldIncidentMetrics;
import org.upc.reportservice.report.infrastructure.persistence.jpa.repositories.GoldIncidentMetricsRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final GoldIncidentMetricsRepository goldRepo;

    public ReportController(GoldIncidentMetricsRepository goldRepo) {
        this.goldRepo = goldRepo;
    }

    @GetMapping("/incidents")
    public ResponseEntity<List<GoldIncidentMetrics>> getIncidentMetrics() {
        return ResponseEntity.ok(goldRepo.findAll());
    }
}
