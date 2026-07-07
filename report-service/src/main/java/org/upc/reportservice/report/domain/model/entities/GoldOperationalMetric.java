package org.upc.reportservice.report.domain.model.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "gold_operational_metrics")
public class GoldOperationalMetric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String domain;

    @Column(nullable = false)
    private String metricName;

    @Column(nullable = false)
    private String metricValue;

    @Column(nullable = false)
    private LocalDateTime aggregatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getMetricValue() { return metricValue; }
    public void setMetricValue(String metricValue) { this.metricValue = metricValue; }
    public LocalDateTime getAggregatedAt() { return aggregatedAt; }
    public void setAggregatedAt(LocalDateTime aggregatedAt) { this.aggregatedAt = aggregatedAt; }
}
