package org.upc.reportservice.report.application.internal.jobs;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.upc.reportservice.report.domain.model.entities.BronzeEvent;
import org.upc.reportservice.report.domain.model.entities.GoldIncidentMetrics;
import org.upc.reportservice.report.domain.model.entities.GoldOperationalMetric;
import org.upc.reportservice.report.domain.model.entities.SilverIncident;
import org.upc.reportservice.report.infrastructure.persistence.jpa.repositories.BronzeEventRepository;
import org.upc.reportservice.report.infrastructure.persistence.jpa.repositories.GoldIncidentMetricsRepository;
import org.upc.reportservice.report.infrastructure.persistence.jpa.repositories.GoldOperationalMetricRepository;
import org.upc.reportservice.report.infrastructure.persistence.jpa.repositories.SilverIncidentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MedallionEtlJob {

    private static final Logger logger = LoggerFactory.getLogger(MedallionEtlJob.class);

    private final BronzeEventRepository bronzeRepo;
    private final SilverIncidentRepository silverRepo;
    private final GoldIncidentMetricsRepository goldRepo;
    private final GoldOperationalMetricRepository operationalMetricRepo;
    private final ObjectMapper objectMapper;

    public MedallionEtlJob(BronzeEventRepository bronzeRepo,
                           SilverIncidentRepository silverRepo,
                           GoldIncidentMetricsRepository goldRepo,
                           GoldOperationalMetricRepository operationalMetricRepo,
                           ObjectMapper objectMapper) {
        this.bronzeRepo = bronzeRepo;
        this.silverRepo = silverRepo;
        this.goldRepo = goldRepo;
        this.operationalMetricRepo = operationalMetricRepo;
        this.objectMapper = objectMapper;
    }

    @Scheduled(initialDelay = 45000, fixedRate = 60000) // First run after 45s, then every minute
    @Transactional
    public void processBronzeToSilver() {
        try {
            logger.info("Executing Bronze to Silver ETL...");
            List<BronzeEvent> unprocessed = bronzeRepo.findByProcessedFalse();
            for (BronzeEvent event : unprocessed) {
                try {
                    JsonNode root = objectMapper.readTree(event.getRawData());
                    processOperationalMetrics(event, root);

                    if ("INCIDENT_CREATED".equals(event.getEventType()) || "incident.created".equals(event.getEventType())) {
                        try {
                            SilverIncident silver = new SilverIncident();
                            silver.setIncidentId(UUID.fromString(root.path("id").asText(root.path("incidentId").asText())));
                            silver.setType(root.path("type").asText());
                            silver.setSeverity(root.path("severity").asText());
                            silver.setStatus(root.path("status").asText());
                            silverRepo.save(silver);
                        } catch (IllegalArgumentException ex) {
                            logger.warn("Skipping Silver incident projection for event without UUID incident id: {}", event.getId());
                        }
                    }
                    event.setProcessed(true);
                    bronzeRepo.save(event);
                } catch (Exception e) {
                    logger.error("Error processing Bronze Event ID: " + event.getId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Error during Bronze to Silver ETL job", e);
        }
    }

    @Scheduled(initialDelay = 50000, fixedRate = 120000) // First run after 50s, then every 2 minutes
    @Transactional
    public void processSilverToGold() {
        try {
            logger.info("Executing Silver to Gold ETL...");
            long totalIncidents = silverRepo.count();
            
            GoldIncidentMetrics totalMetric = goldRepo.findByMetricName("TOTAL_INCIDENTS")
                    .orElse(new GoldIncidentMetrics());
            totalMetric.setMetricName("TOTAL_INCIDENTS");
            totalMetric.setMetricValue(String.valueOf(totalIncidents));
            goldRepo.save(totalMetric);
        } catch (Exception e) {
            logger.error("Error during Silver to Gold ETL job", e);
        }
    }

    private void processOperationalMetrics(BronzeEvent event, JsonNode root) {
        var domain = domainOf(event.getEventType());
        incrementMetric(domain, domain.toUpperCase() + "_EVENTS_TOTAL");
        incrementMetric(domain, metricName(domain, event.getEventType(), "TOTAL"));

        var status = root.path("status").asText(null);
        if (status != null && !status.isBlank()) {
            incrementMetric(domain, metricName(domain, "status." + status.toLowerCase(), "TOTAL"));
        }

        var severity = root.path("severity").asText(null);
        if (severity != null && !severity.isBlank()) {
            incrementMetric(domain, metricName(domain, "severity." + severity.toLowerCase(), "TOTAL"));
        }

        var type = root.path("type").asText(null);
        if (type != null && !type.isBlank()) {
            incrementMetric(domain, metricName(domain, "type." + type.toLowerCase(), "TOTAL"));
        }
    }

    private String domainOf(String eventType) {
        if (eventType == null) return "operations";
        if (eventType.startsWith("ai-validation.")) return "smartvision";
        if (eventType.startsWith("maintenance.")) return "maintenance";
        if (eventType.startsWith("fleet.")) return "fleet";
        if (eventType.startsWith("delivery.")) return "delivery";
        if (eventType.startsWith("incident.") || "INCIDENT_CREATED".equals(eventType)) return "incident";
        return "operations";
    }

    private String metricName(String domain, String key, String suffix) {
        return (domain + "." + key + "." + suffix).replace('-', '_').replace('.', '_').toUpperCase();
    }

    private void incrementMetric(String domain, String metricName) {
        var metric = operationalMetricRepo.findFirstByMetricName(metricName).orElseGet(GoldOperationalMetric::new);
        metric.setDomain(domain);
        metric.setMetricName(metricName);
        metric.setMetricValue(String.valueOf(parseLong(metric.getMetricValue()) + 1));
        metric.setAggregatedAt(LocalDateTime.now());
        operationalMetricRepo.save(metric);
    }

    private long parseLong(String value) {
        if (value == null || value.isBlank()) return 0L;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }
}
