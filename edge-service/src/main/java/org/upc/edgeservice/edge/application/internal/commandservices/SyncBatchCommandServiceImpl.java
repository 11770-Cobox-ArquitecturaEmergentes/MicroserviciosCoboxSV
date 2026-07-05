package org.upc.edgeservice.edge.application.internal.commandservices;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.upc.edgeservice.edge.domain.model.aggregates.SyncBatch;
import org.upc.edgeservice.edge.domain.model.entities.EdgeEventRecord;
import org.upc.edgeservice.edge.domain.model.entities.EdgeEvidence;
import org.upc.edgeservice.edge.domain.model.entities.EdgeTelemetrySample;
import org.upc.edgeservice.edge.domain.model.valueobjects.SyncBatchStatus;
import org.upc.edgeservice.edge.domain.model.valueobjects.SyncItemStatus;
import org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories.EdgeEventRecordRepository;
import org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories.EdgeEvidenceRepository;
import org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories.EdgeTelemetrySampleRepository;
import org.upc.edgeservice.edge.infrastructure.persistence.jpa.repositories.SyncBatchRepository;
import org.upc.edgeservice.edge.interfaces.rest.resources.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class SyncBatchCommandServiceImpl implements SyncBatchCommandService {

    private final SyncBatchRepository syncBatchRepository;
    private final EdgeEvidenceRepository evidenceRepository;
    private final EdgeTelemetrySampleRepository telemetryRepository;
    private final EdgeEventRecordRepository eventRepository;

    public SyncBatchCommandServiceImpl(SyncBatchRepository syncBatchRepository,
                                       EdgeEvidenceRepository evidenceRepository,
                                       EdgeTelemetrySampleRepository telemetryRepository,
                                       EdgeEventRecordRepository eventRepository) {
        this.syncBatchRepository = syncBatchRepository;
        this.evidenceRepository = evidenceRepository;
        this.telemetryRepository = telemetryRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public SyncBatchResponse handle(SyncBatchRequest request) {
        var headerErrors = validateHeader(request);
        if (!headerErrors.isEmpty()) {
            return new SyncBatchResponse(null, request == null ? null : request.clientBatchId(), SyncBatchStatus.REJECTED, headerErrors);
        }

        var existingBatch = syncBatchRepository.findByClientBatchId(request.clientBatchId());
        if (existingBatch.isPresent()) {
            return duplicateBatchResponse(existingBatch.get());
        }

        var results = new ArrayList<SyncItemResultResource>();
        var acceptedItems = 0;
        var rejectedItems = 0;
        var duplicateItems = 0;
        var evidenceIdsInRequest = new HashSet<UUID>();
        var telemetryIdsInRequest = new HashSet<UUID>();
        var eventIdsInRequest = new HashSet<UUID>();

        var batch = syncBatchRepository.save(new SyncBatch(
                request.clientBatchId(),
                request.driverId(),
                request.deviceId().trim(),
                request.sentAt(),
                SyncBatchStatus.ACCEPTED
        ));

        for (var evidence : safeList(request.evidences())) {
            var validation = validateEvidence(evidence, evidenceIdsInRequest);
            if (validation != null) {
                results.add(rejected(evidence == null ? null : evidence.clientEvidenceId(), validation));
                rejectedItems++;
                continue;
            }
            var duplicate = evidenceRepository.findByClientEvidenceId(evidence.clientEvidenceId());
            if (duplicate.isPresent()) {
                results.add(duplicate(evidence.clientEvidenceId(), duplicate.get().getId(), "Evidence already recorded"));
                duplicateItems++;
                continue;
            }
            var saved = evidenceRepository.save(new EdgeEvidence(
                    evidence.clientEvidenceId(),
                    batch,
                    evidence.orderId(),
                    evidence.routeId(),
                    evidence.type().trim(),
                    evidence.objectKey().trim(),
                    evidence.sha256().trim(),
                    trimToNull(evidence.mimeType()),
                    evidence.sizeBytes(),
                    evidence.capturedAt()
            ));
            results.add(recorded(evidence.clientEvidenceId(), saved.getId(), "Evidence recorded"));
            acceptedItems++;
        }

        for (var telemetry : safeList(request.telemetry())) {
            var validation = validateTelemetry(telemetry, telemetryIdsInRequest);
            if (validation != null) {
                results.add(rejected(telemetry == null ? null : telemetry.clientTelemetryId(), validation));
                rejectedItems++;
                continue;
            }
            var duplicate = telemetryRepository.findByClientTelemetryId(telemetry.clientTelemetryId());
            if (duplicate.isPresent()) {
                results.add(duplicate(telemetry.clientTelemetryId(), duplicate.get().getId(), "Telemetry already recorded"));
                duplicateItems++;
                continue;
            }
            var saved = telemetryRepository.save(new EdgeTelemetrySample(
                    telemetry.clientTelemetryId(),
                    batch,
                    telemetry.routeId(),
                    telemetry.latitude(),
                    telemetry.longitude(),
                    telemetry.accuracyMeters(),
                    telemetry.speedKmh(),
                    telemetry.batteryLevel(),
                    telemetry.capturedAt()
            ));
            results.add(recorded(telemetry.clientTelemetryId(), saved.getId(), "Telemetry recorded"));
            acceptedItems++;
        }

        for (var event : safeList(request.events())) {
            var validation = validateEvent(event, eventIdsInRequest);
            if (validation != null) {
                results.add(rejected(event == null ? null : event.clientEventId(), validation));
                rejectedItems++;
                continue;
            }
            var duplicate = eventRepository.findByClientEventId(event.clientEventId());
            if (duplicate.isPresent()) {
                results.add(duplicate(event.clientEventId(), duplicate.get().getId(), "Event already recorded"));
                duplicateItems++;
                continue;
            }
            var saved = eventRepository.save(new EdgeEventRecord(
                    event.clientEventId(),
                    batch,
                    event.type().trim(),
                    event.aggregateType().trim(),
                    event.aggregateId().trim(),
                    event.payload(),
                    event.occurredAt()
            ));
            results.add(recorded(event.clientEventId(), saved.getId(), "Event recorded"));
            acceptedItems++;
        }

        var status = resolveStatus(acceptedItems, rejectedItems, duplicateItems);
        batch.updateStatus(status);
        syncBatchRepository.save(batch);
        return new SyncBatchResponse(batch.getId(), batch.getClientBatchId(), status, results);
    }

    private List<SyncItemResultResource> validateHeader(SyncBatchRequest request) {
        var errors = new ArrayList<SyncItemResultResource>();
        if (request == null) {
            errors.add(rejected(null, "Request body is required"));
            return errors;
        }
        if (request.clientBatchId() == null) errors.add(rejected(null, "clientBatchId is required"));
        if (request.driverId() == null) errors.add(rejected(null, "driverId is required"));
        if (isBlank(request.deviceId())) errors.add(rejected(null, "deviceId is required"));
        if (request.sentAt() == null) errors.add(rejected(null, "sentAt is required"));
        return errors;
    }

    private SyncBatchResponse duplicateBatchResponse(SyncBatch batch) {
        var results = new ArrayList<SyncItemResultResource>();
        evidenceRepository.findBySyncBatchId(batch.getId()).forEach(evidence ->
                results.add(duplicate(evidence.getClientEvidenceId(), evidence.getId(), "Evidence already recorded in this batch")));
        telemetryRepository.findBySyncBatchId(batch.getId()).forEach(telemetry ->
                results.add(duplicate(telemetry.getClientTelemetryId(), telemetry.getId(), "Telemetry already recorded in this batch")));
        eventRepository.findBySyncBatchId(batch.getId()).forEach(event ->
                results.add(duplicate(event.getClientEventId(), event.getId(), "Event already recorded in this batch")));
        return new SyncBatchResponse(batch.getId(), batch.getClientBatchId(), SyncBatchStatus.DUPLICATE, results);
    }

    private String validateEvidence(EdgeEvidenceRequest evidence, HashSet<UUID> idsInRequest) {
        if (evidence == null) return "Evidence item is required";
        if (evidence.clientEvidenceId() == null) return "clientEvidenceId is required";
        if (!idsInRequest.add(evidence.clientEvidenceId())) return "clientEvidenceId is duplicated in request";
        if (evidence.orderId() == null) return "orderId is required";
        if (isBlank(evidence.type())) return "type is required";
        if (isBlank(evidence.objectKey())) return "objectKey is required";
        if (isBlank(evidence.sha256())) return "sha256 is required";
        if (evidence.capturedAt() == null) return "capturedAt is required";
        if (evidence.sizeBytes() != null && evidence.sizeBytes() < 0) return "sizeBytes must be greater than or equal to 0";
        return null;
    }

    private String validateTelemetry(EdgeTelemetryRequest telemetry, HashSet<UUID> idsInRequest) {
        if (telemetry == null) return "Telemetry item is required";
        if (telemetry.clientTelemetryId() == null) return "clientTelemetryId is required";
        if (!idsInRequest.add(telemetry.clientTelemetryId())) return "clientTelemetryId is duplicated in request";
        if (!isLatitude(telemetry.latitude())) return "latitude must be between -90 and 90";
        if (!isLongitude(telemetry.longitude())) return "longitude must be between -180 and 180";
        if (telemetry.capturedAt() == null) return "capturedAt is required";
        if (telemetry.batteryLevel() != null && (telemetry.batteryLevel() < 0 || telemetry.batteryLevel() > 100)) {
            return "batteryLevel must be between 0 and 100";
        }
        return null;
    }

    private String validateEvent(EdgeEventRequest event, HashSet<UUID> idsInRequest) {
        if (event == null) return "Event item is required";
        if (event.clientEventId() == null) return "clientEventId is required";
        if (!idsInRequest.add(event.clientEventId())) return "clientEventId is duplicated in request";
        if (isBlank(event.type())) return "type is required";
        if (isBlank(event.aggregateType())) return "aggregateType is required";
        if (isBlank(event.aggregateId())) return "aggregateId is required";
        if (event.occurredAt() == null) return "occurredAt is required";
        return null;
    }

    private SyncBatchStatus resolveStatus(int acceptedItems, int rejectedItems, int duplicateItems) {
        if (acceptedItems == 0 && rejectedItems > 0 && duplicateItems == 0) return SyncBatchStatus.REJECTED;
        if (rejectedItems > 0 || duplicateItems > 0) return SyncBatchStatus.PARTIALLY_ACCEPTED;
        return SyncBatchStatus.ACCEPTED;
    }

    private SyncItemResultResource recorded(UUID clientId, Long serverId, String message) {
        return new SyncItemResultResource(clientId, serverId, SyncItemStatus.RECORDED, message);
    }

    private SyncItemResultResource duplicate(UUID clientId, Long serverId, String message) {
        return new SyncItemResultResource(clientId, serverId, SyncItemStatus.DUPLICATE, message);
    }

    private SyncItemResultResource rejected(UUID clientId, String message) {
        return new SyncItemResultResource(clientId, null, SyncItemStatus.REJECTED, message);
    }

    private boolean isLatitude(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.valueOf(-90)) >= 0 && value.compareTo(BigDecimal.valueOf(90)) <= 0;
    }

    private boolean isLongitude(BigDecimal value) {
        return value != null && value.compareTo(BigDecimal.valueOf(-180)) >= 0 && value.compareTo(BigDecimal.valueOf(180)) <= 0;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private <T> List<T> safeList(List<T> values) {
        return values == null ? List.of() : values;
    }
}
