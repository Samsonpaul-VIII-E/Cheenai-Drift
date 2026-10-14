# Implementation Plan: Bulk Target Ingestion & CRM Webhook Sync Engine

LeadForge X is being upgraded with two enterprise capabilities: **Bulk Target Ingestion & Batch Queue Processor** and **Automated CRM Webhook Sync Engine**, adhering to our Dark Command Center UI theme (#080C14 background, Cold Laser Cyan #00E5FF, Tactical Emerald #10B981).

---

## 1. User Choices & Directives
Based on the clarifying answers:
- **Bulk Queue Processing Strategy**: **Strict sequential queue processing one domain at a time** with safe rate-limiting delays between targets to prevent network congestion, DNS exhaustion, or target rate-limiting.
- **Webhook Dispatch Policy**: **Immediately upon qualifying tier extraction and on demand** (plus manual one-click lead sync and test ping).

---

## 2. Proposed Architecture & System Design

### A. Feature 1: Bulk Target Ingestion & Batch Queue Processor
1. **Data Model & Room Persistence**:
   - `BatchQueueItem`: Tracks `id`, `batchId`, `domain`, `originalInput`, `status` (`PENDING`, `IN_PROGRESS`, `COMPLETED`, `FAILED`, `CANCELLED`), `startedAt`, `completedAt`, `httpStatus`, `latencyMs`, `leadsFoundCount`, `errorMessage`.
   - `BatchJobSummary`: Aggregates batch-level metrics (total, completed, failed, paused, duration, average latency).
2. **Pre-flight Sanitizer**:
   - Parses mixed inputs: newline-delimited, comma-delimited, whitespace, or CSV/text format.
   - Normalizes domains (removes `http://`, `https://`, trailing slashes, path queries).
   - Validates against Zero-Trust SSRF Shield (rejects loopback, internal 10.x, 192.168.x, 172.16-31.x, link-local, AWS metadata 169.254.x).
   - De-duplicates input against itself and existing database domains.
   - Reports pre-flight validation metrics: "X valid, Y duplicates removed, Z blocked by SSRF shield".
3. **Queue Execution Engine (`BatchQueueProcessor`)**:
   - Coroutine-driven sequential consumer running on `Dispatchers.IO`.
   - Execution lifecycle: `startQueue()`, `pauseQueue()`, `resumeQueue()`, `cancelQueue()`, `clearFinished()`.
   - Respects robots.txt, politeness delays (500ms - 1500ms throttle), and zero-trust security checks.
   - Live telemetry: Emits real-time batch progress state (percent, RPS/throughput, latency gauge, active domain, completed/failed/total counts).

### B. Feature 2: Automated CRM Webhook Sync Engine
1. **Configuration & Data Persistence**:
   - `WebhookConfig`: Persisted in Room / DataStore: `webhookUrl`, `bearerToken`, `minTierThreshold` (`TIER_A` [≥90%], `TIER_B` [≥70%], `ALL` [≥0%]), `autoSyncEnabled`.
   - `WebhookDeliveryLog`: Records each dispatch event: `timestamp`, `targetDomain`, `leadId`, `endpointUrl`, `httpStatusCode`, `success`, `latencyMs`, `retryAttempts`, `errorMessage`, `payloadPreview`.
2. **Dispatcher Engine (`WebhookDispatcher`)**:
   - Uses `OkHttp` with structured JSON payloads:
     ```json
     {
       "event": "lead.extracted",
       "lead_id": 104,
       "domain": "wikimedia.org",
       "company_name": "Wikimedia Foundation",
       "tier": "TIER_A",
       "score": 96,
       "emails": ["press@wikimedia.org"],
       "phones": ["+1-415-839-6885"],
       "social_links": {"wikipedia": "https://en.wikipedia.org/wiki/Wikimedia_Foundation"},
       "location": "San Francisco, CA, USA",
       "extracted_at": 1728900000000,
       "source": "LeadForge X Autonomous Public Scanner"
     }
     ```
   - Standardized authentication headers (`Authorization: Bearer <token>`, `Content-Type: application/json`, `User-Agent: LeadForge-Webhook/2.0`).
   - Exponential backoff retry logic: up to 3 retry attempts (1s, 2s, 4s backoff) for transient network timeouts or 5xx HTTP server responses.
   - Trigger points:
     - Automatic on qualifying tier extraction during both single crawl missions and sequential bulk queue batches.
     - On-demand "Sync to CRM" button on lead cards.
     - "Send Test Webhook" diagnostic ping in Webhook Operations panel.
3. **Audit & Verification**:
   - Records every attempt in the Room `WebhookDeliveryLog` and adds audit events to the Audit Ledger.

---

## 3. User Review Required
> [!NOTE]
> All bulk targets will run sequentially one at a time as requested, guaranteeing zero socket exhaustion and compliance with web target politeness.
> The webhook dispatcher will run asynchronously on IO coroutines so extraction velocity is never held up by slow external webhook endpoints.

---

## 4. Proposed Changes by File

### Data Layer
- **`app/src/main/java/com/example/leadforge/data/Models.kt`**:
  - Add `BatchQueueItem`, `BatchItemStatus`, `BatchQueueState`, `BatchPreflightResult`.
  - Add `WebhookConfig`, `WebhookDeliveryLog`, `WebhookTestResult`.
- **`app/src/main/java/com/example/leadforge/data/LeadForgeDao.kt`**:
  - Add DAOs for `BatchQueueItem` (insert, update, query pending/active, clear) and `WebhookDeliveryLog` (insert, query recent 50 logs).
- **`app/src/main/java/com/example/leadforge/data/LeadForgeDatabase.kt`**:
  - Register new entities (`BatchQueueItem`, `WebhookDeliveryLog`) with version bump and migration strategy.
- **`app/src/main/java/com/example/leadforge/data/WebhookDispatcher.kt`** *(New)*:
  - OkHttp JSON payload builder, authentication header manager, exponential backoff retry executor, and test ping routine.
- **`app/src/main/java/com/example/leadforge/data/LeadForgeRepository.kt`**:
  - Integrate `WebhookDispatcher`, batch queue persistence, preflight parsing, and live telemetry hooks.

### ViewModel & State Management
- **`app/src/main/java/com/example/leadforge/ui/LeadForgeViewModel.kt`**:
  - Add state flows for `bulkInputText`, `preflightResult`, `activeBatchState`, `batchQueueItems`.
  - Add state flows for `webhookConfig`, `isTestingWebhook`, `webhookTestStatus`, `webhookLogs`.
  - Add functions: `parseBulkInput()`, `startBatchQueue()`, `pauseBatchQueue()`, `resumeBatchQueue()`, `cancelBatchQueue()`, `saveWebhookConfig()`, `sendTestWebhook()`, `manualSyncLeadToWebhook()`.
  - Wire automatic webhook trigger into lead insertion when tier criteria is satisfied.

### UI & Dark Command Center Screens
- **`app/src/main/java/com/example/leadforge/ui/screens/NewJobScreen.kt`**:
  - Add top tab switcher: `[ Single Mission | Bulk Ingestion Queue ]`.
  - Render Bulk Queue pane:
    - Multiline target input field with syntax hints and "Load Preset Batch" button.
    - Preflight status metrics bar (Valid targets, duplicates filtered, SSRF screened).
    - Live Batch Telemetry dashboard: Progress bar, Latency / RPS gauge, status badges, Sequential execution table, Pause / Resume / Cancel controls.
- **`app/src/main/java/com/example/leadforge/ui/screens/AuditHealthScreen.kt`**:
  - Add "CRM Webhook Operations & Automation" Card:
    - Webhook URL input with URL validation.
    - Bearer Token / API Key field with toggleable visibility.
    - Minimum Tier threshold selector: `[ Tier A Only (≥90%) | Tiers A & B (≥70%) | All Leads ]`.
    - Auto-Sync enabled switch.
    - "Send Test Webhook" button with live HTTP status pill and latency readout.
    - Webhook Delivery Ledger: Recent 10 delivery attempts with status codes, retry counts, and payloads.
- **`app/src/main/java/com/example/leadforge/ui/screens/LeadsScreen.kt`**:
  - Add "Sync to CRM" quick-action button on each lead card for immediate manual dispatch.
- **`app/src/main/java/com/example/leadforge/ui/components/Components.kt`**:
  - Add UI elements: `TelemetryGaugeCard`, `BatchItemRow`, `WebhookStatusBadge`.

### Testing
- **`app/src/test/java/com/example/LeadForgeCoreUnitTest.kt`**:
  - Add unit tests for:
    - Bulk input pre-flight parsing and SSRF / duplicate rejection.
    - Webhook payload serialization and tier threshold validation.
    - Batch queue status transition logic.

---

## 5. Verification Plan

### Automated Tests
1. Run `gradle :app:testDebugUnitTest` to verify preflight sanitizer logic, duplicate detection, webhook payload schema, and retry policies.
2. Compile verification via `compile_applet`.

### Manual & Interactive Verification
1. **Bulk Target Ingestion**:
   - Open New Target screen -> Switch to "Bulk Queue".
   - Click "Load Preset Batch" to populate public research/science domains.
   - Verify pre-flight validation badges update with exact counts.
   - Click "Launch Batch Queue" -> observe strict sequential execution one domain at a time.
   - Verify live progress bar, completed counter, latency gauge, and item status transitions.
   - Test "Pause", "Resume", and "Cancel".
2. **CRM Webhook Sync**:
   - Open Doctor / Settings screen -> navigate to "Webhook Operations".
   - Enter a webhook endpoint URL (or use mock test endpoint) and bearer token.
   - Click "Send Test Webhook" -> confirm immediate HTTP status result and response time.
   - Adjust Tier threshold (e.g., Tier A Only) and save.
   - Execute a mission or trigger manual lead sync -> verify delivery logged in the Webhook Delivery Ledger and Audit Ledger.
