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
import org.upc.reportservice.report.domain.model.entities.SilverIncident;
import org.upc.reportservice.report.infrastructure.persistence.jpa.repositories.BronzeEventRepository;
import org.upc.reportservice.report.infrastructure.persistence.jpa.repositories.GoldIncidentMetricsRepository;
import org.upc.reportservice.report.infrastructure.persistence.jpa.repositories.SilverIncidentRepository;

import java.util.List;
import java.util.UUID;

@Service
public class MedallionEtlJob {

    private static final Logger logger = LoggerFactory.getLogger(MedallionEtlJob.class);

    private final BronzeEventRepository bronzeRepo;
    private final SilverIncidentRepository silverRepo;
    private final GoldIncidentMetricsRepository goldRepo;
    private final ObjectMapper objectMapper;

    public MedallionEtlJob(BronzeEventRepository bronzeRepo, SilverIncidentRepository silverRepo, GoldIncidentMetricsRepository goldRepo, ObjectMapper objectMapper) {
        this.bronzeRepo = bronzeRepo;
        this.silverRepo = silverRepo;
        this.goldRepo = goldRepo;
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
                    if ("INCIDENT_CREATED".equals(event.getEventType())) {
                        JsonNode root = objectMapper.readTree(event.getRawData());
                        
                        SilverIncident silver = new SilverIncident();
                        silver.setIncidentId(UUID.fromString(root.path("id").asText()));
                        silver.setType(root.path("type").asText());
                        silver.setSeverity(root.path("severity").asText());
                        silver.setStatus(root.path("status").asText());
                        
                        silverRepo.save(silver);
                        
                        event.setProcessed(true);
                        bronzeRepo.save(event);
                    }
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
}
