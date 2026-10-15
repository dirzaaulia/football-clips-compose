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
- **`youtube_client.py`**: YouTube RSS feed parser and WebSub push listener utilizing official club and league channel feeds with **0 YouTube Data API quota spent**.
- **`gemini_client.py`**: High-precision AI re-ranker using **Google Gemini 3.8 Flash** with a sliding-window token bucket rate-limiter (12 RPM) and automatic heuristic fallback scoring (`fallback_score_video`).
- **`supabase_client.py`**: High-performance PostgREST client managing match storage, video link associations, and telemetry event streaming.
- **`edge_cache_client.py`**: Non-blocking client that automatically invalidates Cloudflare Edge Proxy cache upon linking a new match highlight.

### 2. Core Service Pipelines (`worker/services/`)
- **`match_service.py`**: Fixture and live score synchronization. Detects kickoffs (`IN_PLAY`), live goal updates, half-time, and full-time (`FINISHED`), with monotonic score regression protection.
- **`highlight_service.py`**: Scans official club/league feeds, filters noise (shorts, press conferences, fan reactions), scores candidates via Gemini 3.8 Flash, and verifies global embed playability. Supports instant event-driven match linking (`link_websub_video`) via WebSub push notifications in $<100\text{ms}$.
- **`notification_service.py`**: Dispatches real-time alerts across 5 match phases to Firebase Cloud Messaging (FCM) topics (`club_{club_id}`) with an in-memory 24-hour deduplication cache.
- **`standings_service.py`**: Synchronizes official league tables, goal differentials, and rankings.
- **`maintenance_service.py`**: Self-healing service that recovers matches stuck in `IN_PLAY` beyond 180 minutes and purges records older than 30 days.
- **`telemetry_service.py`**: Streams cycle metrics to Google Cloud Storage (`telemetry.json`) and powers the OpenMetrics Prometheus `/metrics` endpoint.

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
| `GET` | `/websub` | Google PubSubHubbub handshake challenge verification |
| `POST` | `/websub` | Real-time YouTube upload push receiver with instant match linking |
| `POST` | `/websub/renew` | Batch subscription renewal for all 96 club & league YouTube channels |

---

## ⏰ Google Cloud Scheduler Configuration (3 Free Cron Jobs)

Configured under Google Cloud's perpetual free tier (3 free jobs/month):

1. **`fc-worker-sync`**: `*/5 * * * *` (Every 5 minutes standard cycle).
2. **`fc-worker-weekend-live`**: `* 12-22 * * 6,0` (Every 1 minute during peak European weekend match window).
3. **`fc-worker-websub-renew`**: `0 3 * * 0` (Every Sunday at 03:00 UTC for automated WebSub subscription renewal).

---

## 🧪 Testing & Verification

The worker includes an extensive test suite verifying matching algorithms, regression guards, Gemini rate limits, and WebSub parsing:

```bash
# Run complete test suite (38/38 passing)
pytest worker/tests/

# Deploy container revision to Google Cloud Run
gcloud run deploy fc-worker --source ./worker --region us-central1 --project highlightdata --quiet
```
