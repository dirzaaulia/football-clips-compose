# 🛡️ Oracle VM Super-Node: Enterprise Infrastructure & Operations Manual

This comprehensive guide details the complete architecture, industry-grade tool evaluation, and step-by-step instructions for managing and utilizing every service hosted on our **Oracle Cloud Always Free Super-Node** (`footballclips-oracle`).

---

## 📑 Table of Contents
1. [Database Viewers & IDEs: NocoDB vs. DBeaver vs. DataGrip](#1-database-viewers--ides-nocodb-vs-dbeaver-vs-datagrip)
2. [Master Network Topology & Access Matrix](#2-master-network-topology--access-matrix)
3. [PostgreSQL 16 Enterprise Datastore](#3-postgresql-16-enterprise-datastore)
4. [PostgREST Universal REST API Tier](#4-postgrest-universal-rest-api-tier)
5. [Edge Delivery & Caddy Reverse Proxy Architecture](#5-edge-delivery--caddy-reverse-proxy-architecture)
6. [Golang Micro-Daemon (`fc-daemon.service`) Operations](#6-golang-micro-daemon-fc-daemonservice-operations)
7. [Domain Architecture & URL Directory](#7-domain-architecture--url-directory)
8. [Tailscale Mesh VPN & Zero-Trust SSH](#8-tailscale-mesh-vpn--zero-trust-ssh)
9. [FileBrowser Quantum (Cloud Storage Hub)](#9-filebrowser-quantum-cloud-storage-hub)
10. [Hybrid AI Gateway & Vibe Coding Suite (Qwen 2.5 Coder)](#10-hybrid-ai-gateway--vibe-coding-suite-qwen-25-coder)
11. [n8n Autonomous Workflow Orchestration](#11-n8n-autonomous-workflow-orchestration)
12. [Automated Disaster Recovery & Backblaze B2](#12-automated-disaster-recovery--backblaze-b2)
13. [System Maintenance, Health Monitoring & Diagnostics](#13-system-maintenance-health-monitoring--diagnostics)

---

## 1. Database Viewers & IDEs: NocoDB vs. DBeaver vs. DataGrip

When interacting with our PostgreSQL 16 database, selecting the right tool directly affects developer productivity, query optimization, and schema safety.

### Web GUI (In-Browser Super-Viewer): NocoDB
- **URL**: `https://db.fc.dirzaaulia.com` (or `http://footballclips-oracle:8082`)
- **Experience**: Supabase / Airtable-style modern spreadsheet view with instant filtering, sorting, relation links, and record editing.
- **Decommissioning Notice**: Legacy **pgAdmin 4** was officially decommissioned and archived. NocoDB replaced it, offering a vastly cleaner, responsive interface while consuming significantly fewer system resources.

### Desktop IDE Comparison: DBeaver vs. DataGrip

| Criteria | 🏆 **DataGrip (JetBrains)** | 🥇 **DBeaver Community** | ⚡ **NocoDB (Web GUI)** |
| :--- | :--- | :--- | :--- |
| **Classification** | Commercial Enterprise IDE | Open-Source Universal Standard | Modern Web Database / Airtable UI |
| **Cost** | Paid (JetBrains All Products Pack) | **$0.00 Free & Open-Source** | **$0.00 Free & Self-Hosted** |
| **Access Mode** | Desktop Application | Desktop Application | Browser Web GUI (`https://db...`) |
| **Autocompletion & Dialect** | Industry-leading contextual SQL engine | Very good (schema-aware) | Spreadsheet / Filter bar UI |
| **Schema Refactoring & DDL** | Automated migration diffs, smart rename | Manual DDL generators | Visual schema builder |
| **Execution Plan Visualizer** | Interactive visual tree with bottleneck warnings | Visual explain graph | N/A (Admin & Table View) |
| **Resource Usage** | Moderate memory footprint | Moderate (Java/Eclipse platform) | Ultra-light container (<150MB) |

---

## 2. Master Network Topology & Access Matrix

All services operate inside a unified zero-trust network overlay:

```mermaid
flowchart TD
    subgraph ClientDevices["📱 Developer & Client Layer"]
        DevPC["Developer Machine (via Tailscale)"]
        MobileApp["Android CMP App"]
        WebApp["Next.js Web App"]
    end

    subgraph EdgeLayer["⚡ Direct Public Edge (Caddy 2 Reverse Proxy)"]
        Caddy["Caddy 2 Reverse Proxy (TLS 1.3)<br/>edge.fc.dirzaaulia.com"]
    end

    subgraph OracleNode["🖥️ Oracle VM Super-Node (footballclips-oracle)"]
        TailscaleDaemon["Tailscale WireGuard Daemon"]
        FcDaemon["fc-daemon (Native Go Systemd Service)<br/>• 60s Live Sync<br/>• 5m Highlight Discovery<br/>• 30m Shorts Ingestion<br/>• :8094 WebSub Push Hub"]
        
        subgraph InternalDockerNet["Docker Bridge Network (jarvis_net)"]
            Postgres["PostgreSQL 16 (Port 5432)"]
            PostgREST["PostgREST (Port 3000)"]
            NocoDB["NocoDB (Port 8082)"]
            FileBrowser["FileBrowser (Port 8090)"]
            AIGateway["AI Gateway (Port 8080)"]
            Ollama["Ollama LLM (Port 11434)"]
            Whisper["Faster-Whisper (Port 8000)"]
            Qdrant["Qdrant Vector DB (Port 6333)"]
            n8n["n8n Workflows (Port 5678)"]
            Shield["Anti-Idle Shield"]
        end
    end

    DevPC -->|Tailscale WireGuard (Port 5432)| Postgres
    DevPC -->|Tailscale SSH (Port 22)| TailscaleDaemon
    DevPC -->|Tailscale HTTP (Ports 8080/5678/8090/8082)| InternalDockerNet

    MobileApp -->|HTTPS / REST| Caddy
    WebApp -->|HTTPS / REST| Caddy

    Caddy -->|Internal Reverse Proxy| PostgREST
    Caddy -->|Web UI Proxy| NocoDB
    Caddy -->|Web GUI Proxy| FileBrowser
    PostgREST -->|Internal SQL| Postgres
    FcDaemon -->|Direct pgxpool Binary Protocol| Postgres
```

### Access Matrix

| Service | Port | Local Tailscale Access | Public Access | Authentication Required |
| :--- | :--- | :--- | :--- | :--- |
| **PostgreSQL 16** | `5432` | `footballclips-oracle:5432` | ❌ Blocked (Zero public exposure) | PostgreSQL Password |
| **PostgREST API** | `3000` / `443` | `footballclips-oracle:3000` | 🌐 `https://edge.fc.dirzaaulia.com` | `web_anon` (Read-only) |
| **fc-daemon WebSub** | `8094` | `footballclips-oracle:8094` | 🌐 Via Caddy WebSub route | HMAC SHA1 Verification |
| **Tailscale SSH** | `22` | `ssh ubuntu@footballclips-oracle` | ❌ Blocked | Tailscale MFA / Web Auth |
| **NocoDB Web UI** | `8082` | `http://footballclips-oracle:8082` | `https://db.fc.dirzaaulia.com` | NocoDB Auth / Password |
| **FileBrowser** | `8090` | `http://footballclips-oracle:8090` | `https://files.fc.dirzaaulia.com` | Admin Password |
| **AI Gateway** | `8080` | `http://footballclips-oracle:8080` | ❌ Blocked | Bearer Token / Open on Mesh |
| **Ollama Inference** | `11434` | `http://footballclips-oracle:11434` | ❌ Blocked | Open on Tailscale Mesh |
| **n8n Automation** | `5678` | `http://footballclips-oracle:5678` | ❌ Blocked | n8n User Session |
| **Qdrant Vector** | `6333` | `http://footballclips-oracle:6333` | ❌ Blocked | Open on Tailscale Mesh |

---

## 3. PostgreSQL 16 Enterprise Datastore

### Configuration & Credentials
- **Host**: `footballclips-oracle` (via Tailscale)
- **Port**: `5432`
- **Database**: `footballclips`
- **Username**: `postgres`
- **Password**: `<POSTGRES_PASSWORD>` (Read from secure environment variable `POSTGRES_PASSWORD`)
- **Driver Type**: PostgreSQL standard JDBC / libpq

### Step-by-Step Connection Instructions

#### Option A: Connecting with DBeaver
1. Open DBeaver and click **New Database Connection** (plug icon with green plus).
2. Select **PostgreSQL** and click **Next**.
3. Fill in connection settings:
   - **Host**: `footballclips-oracle`
   - **Port**: `5432`
   - **Database**: `footballclips`
   - **Username**: `postgres`
   - **Password**: `<POSTGRES_PASSWORD>`
4. Click **Test Connection ...**. You will see:
   > Connected (PostgreSQL 16.x)
5. Click **Finish**.

#### Option B: Direct Terminal CLI (`psql`)
```bash
# Direct psql from local machine over Tailscale:
psql -h footballclips-oracle -U postgres -d footballclips

# From inside the Oracle VM:
ssh ubuntu@footballclips-oracle "docker exec -it jarvis-postgres psql -U postgres -d footballclips"
```

### Useful Management Queries
```sql
-- Check table counts
SELECT count(*) FROM matches;
SELECT count(*) FROM standings;
SELECT count(*) FROM supported_clubs;
SELECT count(*) FROM shorts;

-- View highlights linked in Season 2026/27
SELECT id, competition_id, home_team_name, away_team_name, highlight_video_id, utc_date 
FROM matches 
WHERE season = 2026 AND highlight_video_id IS NOT NULL 
ORDER BY utc_date DESC 
LIMIT 10;

-- Refresh PostgREST schema cache after manual schema edits
NOTIFY pgrst, 'reload schema';
```

---

## 4. PostgREST Universal REST API Tier

PostgREST sits directly on top of PostgreSQL, turning tables into a high-performance REST API with zero manual backend code.

### Endpoints
- **Production Edge PostgREST**: `https://edge.fc.dirzaaulia.com`

### How to Query PostgREST
```bash
# 1. Fetch 5 newest finished matches
curl -s "https://edge.fc.dirzaaulia.com/rest/v1/matches?status=eq.FINISHED&order=utc_date.desc&limit=5"

# 2. Filter matches for a specific league and season
curl -s "https://edge.fc.dirzaaulia.com/rest/v1/matches?competition_id=eq.PL&season=eq.2026&limit=10"

# 3. Fetch league standings for Serie A
curl -s "https://edge.fc.dirzaaulia.com/rest/v1/standings?competition_code=eq.SA&season=eq.2026&order=position.asc"

# 4. Fetch top trending shorts by Hype Score
curl -s "https://edge.fc.dirzaaulia.com/rest/v1/shorts?order=hype_score.desc&limit=10"
```

---

## 5. Edge Delivery & Caddy Reverse Proxy Architecture

All incoming client traffic routes directly through **Caddy 2**, providing:
- **Zero-Configuration HTTPS**: Automatic ACME certificate provisioning via Let's Encrypt with HTTP/2 and HTTP/3 support.
- **Header Sanitization**: Strips incoming authorization headers (`header_up -Authorization`) on `/rest/v1/*` so `web_anon` client queries execute without JWT errors.
- **Direct Origin Routing**: Reverse proxies directly to PostgREST 12 on port 3000, eliminating third-party edge hops.

---

## 6. Golang Micro-Daemon (`fc-daemon.service`) Operations

The backend operational pipelines run under a native Linux systemd service:

### Service Management
```bash
# View service status and resource utilization
sudo systemctl status fc-daemon

# Restart service after deployment
sudo systemctl restart fc-daemon

# Stream live operational logs
journalctl -u fc-daemon -f
```

### Operational Invariants & Schedules
- **Live Match Ingestion**: 60s tick querying ESPN API.
- **Highlight Discovery**: 5m tick evaluating finished matches against YouTube RSS and API.
- **Shorts Rotation**: 30m tick evaluating 50-club batches using 0-quota YouTube Atom RSS.
- **WebSub Push Hub**: Listening on port `8094` for real-time YouTube upload pushes.

---

## 7. Domain Architecture & URL Directory

| Target Service | Category | URL / Access Path | Access Scope |
| :--- | :--- | :--- | :--- |
| **FootballClips Web App** | Production Frontend | **`https://fc.dirzaaulia.com`** | Public Web (Firebase Hosting) |
| **Edge REST API** | Production Backend | **`https://edge.fc.dirzaaulia.com`** | Public Web (Caddy / PostgREST) |
| **Web Database Studio** | Database Admin | **`https://db.fc.dirzaaulia.com`** | Password Authenticated (NocoDB) |
| **Cloud Storage Hub** | Files / Backups | **`https://files.fc.dirzaaulia.com`** | Password Authenticated (FileBrowser) |
| **PostgreSQL 16 Engine** | Database Engine | **`footballclips-oracle:5432`** | Private WireGuard Mesh |
| **Ollama LLM Engine** | Local AI / Inference | **`http://footballclips-oracle:11434`** | Private WireGuard Mesh |
| **AI Gateway** | Unified AI API | **`http://footballclips-oracle:8080`** | Private WireGuard Mesh |
| **n8n Automation** | Workflow UI | **`http://footballclips-oracle:5678`** | Private WireGuard Mesh |
| **Zero-Trust SSH** | Server Terminal | **`ssh ubuntu@footballclips-oracle`** | Private WireGuard Mesh |

---

## 8. Tailscale Mesh VPN & Zero-Trust SSH

Connect to the VM without passwords or public SSH keys:
```powershell
ssh ubuntu@footballclips-oracle
```

---

## 9. FileBrowser Quantum (Cloud Storage Hub)

- **Over Public Web**: `https://files.fc.dirzaaulia.com`
- **Over Tailscale**: `http://footballclips-oracle:8090`
- **Username**: `admin`
- **Password**: `<FILEBROWSER_ADMIN_PASSWORD>` (Read from `.env`)

---

## 10. Hybrid AI Gateway & Vibe Coding Suite (Qwen 2.5 Coder)

Ampere A1 ARM64 cores run local models with zero token cost:
- `deepseek-coder-v2:16b` (8.9 GB MoE)
- `qwen2.5-coder:7b` (4.7 GB Dense)
- `phi4-mini:latest` (2.5 GB Dense)

---

## 11. n8n Autonomous Workflow Orchestration

- **Dashboard**: `http://footballclips-oracle:5678` (Tailscale)
- **Active Workflow**: `fcOpsMaintenance` running hourly at minute 7 (`7 * * * *`).
- **Scope**: Validates backup existence, compacts old logs, verifies NVMe disk health.

---

## 12. Automated Disaster Recovery & Backblaze B2

- **Nightly Backup**: Scheduled daily at `03:00 UTC` using `pg_dump` targeting `footballclips`.
- **Retention**: 14 days rolling snapshots in `/home/ubuntu/jarvis/storage/cloud/Backups/`.
- **Offsite Replication**: Synced to Backblaze B2 bucket (`footballclips-archives`) via `rclone`.

---

## 13. System Maintenance, Health Monitoring & Diagnostics

```powershell
# Health check for all Docker containers
ssh ubuntu@footballclips-oracle "docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'"

# Check native Go daemon
ssh ubuntu@footballclips-oracle "sudo systemctl status fc-daemon"
```
