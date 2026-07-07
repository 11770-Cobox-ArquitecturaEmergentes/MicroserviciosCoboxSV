package org.upc.reportservice.report.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.upc.reportservice.report.domain.model.entities.GoldOperationalMetric;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoldOperationalMetricRepository extends JpaRepository<GoldOperationalMetric, Long> {
    Optional<GoldOperationalMetric> findFirstByMetricName(String metricName);
    List<GoldOperationalMetric> findByDomainOrderByMetricNameAsc(String domain);
}
