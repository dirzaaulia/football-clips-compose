# 🛡️ FootballClips Production Operations Runbook

## 1. System Access Points & Endpoints

| Resource | URL / Address | Authentication & Access Scope |
| :--- | :--- | :--- |
| **Production Web App** | `https://fc.dirzaaulia.com` | Public HTTPS (Firebase Hosting CDN) |
| **Telemetry & Ops Console** | `https://fc.dirzaaulia.com/telemetry` | Google OAuth2 Restricted (`dirzaaulia11@gmail.com`) |
| **Edge PostgREST API** | `https://edge.fc.dirzaaulia.com` | Public HTTPS via Caddy 2 Reverse Proxy (`web_anon`) |
| **Web Database Studio (NocoDB)** | `https://db.fc.dirzaaulia.com` | Password / SSO Authenticated |
| **Cloud Storage Hub (FileBrowser)** | `https://files.fc.dirzaaulia.com` | Admin Authenticated |
| **Direct VM Shell (Tailscale SSH)** | `ssh ubuntu@footballclips-oracle` | Tailscale Zero-Trust WireGuard Mesh |

---

## 2. Infrastructure Topology & Execution Architecture

All operations run on an **Oracle Cloud Infrastructure (OCI) Always Free ARM64 Ampere A1** super-node (4 OCPUs, 24 GB RAM, 200 GB NVMe):

| Component | Technology | Responsibility |
| :--- | :--- | :--- |
| **`fc-daemon.service`** | Golang 1.23+ Systemd Service | Ingests live match scores (60s), finished highlights (5m), YouTube shorts (30m), and WebSub callbacks (:8094). |
| **PostgreSQL 16** | Docker Container (`jarvis-postgres`) | Canonical ACID relational datastore with immutable historical archive trigger (`protect_archive_matches`). |
| **PostgREST 12** | Docker Container (`jarvis-postgrest`) | Direct high-throughput HTTP-to-SQL REST gateway querying PostgreSQL on port 3000. |
| **Caddy 2** | Docker Container (`jarvis-caddy`) | Automatic Let's Encrypt TLS 1.3 reverse proxy, edge header sanitization (`header_up -Authorization`). |
| **NocoDB** | Docker Container (`jarvis-nocodb`) | Airtable-like web spreadsheet database GUI at `https://db.fc.dirzaaulia.com` (replaces legacy pgAdmin). |
| **AI Gateway & Ollama** | Docker Container (`jarvis-gateway`, `jarvis-ollama`) | ARM64 on-device LLMs (Qwen 2.5 Coder 7B, DeepSeek Coder 16B) for candidate highlight reranking. |
| **Disaster Recovery** | System Cron & `rclone` | Nightly `pg_dump` compressed backups with 14-day rolling retention and Backblaze B2 replication. |

---

## 3. Managing the Go Micro-Daemon (`fc-daemon`)

The core engine runs as a native systemd unit on the host OS for minimal memory footprint (<20 MB) and direct hardware execution.

### Essential Systemd Commands
```bash
# Check daemon health, uptime, and memory usage
sudo systemctl status fc-daemon

# Follow live structured logs in real-time
journalctl -u fc-daemon -f

# Follow logs with JSON or high-severity filtering
journalctl -u fc-daemon -n 100 --no-pager

# Restart daemon after updating binary
sudo systemctl restart fc-daemon

# Stop daemon during database maintenance
sudo systemctl stop fc-daemon
```

### Binary Deployment & Location
- **Executable Path**: `/home/ubuntu/footballclips-operations/core-go/bin/fc-daemon`
- **Systemd Unit File**: `/etc/systemd/system/fc-daemon.service`
- **Environment File**: `/etc/footballclips/fc-daemon.env`

---

## 4. Pipeline Schedules & Quota Protection

### A. Live Fixtures & Score Sync (60s Tick)
* Polled every 60 seconds from ESPN API.
* Main-safe concurrent processing using Go channels.
* Does NOT persist bulky match telemetry or coordinates into PostgreSQL; data is streamed directly to clients.
* Dispatches Firebase Cloud Messaging (FCM) v1 push notifications on kickoff and score changes.

### B. YouTube Highlight Discovery (5m Tick)
* Evaluates finished matches within the 4-tier cooldown matrix:
  * **Tier 1 (30m after FT)**: Official club channels.
  * **Tier 2 (2h after FT)**: Official league broadcasters.
  * **Tier 3 (6h after FT)**: Continental & global highlights.
  * **Tier 4 (24h after FT)**: Terminal fallback.
* Discovery prioritizes **Zero-Quota Atom XML RSS** feeds before consuming YouTube Data API v3 quota.
* **Target Market Geo-Cutoff**: When a valid highlight is verified that satisfies target market rules (either domestic club country or worldwide watchability $\ge 80\%$), further API search is terminated immediately.

### C. Football Shorts Ingestion (30m Tick)
* Automatically cycles through all 530+ supported clubs in batches of 50 via round-robin.
* Consumes **0 units of YouTube Data API quota** by reading official YouTube channel Atom RSS feeds (`https://www.youtube.com/feeds/videos.xml?channel_id=...`).
* Dynamically calculates **Hype Score (10–100)**:
  * Base: 50
  * Top-tier clubs (Real Madrid, Barca, Man City, Arsenal, Inter, Juve, etc.): $+25$
  * Derby / Rivalry matches: $+15$
  * Decay: $-5$ per day of age.

---

## 5. Telemetry Hub & Verification

1. Navigate to **`https://fc.dirzaaulia.com/telemetry`**.
2. Sign in with the authorized Google owner account (`dirzaaulia11@gmail.com`).
3. **Overview Pane**: Real-time pipeline state, database disk space, and API quota utilization.
4. **Waterfall Inspection**: Click on any finished match to view the granular candidate evaluation tree:
   * Channels queried.
   * Discovered candidate videos with source badge (`Zero-Quota RSS`, `WebSub Push`, or `YouTube Data API`).
   * AI Evaluation verdicts (`ACCEPTED` vs `REJECTED`) with explicit reasoning.
   * Final linked video duration and worldwide watchability score.

---

## 6. Disaster Recovery & Database Backups

* **Nightly Automated Backup**: Scheduled at `03:00 UTC` via `/home/ubuntu/jarvis/scripts/backup_postgres.sh`.
* **Local Storage Path**: `/home/ubuntu/jarvis/storage/cloud/Backups/` (retained for 14 days).
* **Offsite Replication**: Synced to Backblaze B2 encrypted bucket (`footballclips-archives`) via `sync_backblaze.sh`.
* **Testing Backup Integrity**:
  ```bash
  # Check latest backup archive size and integrity
  ls -lh /home/ubuntu/jarvis/storage/cloud/Backups/ | tail -n 5
  ```

---

## 7. Operational Invariants

1. **Never Purge Canonical Archives**: Seasons $< 2026$ are protected by the `protect_archive_matches` PostgreSQL trigger. Never disable this trigger or attempt bulk deletions.
2. **Zero Plaintext Secrets**: Never commit passwords, tokens, or production host IPs into source control. Always read credentials via environment variables (`ENV['...']`).
3. **Main-Safe I/O**: Network requests and database queries must run under cancellation contexts with timeouts (10s default).
