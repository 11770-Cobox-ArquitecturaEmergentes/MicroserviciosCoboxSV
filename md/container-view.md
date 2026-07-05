# CoBox Smart Vision - Container View

## Mobile Evidence Upload Flow

The mobile app does not upload evidence binaries through backend services.

1. Mobile requests an upload intent through `Gateway -> mobile-bff-service`.
2. `mobile-bff-service` creates an idempotent `UploadIntent`, generates an S3 object key and returns a presigned `PUT` URL.
3. Mobile uploads the binary directly to S3 using the required signed headers.
4. Mobile confirms the upload through `Gateway -> mobile-bff-service`.
5. `mobile-bff-service` validates the uploaded object with S3 `HeadObject`.
6. `mobile-bff-service` records `EvidenceUploadConfirmed` in its local outbox and publishes it to RabbitMQ in local/demo deployments.
7. Mobile continues synchronizing offline metadata and events through `Gateway -> edge-service`.

```text
Mobile App
  | POST /api/v1/mobile/evidence/upload-intents
  v
Gateway
  v
mobile-bff-service
  | presign PUT
  v
Amazon S3

Mobile App
  | PUT binary evidence using presigned URL
  v
Amazon S3

Mobile App
  | POST /api/v1/mobile/evidence/upload-intents/{id}/confirm
  v
Gateway
  v
mobile-bff-service
  | HeadObject validation
  v
Amazon S3

mobile-bff-service
  | local outbox relay: EvidenceUploadConfirmed
  v
RabbitMQ local/demo
  | exchange cobox.events
  | routing key evidence.upload.confirmed
  v
ai-validation-service
  +--> Amazon S3: object read/analyze reference
  +--> AWS Textract: OCR/documents
  +--> AWS Rekognition: visual labels
  +--> edge-service: evidence metadata and route telemetry

Mobile App
  | POST /api/v1/edge/sync-batches
  v
Gateway
  v
edge-service
```

## Ownership Boundary

`mobile-bff-service` owns upload authorization and confirmation only. It does not become the source of truth for logistics evidence. `edge-service` remains responsible for offline synchronization metadata, and future async consumers will correlate records by `clientEvidenceId`.

`ai-validation-service` owns asynchronous evidence analysis results and AI alerts. It does not block the driver's workflow and does not write final logistics evidence records. It consumes `EvidenceUploadConfirmed`, loads the object context from S3, runs AWS Textract/Rekognition through `AiVisionProvider`, and correlates with `edge-service` by `clientEvidenceId` and `routeId`.

## AI Validation Event Flow

```text
mobile-bff-service
  | PENDING -> PROCESSING -> PUBLISHED / FAILED
  | max retries exceeded -> DEAD_LETTERED
  v
RabbitMQ local/demo
  | Exchange: cobox.events
  | Queue: ai.evidence-upload-confirmed
  | DLQ: ai.evidence-upload-confirmed.dlq
  v
ai-validation-service
  | idempotency key: clientEvidenceId
  | analysis states:
  | PENDING, PROCESSING, COMPLETED, FAILED, REVIEW_REQUIRED,
  | RECAPTURE_REQUIRED, FRAUD_SUSPECTED, DEGRADED
  v
Management clients
  | GET /api/v1/ai-validation/evidence-analyses/{clientEvidenceId}
  | GET /api/v1/ai-validation/alerts
```

## Desktop Aggregated Views

The desktop web app consumes operational views through `desktop-bff-service` instead of composing data directly from internal microservices.

```text
Web App
  | GET /api/v1/desktop/dashboard/operations
  | GET /api/v1/desktop/routes/{routeId}/overview
  | GET /api/v1/desktop/vehicles/{vehicleId}/health
  v
Gateway
  v
desktop-bff-service
  | Feign REST calls
  +--> fleet-service
  +--> delivery-service
  +--> incident-service
  +--> maintenance-service
```

`desktop-bff-service` is read-only in this cut. It does not own domain state, does not persist data, and returns partial responses with `degradedSections` when secondary dependencies fail.
