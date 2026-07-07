package org.upc.reportservice.report.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.upc.reportservice.report.domain.model.entities.GoldIncidentMetrics;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GoldIncidentMetricsRepository extends JpaRepository<GoldIncidentMetrics, Long> {
    Optional<GoldIncidentMetrics> findByMetricName(String metricName);
}
