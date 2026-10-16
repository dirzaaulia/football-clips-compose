# 🛡️ Oracle Always Free Autonomous Node (Jarvis Super-Node)

High-performance, zero-dollar self-hosted super-node deployed on an **Oracle Cloud Infrastructure (OCI) Always Free ARM64 Ampere A1** instance (4 OCPUs, 24 GB RAM, 200 GB NVMe storage).

---

## 🏗️ Architecture Overview

The node operates a unified Docker Compose multi-service topology under a zero-trust network model:

1. **Caddy Reverse Proxy (`jarvis-caddy`)**:
   - Automatic TLS certificate issuance & renewal via Let's Encrypt / ACME.
   - Dynamic domain routing via `sslip.io` wildcards (`*.168.110.196.30.sslip.io`).
2. **FileBrowser Quantum (`jarvis-filebrowser`)**:
   - Actively maintained, high-performance community fork of FileBrowser (`gtstef/filebrowser`).
   - Web GUI exposed via HTTPS at `https://files.168.110.196.30.sslip.io`.
   - Local directory mounts for `Cloud Storage`, `Backblaze B2`, and `Automated Backups`.
3. **Enterprise PostgreSQL 16 (`jarvis-postgres`)**:
   - Core production datastore running on Alpine Linux with high-concurrency tuning.
   - Nightly database dumps via `scripts/backup_postgres.sh`.
4. **PostgREST Universal REST API (`jarvis-postgrest`)**:
   - Direct, high-throughput REST API layer mirroring Supabase endpoints.
   - Secured behind Caddy reverse proxy at `https://168.110.196.30.sslip.io`.
5. **Backblaze B2 Sync Subsystem (`scripts/sync_backblaze.sh`)**:
   - Automated offsite disaster recovery and cloud file synchronization via `rclone`.
   - Scheduled daily via cron at `03:00 UTC`.
6. **AI Intelligence Engine & Semantic Memory**:
   - **Ollama ARM64** (`jarvis-ollama`): Local LLM inference (Llama 3, DeepSeek, Qwen).
   - **Speaches / Faster-Whisper Large-v3-Turbo** (`jarvis-whisper`): Speech-to-text audio perception.
   - **FastAPI AI Gateway** (`jarvis-gateway`): Multi-provider proxy router.
   - **Qdrant Vector Database** (`jarvis-qdrant`): Semantic vector search and embeddings.
   - **n8n Workflow Hub** (`jarvis-n8n`): Autonomous event triggers and automation.
7. **Oracle Anti-Idle Shield (`jarvis-oracle-shield`)**:
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
