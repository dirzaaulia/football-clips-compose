# 🛡️ Oracle Always Free Autonomous Node (Jarvis Super-Node)

High-performance, zero-dollar self-hosted super-node deployed on an **Oracle Cloud Infrastructure (OCI) Always Free ARM64 Ampere A1** instance (4 OCPUs, 24 GB RAM, 200 GB NVMe storage).

---

## 🏗️ Architecture Overview

The node operates a unified Docker Compose multi-service topology under a zero-trust network model:

1. **Caddy Reverse Proxy (`jarvis-caddy`)**:
   - Automatic TLS certificate issuance & renewal via Let's Encrypt / ACME.
   - Dynamic domain routing via `sslip.io` wildcards (`*.168.110.196.30.sslip.io`), `edge.fc.dirzaaulia.com`, and `api.fc.dirzaaulia.com`.
   - Header sanitization (`header_up -Authorization` on `/rest/v1/*`) ensuring `web_anon` clients bypass Supabase JWT errors.
   - Forward-auth gating for `/pgadmin/*` via `footballclips-operations:8093`.
2. **FootballClips Operations Service (`footballclips-operations`)**:
   - High-throughput FastAPI core operations microservice running on port `8093`.
   - Serves modern operations endpoints (`/api/v2/*`, `/api/ops/v1/*`, `/api/status/v1`), cycle telemetry, and mutation protections.
   - Exposes `/internal/maintenance` triggered by n8n for automated backup integrity, log compaction, and disk storage metrics.
   - Exposes `/internal/pgadmin-auth` forward-authentication proxy for database GUI sessions.
3. **Autonomous Action Hub (`jarvis-n8n`)**:
   - Event orchestration and workflow engine running on port `5678` (`n8nio/n8n:2.43.6`).
   - Production Workflow: `fcOpsMaintenance` ("FootballClips — hourly maintenance verification").
   - Schedule Trigger: Executes hourly at minute 7 (`7 * * * *`) + manual owner trigger.
   - Dispatches authenticated HTTP requests to `http://footballclips-operations:8093/internal/maintenance` ensuring automated health and storage validation.
4. **Enterprise PostgreSQL 16 (`jarvis-postgres`)**:
   - Core production datastore running on Alpine Linux with high-concurrency tuning.
   - Hosts 3,420+ Season 2025/26 archive matches and active Season 2026/27 matches.
   - Enforces `protect_archive_matches` immutability trigger preventing unintended retention deletion of historical archives.
   - Nightly database dumps via `scripts/backup_postgres.sh`.
5. **Database Web Administration (`footballclips-pgadmin`)**:
   - Secure web-based GUI for PostgreSQL 16 exposed at `https://168.110.196.30.sslip.io/pgadmin/`.
   - Never exposes raw database ports; zero-trust session gating handled by Caddy forward-auth.
6. **PostgREST Universal REST API (`jarvis-postgrest`)**:
   - Direct, high-throughput REST API layer mirroring Supabase endpoints.
   - Directly queried by Cloudflare Edge Proxy (`edge.fc.dirzaaulia.com`) for match, standing, and fixture queries.
   - Secured behind Caddy reverse proxy at `https://168.110.196.30.sslip.io`.
7. **FileBrowser Quantum & Texture CDN (`jarvis-filebrowser`)**:
   - Actively maintained community fork of FileBrowser (`gtstef/filebrowser`) on port `8090`.
   - Web GUI exposed via HTTPS at `https://files.168.110.196.30.sslip.io` and `https://files.fc.dirzaaulia.com`.
   - Serves cached country texture maps (`/countries/textures/eyes-v1/*`) with immutable 1-year cache headers.
8. **Backblaze B2 Sync Subsystem (`scripts/sync_backblaze.sh`)**:
   - Automated offsite disaster recovery and cloud file synchronization via `rclone` scheduled daily at `03:00 UTC`.
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

## 🚀 Quick Setup & Deployment

1. **Environment Variables**:
   Copy `.env.example` to `.env` and configure credentials:
   ```bash
   FILEBROWSER_ADMIN_PASSWORD=your_secure_password
   B2_APPLICATION_KEY_ID=your_backblaze_key_id
   B2_APPLICATION_KEY=your_backblaze_app_key
   B2_BUCKET_NAME=your_backblaze_bucket
   ```

2. **Start Services**:
   ```bash
   docker compose up -d
   ```

3. **Verify Health**:
   ```bash
   docker ps
   curl -k -I https://files.168.110.196.30.sslip.io
   curl -k -I https://168.110.196.30.sslip.io
   ```
