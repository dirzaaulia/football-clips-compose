# 🛡️ Oracle Always Free Autonomous Node (Jarvis Super-Node)

High-performance, zero-dollar self-hosted super-node deployed on an **Oracle Cloud Infrastructure (OCI) Always Free ARM64 Ampere A1** instance (4 OCPUs, 24 GB RAM, 200 GB NVMe storage).

---

## 🏗️ Architecture Overview

The node operates a unified Docker Compose multi-service topology alongside native host systemd micro-daemons under a zero-trust network model:

1. **Caddy Reverse Proxy (`jarvis-caddy`)**:
   - Automatic TLS certificate issuance & renewal via Let's Encrypt / ACME.
   - Dynamic domain routing via `edge.fc.dirzaaulia.com` and `api.fc.dirzaaulia.com`.
   - Header sanitization (`header_up -Authorization` on `/rest/v1/*`) ensuring `web_anon` clients bypass Supabase JWT errors.
   - Direct HTTPS reverse proxy to PostgREST 12, NocoDB, and FileBrowser.
2. **FootballClips Go Micro-Daemon (`fc-daemon.service`)**:
   - High-throughput Golang 1.23+ operations daemon running as a native host systemd service.
   - Orchestrates 60s live match polling (ESPN), 5m highlight discovery (multi-tier RSS & YouTube API), 30m shorts ingestion (50-club round-robin, Hype Score 10–100), and WebSub Push Receiver (:8094).
   - Ultra-lightweight footprint (<20 MB RAM) with zero garbage collection spikes.
3. **Autonomous Action Hub (`jarvis-n8n`)**:
   - Event orchestration and workflow engine (`n8nio/n8n:2.43.6`).
   - Production Workflow: `fcOpsMaintenance` ("FootballClips — hourly maintenance verification").
   - Schedule Trigger: Executes hourly at minute 7 (`7 * * * *`) + manual owner trigger.
   - Dispatches health telemetry, disk storage verification, and backup validation.
4. **Enterprise PostgreSQL 16 (`jarvis-postgres`)**:
   - Core production datastore running on Alpine Linux with high-concurrency tuning.
   - Hosts 3,420+ Season 2025/26 archive matches and active Season 2026/27 matches.
   - Enforces `protect_archive_matches` immutability trigger preventing unintended retention deletion of historical archives.
   - Nightly database dumps via host backup script.
5. **Database Web Administration (`jarvis-nocodb`)**:
   - Modern Airtable-like web spreadsheet GUI at `https://db.fc.dirzaaulia.com`.
   - Secure web-based inspection and filtering for PostgreSQL 16 (replaces legacy pgAdmin 4).
6. **PostgREST Universal REST API (`jarvis-postgrest`)**:
   - Direct, high-throughput REST API layer mirroring Supabase endpoints.
   - Directly queried by Edge Reverse Proxy (`edge.fc.dirzaaulia.com`) for match, standing, fixture, and shorts queries.
   - Secured behind Caddy reverse proxy at `https://edge.fc.dirzaaulia.com`.
7. **FileBrowser Quantum & Texture CDN (`jarvis-filebrowser`)**:
   - Actively maintained community fork of FileBrowser (`gtstef/filebrowser`).
   - Web GUI exposed via HTTPS at `https://files.fc.dirzaaulia.com`.
   - Serves cached country texture maps (`/countries/textures/eyes-v1/*`) with immutable 1-year cache headers.
8. **Disaster Recovery Subsystem**:
   - Automated offsite disaster recovery and cloud file synchronization via `rclone` to Backblaze B2 scheduled daily at `03:00 UTC`.
9. **AI Intelligence Engine & Semantic Memory**:
   - **Ollama ARM64** (`jarvis-ollama`): Local LLM inference (Llama 3, DeepSeek, Qwen).
   - **Speaches / Faster-Whisper Large-v3-Turbo** (`jarvis-whisper`): Speech-to-text audio perception.
   - **FastAPI AI Gateway** (`jarvis-gateway`): Multi-provider proxy router (`llm.dirzaaulia.com`).
   - **Qdrant Vector Database** (`jarvis-qdrant`): Semantic vector search and embeddings.
10. **Oracle Anti-Idle Shield (`jarvis-oracle-shield`)**:
    - Continuous background activity simulator preventing Oracle Always Free 7-day idle reclamation.

---

## 📂 Storage Directory Structure

```
/home/ubuntu/jarvis/storage/cloud/
├── Backblaze/       # Synced Backblaze B2 bucket storage
├── Backups/         # Database dumps (PostgreSQL) and system states
├── Documents/       # Document repository (PDFs, guides, notes)
├── Media/           # Media files (video highlights, audio, posters)
└── Projects/        # Project repositories, archives, and build artifacts
```

---

## 🚀 Quick Management Commands

```bash
# Check status of native Go daemon
sudo systemctl status fc-daemon

# View live daemon logs
journalctl -u fc-daemon -f

# Check status of all Docker services
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"

# Verify HTTPS endpoints
curl -k -I https://edge.fc.dirzaaulia.com/rest/v1/matches?limit=1
curl -k -I https://files.fc.dirzaaulia.com
curl -k -I https://db.fc.dirzaaulia.com
```
