# 🛡️ Oracle VM Super-Node: Enterprise Infrastructure & Operations Manual

This comprehensive guide details the complete architecture, industry-grade tool evaluation, and step-by-step instructions for managing and utilizing every service hosted on our **Oracle Cloud Always Free Super-Node** (`footballclips-oracle`).

---

## 📑 Table of Contents
1. [Database IDE Evaluation: DataGrip vs. DBeaver vs. pgAdmin](#1-database-ide-evaluation-datagrip-vs-dbeaver-vs-pgadmin)
2. [Master Network Topology & Access Matrix](#2-master-network-topology--access-matrix)
3. [PostgreSQL 16 Enterprise Datastore](#3-postgresql-16-enterprise-datastore)
4. [PostgREST Universal REST API Tier](#4-postgrest-universal-rest-api-tier)
5. [Cloudflare Anycast Edge Proxy (`fc-edge-proxy`)](#5-cloudflare-anycast-edge-proxy-fc-edge-proxy)
6. [Domain Architecture & Human-Friendly URL Matrix](#6-domain-architecture--human-friendly-url-matrix)
7. [Tailscale Mesh VPN & Zero-Trust SSH](#7-tailscale-mesh-vpn--zero-trust-ssh)
8. [FileBrowser Quantum (Cloud Storage Hub)](#8-filebrowser-quantum-cloud-storage-hub)
9. [Hybrid AI Gateway & Vibe Coding Suite (Qwen 2.5 Coder)](#9-hybrid-ai-gateway--vibe-coding-suite-qwen-25-coder)
10. [n8n Autonomous Workflow Orchestration](#10-n8n-autonomous-workflow-orchestration)
11. [Automated Disaster Recovery & Backblaze B2](#11-automated-disaster-recovery--backblaze-b2)
12. [System Maintenance, Health Monitoring & Diagnostics](#12-system-maintenance-health-monitoring--diagnostics)

---

## 1. Database IDE Evaluation: DataGrip vs. DBeaver vs. pgAdmin

When interacting with our PostgreSQL 16 database, selecting the right tool directly affects developer productivity, query optimization, and schema safety. Here is how the industry standards compare:

| Criteria | 🏆 **DataGrip (JetBrains)** | 🥇 **DBeaver Community** | 🥈 **pgAdmin 4** |
| :--- | :--- | :--- | :--- |
| **Classification** | Commercial Enterprise IDE | Open-Source Universal Standard | Official PostgreSQL GUI |
| **Cost** | Paid (JetBrains All Products Pack) | **$0.00 Free & Open-Source** | **$0.00 Free & Open-Source** |
| **Autocompletion & Dialect** | Industry-leading contextual SQL engine | Very good (schema-aware) | Basic syntax highlighting |
| **Schema Refactoring & DDL** | Automated migration diffs, smart rename | Manual DDL generators | Standard PostgreSQL object tree |
| **Git / VCS Integration** | Native Git history, query branching | Basic file saving | None |
| **Execution Plan Visualizer** | Interactive visual tree with bottleneck warnings | Visual explain graph | Visual explain tree |
| **Multi-Engine Support** | Yes (Postgres, SQLite, DuckDB, Redis, etc.) | Yes (80+ databases supported) | PostgreSQL only |
| **Resource Usage** | Moderate memory footprint | Moderate (Java/Eclipse platform) | High (embedded web application) |

### Recommendation
- **Best $0.00 Industry-Standard Choice (Recommended)**: **DBeaver Community Edition**. It gives you 90% of DataGrip's capabilities with visual ER diagrams, CSV/JSON export wizards, and fast query execution without spending a dollar.
- **Top Commercial Enterprise Choice**: **DataGrip** (if you already have a JetBrains subscription via IntelliJ IDEA Ultimate or the All Products Pack).
- **pgAdmin 4**: Useful primarily for PostgreSQL-specific administrative tasks (such as inspecting running vacuums or table bloat), but noticeably slower for day-to-day query authoring.

---

## 2. Master Network Topology & Access Matrix

All services operate inside a unified zero-trust network overlay:

```mermaid
flowchart TD
    subgraph ClientDevices["📱 Developer & Client Layer"]
        DevPC["Developer Machine (g14 via Tailscale)"]
        MobileApp["Android CMP App"]
        WebApp["Next.js Web App"]
    end

    subgraph EdgeLayer["⚡ Global Anycast Edge (Cloudflare)"]
        CFProxy["fc-edge-proxy.dirzaaulia11.workers.dev"]
    end

    subgraph OracleNode["🖥️ Oracle VM Super-Node (100.82.171.21 / 168.110.196.30)"]
        Caddy["Caddy 2 Reverse Proxy (TLS 1.3)"]
        TailscaleDaemon["Tailscale WireGuard Daemon"]
        
        subgraph InternalDockerNet["Docker Bridge Network (jarvis_net)"]
            Postgres["PostgreSQL 16 (Port 5432)"]
            PostgREST["PostgREST (Port 3000)"]
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
    DevPC -->|Tailscale HTTP (Ports 8080/5678/8090)| InternalDockerNet

    MobileApp -->|HTTPS / Caching| CFProxy
    WebApp -->|HTTPS / Caching| CFProxy

    CFProxy -->|Encrypted Origin Fetch| Caddy
    Caddy -->|Internal Reverse Proxy| PostgREST
    PostgREST -->|Internal SQL| Postgres
```

### Access Matrix

| Service | Port | Local Tailscale Access | Public Access | Authentication Required |
| :--- | :--- | :--- | :--- | :--- |
| **PostgreSQL 16** | `5432` | `footballclips-oracle:5432` | ❌ Blocked (Zero public exposure) | PostgreSQL Password |
| **PostgREST API** | `3000` / `443` | `footballclips-oracle:3000` | 🌐 `https://fc-edge-proxy...` | `web_anon` (Read-only) |
| **Tailscale SSH** | `22` | `ssh ubuntu@footballclips-oracle` | ❌ Blocked | Tailscale MFA / Web Auth |
| **FileBrowser** | `8090` | `http://footballclips-oracle:8090` | `https://files.168.110.196.30.sslip.io` | Admin Password |
| **AI Gateway** | `8080` | `http://footballclips-oracle:8080` | ❌ Blocked | Bearer Token / Open on Mesh |
| **Ollama Inference** | `11434` | `http://footballclips-oracle:11434` | ❌ Blocked | Open on Tailscale Mesh |
| **n8n Automation** | `5678` | `http://footballclips-oracle:5678` | ❌ Blocked | n8n User Session |
| **Qdrant Vector** | `6333` | `http://footballclips-oracle:6333` | ❌ Blocked | Open on Tailscale Mesh |

---

## 3. PostgreSQL 16 Enterprise Datastore

### Configuration & Credentials
- **Host**: `footballclips-oracle` (or `100.82.171.21`)
- **Port**: `5432`
- **Database**: `footballclips`
- **Username**: `postgres`
- **Password**: `jarvis_postgres_secure_2026`
- **Driver Type**: PostgreSQL standard JDBC / libpq

### Step-by-Step Connection Instructions

#### Option A: Connecting with DBeaver
1. Open DBeaver and click **New Database Connection** (plug icon with green plus).
2. Select **PostgreSQL** and click **Next**.
3. Fill in the connection settings:
   - **Host**: `footballclips-oracle`
   - **Port**: `5432`
   - **Database**: `footballclips`
   - **Authentication**: `Database Native`
   - **Username**: `postgres`
   - **Password**: `jarvis_postgres_secure_2026`
4. Click **Test Connection ...**. You will see:
   > Connected (PostgreSQL 16.x)
5. Click **Finish**. You can now visually browse schemas, tables (`matches`, `standings`, `supported_clubs`), edit rows, and generate ER diagrams.

#### Option B: Connecting with DataGrip
1. Click **+** in Database Explorer -> **Data Source** -> **PostgreSQL**.
2. Set **Host** to `footballclips-oracle`, **User** to `postgres`, **Password** to `jarvis_postgres_secure_2026`, and **Database** to `footballclips`.
3. Click **Test Connection**. Once verified with a green checkmark, click **OK**.

#### Option C: Direct Terminal CLI (`psql`)
From your terminal or inside the VM:
```bash
# Direct psql from local machine (with PostgreSQL client installed):
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

-- View highlights linked in Season 2025/26
SELECT id, competition_id, home_team_name, away_team_name, highlight_video_id, utc_date 
FROM matches 
WHERE season = 2025 AND highlight_video_id IS NOT NULL 
ORDER BY utc_date DESC 
LIMIT 10;

-- Refresh PostgREST schema cache after manual schema edits
NOTIFY pgrst, 'reload schema';
```

---

## 4. PostgREST Universal REST API Tier

PostgREST sits directly on top of PostgreSQL, turning tables into a high-performance REST API with zero manual backend code.

### Endpoints
- **Direct VM PostgREST**: `https://168.110.196.30.sslip.io`
- **Production Edge Proxy**: `https://fc-edge-proxy.dirzaaulia11.workers.dev/rest/v1`

### How to Query PostgREST
PostgREST uses declarative URL filtering:

```bash
# 1. Fetch 5 newest finished matches
curl -s "https://fc-edge-proxy.dirzaaulia11.workers.dev/rest/v1/matches?status=eq.FINISHED&order=utc_date.desc&limit=5"

# 2. Filter matches for a specific league and season
curl -s "https://fc-edge-proxy.dirzaaulia11.workers.dev/rest/v1/matches?competition_id=eq.PL&season=eq.2025&limit=10"

# 3. Fetch league standings for Serie A
curl -s "https://fc-edge-proxy.dirzaaulia11.workers.dev/rest/v1/standings?competition_code=eq.SA&season=eq.2026&order=position.asc"
```

---

## 5. Cloudflare Anycast Edge Proxy (`fc-edge-proxy`)

The edge proxy handles request routing, origin IP masking, enterprise security headers, and multi-tier edge caching across 300+ Cloudflare global data centers.

### Edge Caching Policies
- **Live Matches** (`IN_PLAY`, `PAUSED`): `30s TTL`
- **Finished Matches** (`FINISHED`, `FULL_TIME`): `24h TTL`
- **Standings** (`standings`): `6h TTL`
- **Supported Clubs** (`supported_clubs`): `24h TTL`
- **Auth & Profiles**: `Cache BYPASS` (strictly dynamic)

### Instant Cache Purge Hook
When the worker discovers new highlights or final scores, it triggers an instant edge purge:

```bash
curl -X POST https://fc-edge-proxy.dirzaaulia11.workers.dev/api/purge-cache \
  -H "Content-Type: application/json" \
  -d '{"target_url": "https://fc-edge-proxy.dirzaaulia11.workers.dev/rest/v1/matches?status=eq.FINISHED"}'
```

---

## 6. Domain Architecture & Human-Friendly URL Matrix

No developer should ever have to remember or type awkward IP addresses (`168.110.196.30` or `100.82.171.21`). Every endpoint is mapped to either your branded custom domain (`dirzaaulia.com` / `fc.dirzaaulia.com`) or your private Tailscale MagicDNS name (`footballclips-oracle`).

### Clean Master URL Directory

| Target Service | Category | Old Ugly URL / Raw IP | 🌟 Clean Readable Short URL | Access Scope |
| :--- | :--- | :--- | :--- | :--- |
| **FootballClips Web App** | Production Frontend | `footballclips.web.app` | **`https://fc.dirzaaulia.com`** | Public Web |
| **Edge REST API** | Production Backend | `fc-edge-proxy...workers.dev` | **`https://api.fc.dirzaaulia.com`** | Public Web (Enterprise Edge) |
| **Cloud Storage Hub** | Files / Backups | `https://files.168.110.196.30...` | **`https://files.fc.dirzaaulia.com`** | Public Web (TLS 1.3 Caddy) |
| **PostgreSQL 16** | Database Engine | `100.82.171.21:5432` | **`footballclips-oracle:5432`** | Private WireGuard Mesh |
| **Qwen 2.5 Coder 7B** | Local AI / Vibe Coding | `100.82.171.21:11434` | **`http://footballclips-oracle:11434`** | Private WireGuard Mesh |
| **AI Gateway** | OpenAI Routing API | `100.82.171.21:8080` | **`http://footballclips-oracle:8080`** | Private WireGuard Mesh |
| **n8n Automation** | Workflow UI | `100.82.171.21:5678` | **`http://footballclips-oracle:5678`** | Private WireGuard Mesh |
| **FileBrowser Local** | File Explorer UI | `100.82.171.21:8090` | **`http://footballclips-oracle:8090`** | Private WireGuard Mesh |
| **Speaches / Whisper** | Audio Transcription | `100.82.171.21:8000` | **`http://footballclips-oracle:8000`** | Private WireGuard Mesh |
| **Qdrant Vector DB** | Embeddings Store | `100.82.171.21:6333` | **`http://footballclips-oracle:6333`** | Private WireGuard Mesh |
| **Zero-Trust SSH** | Server Terminal | `ssh ubuntu@168.110.196.30` | **`ssh ubuntu@footballclips-oracle`** | Private WireGuard Mesh |

### Spaceship DNS Records Configuration (for `dirzaaulia.com`)

To activate the public subdomains, add these simple records into your Spaceship DNS Manager:

| Host / Subdomain | Type | Target Value | Description |
| :--- | :--- | :--- | :--- |
| `fc` | `CNAME` | `footballclips.web.app` | Points FootballClips frontend to Firebase |
| `api.fc` | `CNAME` | `fc-edge-proxy.dirzaaulia11.workers.dev` | Points Edge API to Cloudflare Workers proxy |
| `files.fc` | `A` | `168.110.196.30` | Points file explorer to Caddy on Oracle VM (Auto-TLS) |

---

## 7. Tailscale Mesh VPN & Zero-Trust SSH

### Accessing the VM
Because Tailscale SSH is active, connect without passwords or SSH keys:

```powershell
# From Windows PowerShell / Command Prompt / Terminal:
ssh ubuntu@footballclips-oracle
```

### Checking Status on Host
```bash
# Check connected peers and throughput
tailscale status

# Check assigned IP addresses
tailscale ip -4
```

---

## 8. FileBrowser Quantum (Cloud Storage Hub)

FileBrowser provides an interactive web-based file explorer for managing database backups, media assets, logs, and datasets.

### Access Details
- **Over Public Web**: `https://files.168.110.196.30.sslip.io`
- **Over Tailscale**: `http://footballclips-oracle:8090`
- **Username**: `admin`
- **Default Password**: `jarvis_secure_admin_2026` *(Change on first login in Settings -> User Management)*

### Storage Paths Managed
- `/srv/Backups`: Automated PostgreSQL `.sql.gz` database dumps.
- `/srv/Media`: Downloaded match clips, player posters, club crests.
- `/srv/Documents`: Schemas, documentation, configuration notes.

---

## 9. Hybrid AI Gateway & Vibe Coding Suite (Qwen 2.5 Coder)

The Oracle VM hosts a multi-engine AI stack on ARM64 Ampere cores:

### 1. FastAPI AI Gateway (`:8080`)
Exposes an OpenAI-compatible unified API that routes between local models and cloud providers (Groq, Gemini, OpenRouter).
```bash
curl -s http://footballclips-oracle:8080/health
# Returns: {"status":"healthy","engine":"jarvis-hybrid-gateway"}
```

### 2. Ollama Local LLMs (`:11434`)
Local on-device reasoning and coding models running directly on Ampere A1 ARM64 cores without any external API tokens or internet access. The container memory limit has been upgraded to **16 GB** (utilizing our 24 GB RAM while leaving 8 GB fully reserved for PostgreSQL, Caddy, PostgREST, and n8n):

| Model | Size | Architecture | Purpose & Vibe Coding Capability |
| :--- | :--- | :--- | :--- |
| **`deepseek-coder-v2:16b`** | 8.9 GB | 16B MoE (2.4B active params) | 🏆 **Top-Tier Flagship**: Trained on 6T tokens across 338 languages. Exceptional multi-file refactoring, vibe coding, and algorithmic synthesis. |
| **`qwen2.5-coder:7b`** | 4.7 GB | Dense 7B | 🥇 **Ultra-Fast Scaffolding**: Fast interactive completions and script generation. |
| **`phi4-mini:latest`** | 2.5 GB | Dense 3.8B | 🥈 **Concise Analytical Reasoning**: Logic puzzles, match summaries, and fast evaluations. |

#### How to use DeepSeek-Coder-V2 for Vibe Coding:
1. **Interactive Chat in Terminal**:
   ```bash
   ssh -t ubuntu@footballclips-oracle "docker exec -it jarvis-ollama ollama run deepseek-coder-v2:16b"
   ```
2. **One-shot CLI generation from PowerShell**:
   ```powershell
   ssh ubuntu@footballclips-oracle "docker exec jarvis-ollama ollama run deepseek-coder-v2:16b 'Scaffold a Jetpack Compose glassmorphic card for a live football match with pulse animation'"
   ```
3. **Connect to VS Code / Cursor / Continue Extension**:
   - In VS Code / Cursor, install the **Continue** extension (`continue.continue`).
   - In `~/.continue/config.json`, configure:
     ```json
     {
       "models": [
         {
           "title": "Oracle DeepSeek-Coder-V2 16B",
           "provider": "ollama",
           "model": "deepseek-coder-v2:16b",
           "apiBase": "http://footballclips-oracle:11434"
         },
         {
           "title": "Oracle Qwen 2.5 Coder 7B",
           "provider": "ollama",
           "model": "qwen2.5-coder:7b",
           "apiBase": "http://footballclips-oracle:11434"
         }
       ]
     }
     ```
   - Now press `Ctrl+I` or `Ctrl+L` in VS Code to vibe code with your private Oracle LLM!

### 3. Speaches / Faster-Whisper Large-v3-Turbo (`:8000`)
High-speed speech-to-text audio perception model running int8 quantization on ARM64.

### 4. Qdrant Vector Database (`:6333`)
Semantic memory store for highlight embeddings and similarity search:
```bash
curl -s http://footballclips-oracle:6333/collections
```

---

## 10. n8n Autonomous Workflow Orchestration

n8n serves as the central orchestration engine for automated platform health checks, background jobs, and operational alerts.

### 1. Active Production Workflow: `fcOpsMaintenance`
- **Workflow Name**: `FootballClips — hourly maintenance verification` (ID: `fcOpsMaintenance`)
- **Execution Schedule**: Hourly at minute 7 (`7 * * * *`) via `scheduleTrigger` + on-demand via `manualTrigger`.
- **Target Endpoint**: `POST http://footballclips-operations:8093/internal/maintenance`
- **Security**: Authenticated via internal HMAC header authentication (`genericCredentialType` / `httpHeaderAuth`).
- **Operational Scope**:
  1. **Backup Verification**: Validates that daily PostgreSQL database snapshots exist in `/storage/cloud/Backups/` and are non-empty.
  2. **Log Compaction**: Aggregates and compacts older worker event logs to prevent unbounded table growth.
  3. **Storage Telemetry**: Measures disk usage across NVMe partitions and records metrics in `fc_ops.metrics`.

### 2. Access & Management
- **Dashboard URL**: `http://footballclips-oracle:5678` *(Accessible securely over Tailscale mesh)*
- **Container**: `jarvis-n8n` (`n8nio/n8n:2.43.6`, RAM limit: 3 GB)
- **Persistent Data**: SQLite datastore at `/home/ubuntu/jarvis/storage/n8n/database.sqlite`

### 3. Manual Workflow Trigger & Diagnostic Inspection
To trigger the maintenance verification on-demand:
```bash
# Trigger maintenance workflow directly via operations API
ssh ubuntu@footballclips-oracle "curl -X POST http://footballclips-operations:8093/internal/maintenance"

# Inspect latest n8n execution status
ssh ubuntu@footballclips-oracle "docker logs --tail 50 jarvis-n8n"
```

## 11. Automated Disaster Recovery & Backblaze B2

The node includes automated nightly database backups and offsite cloud replication.

### 1. Nightly Database Dump (`backup_postgres.sh`)
Located at `/home/ubuntu/jarvis/scripts/backup_postgres.sh`:
- Runs `pg_dump` targeting the `footballclips` database.
- Compresses output with `gzip`.
- Retains the last 14 days of rolling backups in `/home/ubuntu/jarvis/storage/cloud/Backups`.

### 2. Backblaze B2 Sync (`sync_backblaze.sh`)
Located at `/home/ubuntu/jarvis/scripts/sync_backblaze.sh`:
- Uses `rclone` to synchronize local backups to a private Backblaze B2 bucket (`footballclips-archives`).
- Runs daily via cron at `03:00 UTC`.

### Manual Trigger for Emergency Backup
```bash
ssh ubuntu@footballclips-oracle "sudo bash /home/ubuntu/jarvis/scripts/backup_postgres.sh"
```

---

## 12. System Maintenance, Health Monitoring & Diagnostics

### Standard Service Health Check
Run this single command from your local machine to check all 10 Docker containers:
```powershell
ssh ubuntu@footballclips-oracle "docker ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'"
```

### Checking Container Logs
```bash
# PostgREST logs
ssh ubuntu@footballclips-oracle "docker logs --tail 50 jarvis-postgrest"

# PostgreSQL engine logs
ssh ubuntu@footballclips-oracle "docker logs --tail 50 jarvis-postgres"

# Caddy web server logs
ssh ubuntu@footballclips-oracle "docker logs --tail 50 jarvis-caddy"
```

### Oracle Cloud Anti-Idle Shield Status
Oracle Cloud reclaims Always Free instances if CPU utilization stays under 15% for 7 days. Our background `jarvis-oracle-shield` container continuously maintains harmless synthetic activity:
```bash
ssh ubuntu@footballclips-oracle "docker logs jarvis-oracle-shield"
```

### 12.4 Zero-Spend Financial Guardrail ($1.00 Alert)
An automated financial safety circuit is active at the tenancy control plane level:
- **Budget Name**: `Zero-Spend-Shield` (Monthly target: `$1.00`)
- **Actual Spend Alert**: Fires if actual tenancy billable usage reaches `$1.00`
- **Forecasted Spend Alert**: Fires if projected monthly usage reaches `$1.00`
- **Notification Target**: `dirzaaulia11@gmail.com`
- **Budget ID**: `ocid1.budget.oc1.ap-batam-1.amaaaaaa6bzpczyak337e5s5aendvweist2yfzsiuuhvs72drr3fxgmtvn5q`
- **Management Script**: `python scripts/create_oci_budget.py`
