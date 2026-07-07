package org.upc.edgeservice.edge.domain.model.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.upc.edgeservice.edge.domain.model.aggregates.SyncBatch;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_edge_telemetry_client_telemetry_id", columnNames = "client_telemetry_id"))
public class EdgeTelemetrySample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_telemetry_id", nullable = false, updatable = false)
    private UUID clientTelemetryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sync_batch_id", nullable = false)
    private SyncBatch syncBatch;

    private Long routeId;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    private BigDecimal accuracyMeters;

    private BigDecimal speedKmh;

    private Integer batteryLevel;

    @Column(nullable = false)
    private Instant capturedAt;

    public EdgeTelemetrySample(UUID clientTelemetryId, SyncBatch syncBatch, Long routeId, BigDecimal latitude,
                               BigDecimal longitude, BigDecimal accuracyMeters, BigDecimal speedKmh,
                               Integer batteryLevel, Instant capturedAt) {
        this.clientTelemetryId = clientTelemetryId;
        this.syncBatch = syncBatch;
        this.routeId = routeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.accuracyMeters = accuracyMeters;
        this.speedKmh = speedKmh;
        this.batteryLevel = batteryLevel;
        this.capturedAt = capturedAt;
    }
}
