# ai-validation-service

## Purpose

`ai-validation-service` is the asynchronous auditor for uploaded evidence. It consumes `EvidenceUploadConfirmed`, analyzes the S3 object through `AiVisionProvider`, correlates the event with offline metadata and telemetry from `edge-service`, and persists analysis results plus management alerts.

The driver does not wait for this service. Upload confirmation remains in `mobile-bff-service`; logistics evidence metadata remains in `edge-service`.

## Main Flow

1. `mobile-bff-service` confirms S3 `HeadObject` and stores an outbox event.
2. The mobile outbox relay publishes `EvidenceUploadConfirmed` to RabbitMQ local/demo.
3. `ai-validation-service` consumes from `ai.evidence-upload-confirmed`.
4. `AiVisionProvider` runs the configured provider.
5. `AwsAiVisionProvider` uses AWS Textract for OCR/document evidence and AWS Rekognition for visual labels.
6. The service queries `edge-service` for offline evidence metadata and route telemetry.
7. The result is stored under the idempotency key `clientEvidenceId`.

## RabbitMQ

- Exchange: `cobox.events`
- Routing key: `evidence.upload.confirmed`
- Queue: `ai.evidence-upload-confirmed`
- DLQ: `ai.evidence-upload-confirmed.dlq`

RabbitMQ is configured only for Docker/local/demo. There is no mandatory managed broker dependency for cloud in this cut.

## Analysis States

- `PENDING`
- `PROCESSING`
- `COMPLETED`
- `FAILED`
- `REVIEW_REQUIRED`
- `RECAPTURE_REQUIRED`
- `FRAUD_SUSPECTED`
- `DEGRADED`

## V1 Rules

- Blurry, low-light or illegible evidence -> `RECAPTURE_REQUIRED`.
- Ambiguous AI result -> `REVIEW_REQUIRED`.
- Missing telemetry -> `DEGRADED`, not automatic fraud.
- Strong mismatch between upload intent and offline edge metadata -> `FRAUD_SUSPECTED`.

## Public Read API

- `GET /api/v1/ai-validation/evidence-analyses/{clientEvidenceId}`
- `GET /api/v1/ai-validation/alerts`
- `GET /api/v1/ai-validation/alerts/status/{status}`

## Mini ADR

Decision: use AWS Textract/Rekognition as the AI provider for v1.

Impact in 12 weeks: medium. This adds AWS SDK/runtime configuration and new operational failure modes, but the domain rules are protected by the `AiVisionProvider` interface.

Cloud cost: low in controlled tests. RabbitMQ remains local/demo only, with no required cloud broker.

Risk: medium. The mitigation is keeping AWS-specific code inside `AwsAiVisionProvider`; domain rules and persistence depend on provider-neutral DTOs.

## Technical Debt

- No production broker strategy is selected yet; RabbitMQ is local/demo only.
- No outbox relay exists inside `ai-validation-service` because this cut persists alerts locally only.
- AWS Textract/Rekognition result parsing is intentionally basic and should be tuned with real evidence samples.
- Fraud rules are deterministic v1 heuristics, not calibrated statistical models.
- Alert lifecycle is read-only; acknowledge/resolve commands are deferred.
- No authorization by operations role is implemented yet beyond general JWT validation.

## Validation Commands

```bash
./mvnw -pl ai-validation-service -am test
./mvnw -pl ai-validation-service -am package
```
