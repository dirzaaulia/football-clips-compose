# 🤖 Football Clips Autonomous AI Worker Engine (v2.0)

Modern, modular, asynchronous data synchronization engine for FootballClips running continuously as a systemd service on Google Cloud Platform (`free-football-worker`).

---

## 🏛️ Architecture & Services

The worker is designed with clean service separation and asynchronous non-blocking I/O using Python 3.13 and `asyncio`:

- **`clients/`**:
  - `football_data_client.py`: Resilient client for Football-Data.org with adaptive throttling (10 requests/min ceiling) to avoid 429 rate limits.
  - `youtube_client.py`: Quota-managed client for YouTube Data API v3 with persistent daily quota tracking (10,000 units safety guard) and upload playlist RSS fallback.
  - `gemini_client.py`: Semantic video re-ranker using **Google Gemini 3.8 Flash / 3.7 Flash** with structured JSON output and season-aware match matching prompts.
  - `supabase_client.py`: Async Postgrest client managing database operations and telemetry event streams.

- **`services/`**:
  - **`notification_service.py`**: Dispatches real-time alerts across 5 key events (Goals, Kickoffs, Full-Time, Match Countdown, and Highlight releases) to FCM topics (`club_{club_id}`) at **$0.00 infrastructure cost**. Features an in-memory 24-hour event deduplicator preventing duplicate notifications.
  - **`match_service.py`**: Synchronizes fixtures, live scores, and statuses without regressing live scores. Triggers real-time goal alerts and kickoff notifications.
  - **`highlight_service.py`**: Scans official club/league YouTube channels, applies title year and duration filters, filters out press conferences/podcasts via Gemini AI, and links verified recaps.
  - **`club_service.py`**: Auto-syncs Big 5 league clubs to `supported_clubs` table (zero manual maintenance across football seasons).
  - **`standings_service.py`**: Updates league standings tables and goal difference statistics.
  - **`maintenance_service.py`**: Performs automated rolling 30-day cleanup of old matches, highlights, and telemetry events to keep database storage optimal.
  - **`telemetry_service.py`**: Emits cycle summaries (`worker_cycles`) and detailed step logs (`worker_events`) consumed directly by the Kotlin/Wasm Telemetry dashboard.

---

## 🛠️ Management via `uv`

The worker utilizes Astral's [`uv`](https://docs.astral.sh/uv/) for high-speed package management and virtual environment execution.

```bash
# Install and synchronize dependencies
uv sync

# Run the complete test suite
uv run pytest

# Execute a single test module
uv run pytest tests/test_notification_service.py -v

# Run worker manually
uv run python main.py
```

---

## ☁️ Production VM Deployment (GCP Compute Engine)

The worker is hosted on the GCP VM instance `free-football-worker` (Zone: `us-central1-a`, Project: `highlightdata`) managed by `systemd`.

### Service Status & Logs

```bash
# Check service status
sudo systemctl status football-worker.service

# View live service logs
sudo journalctl -u football-worker.service -f

# Restart worker service
sudo systemctl restart football-worker.service
```

### Systemd Configuration (`/etc/systemd/system/football-worker.service`)

```ini
[Unit]
Description=FootballClips Modern Async Worker v2.0
After=network.target

[Service]
Type=simple
User=dirzaaulia11
WorkingDirectory=/home/dirzaaulia11/worker
ExecStart=/home/dirzaaulia11/.local/bin/uv run main.py
Restart=always
RestartSec=10
StandardOutput=append:/home/dirzaaulia11/sync_log.txt
StandardError=append:/home/dirzaaulia11/sync_log.txt

[Install]
WantedBy=multi-user.target
```
