# 🤖 FootballClips Autonomous AI Worker & Serverless Engine (v2.0)

Modern, asynchronous data synchronization engine and serverless APM platform for FootballClips running on Google Cloud Run (`fc-worker`) with zero-cost edge distribution via Cloudflare Workers.

---

## 🏛️ System Architecture & Services

The worker operates both as a **continuous orchestrator** and as an **event-driven serverless container** (`FastAPI` + `asyncio`):

```
                                  [Cloud Scheduler]
                                         │
                                         ▼ (HTTP POST /run-cycle)
[ESPN Sports API] ───────► [match_service.py] ──────► [Supabase DB (PostgreSQL)]
(0 Rate Limit, 0 Quota)          │                               ▲
                                 ▼                               │
                      [notification_service.py]                  │
                                 │                               │
                                 ▼ (FCM Topics: club_{id})       │
                       [Android & Web Push]                      │
                                                                 │
[YouTube WebSub Push] ───► [FastAPI /websub]                     │
[Official Channel RSS] ──► [highlight_service.py] ───────────────┤
                                 │ (Gemini 3.8 Flash AI)         │
                                 ▼                               ▼
                      [EdgeCacheClient (Purge)] ──► [Cloudflare Edge Proxy]
                                                    (fc-edge-proxy)
```

### 1. Client Modules (`worker/clients/`)
- **`espn_client.py`**: Zero-cost, resilient football score and fixture client querying ESPN public endpoints for Premier League, UEFA Champions League, La Liga, Serie A, Bundesliga, Ligue 1, Eredivisie, etc.
- **`youtube_client.py`**: YouTube Data API v3 client with 60,000 units/day approved quota, RSS feed parser, and WebSub push listener.
- **`ai_gateway_client.py`**: High-precision AI re-ranker using Oracle VM AI Gateway (DeepSeek-R1 / Ollama ARM64) with circuit-breaking fallback to **Google Gemini 2.5 Flash Lite** and deterministic mathematical scoring.
- **`cloud_tasks_client.py`**: Decoupled, asynchronous task dispatching to Google Cloud Tasks (`fc-highlight-queue`) for per-match fault isolation and multi-stage embargo scheduling (Stage 1: $T+15\text{m}$, Stage 2: $T+90\text{m}$).
- **`database_client.py` / `supabase_client.py`**: High-performance client managing PostgreSQL datastore, video link associations, and telemetry event streaming.
- **`edge_cache_client.py`**: Non-blocking client that automatically invalidates Cloudflare Edge Proxy cache upon linking a new match highlight.

### 2. Core Service Pipelines (`worker/services/`)
- **`match_service.py`**: Fixture and live score synchronization. Detects kickoffs (`IN_PLAY`), live goal updates, half-time, and full-time (`FINISHED`), with monotonic score regression protection and Cloud Tasks dispatching.
- **`single_highlight_processor.py`**: Isolated per-match highlight pipeline with 4-step waterfall diagnostic trace, regional fast-break catering (Indonesia `ID`), and automatic worldwide promotion.
- **`highlight_service.py`**: Scans official club/league feeds, filters noise, manages daily quota buckets (60,000 units/day), and verifies global embed playability. For the full technical breakdown, worldwide audience architecture, and multi-tier quota maximizer, see **[`PIPELINE.md`](./PIPELINE.md)**.
- **`notification_service.py`**: Dispatches real-time alerts across 5 match phases to Firebase Cloud Messaging (FCM) topics (`club_{club_id}`) with an in-memory 24-hour deduplication cache.
- **`standings_service.py`**: Synchronizes official league tables, goal differentials, and rankings.
- **`maintenance_service.py`**: Self-healing service that recovers matches stuck in `IN_PLAY` beyond 180 minutes and purges records older than 30 days.
- **`telemetry_service.py`**: Streams cycle metrics and waterfall traces to Google Cloud Storage (`telemetry.json`) and powers the OpenMetrics Prometheus `/metrics` endpoint.

---

## 🌐 Serverless HTTP Endpoints (`worker/server.py`)

Deployed to Google Cloud Run with `CORSMiddleware` enabled:

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` or `/dashboard` | Interactive responsive HTML Control Center & Node Health Dashboard |
| `GET` | `/health` | JSON node health status, provider attribution (`espn`), and target leagues |
| `GET` | `/status` | JSON sync cycle index, orchestrator state, and last sync timestamps |
| `GET` | `/metrics` | **OpenMetrics / Prometheus APM telemetry endpoint** (60s in-memory cache) |
| `POST` | `/run-cycle` | On-demand cycle execution trigger (`?async_execution=true&force_full=false`) |
| `POST` | `/sync/matches` | High-frequency fixture & live score sync with Cloud Tasks dispatching |
| `POST` | `/sync/highlights` | Highlight discovery and status sweep across all finished matches |
| `POST` | `/tasks/process-highlight` | **Decoupled Cloud Tasks handler** for isolated single-match highlight resolution |
| `GET` | `/websub` | Google PubSubHubbub handshake challenge verification |
| `POST` | `/websub` | Real-time YouTube upload push receiver with instant match linking |
| `POST` | `/websub/renew` | Batch subscription renewal for all 96 club & league YouTube channels |

---

## ⏰ Google Cloud Scheduler Configuration (Perpetual Free Tier)

Configured under Google Cloud's perpetual free tier:

1. **`fc-worker-sync`**: `* * * * *` (Every 1 minute live match and fixture sync).
2. **`fc-worker-highlights`**: `*/15 * * * *` (Every 15 minutes highlight discovery and cleanup sweep).
3. **`fc-highlight-queue` (Cloud Tasks)**: Decoupled per-match queue (2 tasks/sec, max 3 retries).

---

## 🧪 Testing & Verification

The worker includes an extensive test suite verifying matching algorithms, regression guards, Gemini rate limits, Cloud Tasks client, single highlight processor, and WebSub parsing:

```bash
# Run complete test suite (54/54 passing)
pytest worker/tests/

# Deploy container revision to Google Cloud Run
gcloud run deploy fc-worker --source ./worker --region us-central1 --project highlightdata --quiet
```
