# 🏛️ Oracle Cloud (OCI) Always Free Tier — Master Strategic Blueprint
**Author & Maintainer:** Dirza Aulia  
**Infrastructure Target:** Oracle Cloud Infrastructure (OCI) Always Free Tier  
**Compute Spec:** Ampere A1 Compute (ARM64) — **4 OCPU (Cores) + 24 GB RAM + 200 GB NVMe SSD + 10 TB Egress/Month**  
**Budget:** Strictly **$0.00 / Month** (Perpetual Free Tier)  
**Date:** October 2026

---

## 1. Executive Summary & Why OCI Always Free?

Oracle Cloud Infrastructure (OCI) provides the single most generous perpetual free tier in the cloud industry:
- **4 OCPU ARM64 Cores** & **24 GB of RAM** (Can be allocated as 1 large beast VM or up to 4 smaller VMs).
- **200 GB Total NVMe SSD Block Storage** with automated backups.
- **10 TB (10,000 GB) Outbound Egress Bandwidth per month** (eliminates bandwidth limits).
- **1 Free Public IPv4 Address** + full IPv6 subnets.
- **2 Autonomous Databases** (20 GB each, 1 OCPU).

---

## 2. The 7-Day "Idle VM" Reclamation Trap & The Solution

> [!CAUTION]
> ### Oracle's Idle VM Policy
> Oracle automatically reclaims (terminates) Always Free compute instances if they are deemed **"idle"** for 7 consecutive days (CPU, memory, and network all $< 20\%$).
>
> ### The 2 Ironclad Countermeasures:
> 1. **Upgrade to "Pay As You Go" (PAYG) Status:**  
>    Add a verified payment method and convert the account to PAYG. In Oracle's official service terms, **PAYG accounts are 100% exempt from idle VM reclamation**. As long as you stay within the Always Free limits (4 OCPU, 24 GB RAM, 200 GB storage), your monthly invoice is **$0.00**.
> 2. **Baseline Working Memory Set:**  
>    By running our core Docker containers (Coolify, Redis, Postgres, Prometheus), the baseline RAM usage will sit at $\approx 11.2\text{ GB}$ (**$46\%$ memory utilization**), ensuring it is never flagged as idle.

---

## 3. Reality Check: Steam Caching vs. Multi-Device Data Sharing

### ❌ Steam Caching on Cloud VM: DO NOT DO THIS
- **Why LanCache/SteamCache fails on Cloud:**
  - LanCache is designed strictly for a **Local Area Network (LAN)** (e.g. home Raspberry Pi, TrueNAS, unRAID) over 1Gbps/2.5Gbps Ethernet.
  - Putting it on a remote cloud VM means your PC still downloads every gigabyte over your home ISP from Oracle, saving **0% home internet bandwidth**.
  - Just 2 modern games (*Cyberpunk 2077* + *Baldur's Gate 3*) consume $> 200\text{ GB}$, which will crash your OCI disk.
  - Valve and Steam already have enterprise Akamai/Valve CDN edge caches directly inside local Indonesian/regional ISPs that download faster than a single cloud VM.

### ✅ Multi-Device Data Sharing & Private Cloud: 1000% YES!
Using the VM as a private 24/7 cross-device hub is one of the highest-value uses:
1. **[Filebrowser](https://filebrowser.org/) / [Nextcloud AIO](https://github.com/nextcloud/all-in-one)**:
   - Private personal cloud storage (Google Drive / Dropbox alternative). Drag-and-drop, stream, and manage files, APK releases, and documents from any browser across Android, Windows PC, and laptop.
2. **[Syncthing](https://syncthing.net/) Always-On Hub**:
   - Continuous peer-to-peer folder synchronization between your Android phone, laptop, and PC. The VM serves as the 24/7 online relay so your PC doesn't need to stay powered on for your phone to sync files.
3. **[PairDrop](https://pairdrop.net/) (Private AirDrop)**:
   - Web-based encrypted local and remote file sharing. Open the URL on any device to instantly transfer files, photos, or clipboards across the internet securely.
4. **[RustDesk Server](https://rustdesk.com/)**:
   - Self-hosted remote desktop gateway (free TeamViewer / AnyDesk alternative) to remote control your PC from your Android device with sub-20ms latency.

---

## 4. Project-by-Project Upgrade Blueprint

### ⚽ 1. FootballClips (Live Football Scores & Official Highlights)
- **Persistent Live WebSocket & SSE Gateway**:
  - Cloud Run is scale-to-zero (request/response). Holding 10,000 open WebSockets consumes active CPU minutes.
  - The OCI Ampere VM can run a high-concurrency **FastAPI / Node.js WebSocket Hub** maintaining 50,000+ open sockets broadcasting live pitch clocks (`12'`, `45+2'`, `HT`) and goal celebrations with $<5\text{ms}$ latency at **$0 cost**.
- **Universal Sub-1ms Redis 7 Cache**:
  - In-memory caching for live matches and verified highlight links, reducing database latency from $150\text{ms}$ to $< 1\text{ms}$.
- **PostgreSQL 16 Historical Archive & Replica**:
  - Replicate Supabase match data into OCI NVMe storage, bypassing Supabase's 500 MB free cap and keeping unlimited historical season archives.
- **10 TB Free Egress**:
  - Stream rich matchweek JSON payloads and high-res telemetry without egress billing concerns.

---

### 🏎️ 2. Formula1 (Live F1 Telemetry & Standings App)
- **Live Race Telemetry & Lap Timing Hub**:
  - Ingest real-time OpenF1 / FastF1 data feeds and broadcast live sector times, speed traps, and pit stop gaps to the Android app over persistent WebSockets.
- **Historical Grand Prix Telemetry Database**:
  - High-frequency telemetry traces (throttle, brake, RPM, DRS, speed vectors) consume 20-50 GB of storage. OCI's 160 GB NVMe storage stores years of race data effortlessly.

---

### 🎒 3. LootTrack (Gaming Inventory & Asset Tracker)
- **Dedicated Backend Microservices**:
  - Run heavy relational queries, periodic cron scrapers, or batch export generators alongside Cloudflare Workers.
- **Asset Processing & Automated Screenshot Worker**:
  - Use 4 OCPU ARM cores for image compression, thumbnail generation, and running headless **Playwright** for automated store listing screenshots (`capture_clean_screenshots.py`).

---

### 💳 4. CreditCard (Finance & Bank Statement Ingestion App)
- **Private Self-Hosted AI Statement Extractor (OCR + Parser)**:
  - Run an open-source parsing pipeline (Python `pdfplumber` + lightweight local OCR/LLM) on the VM.
  - Drop a bank statement PDF (BCA, Mandiri, Citi, etc.) into the app $\to$ parses dates, merchant names, amounts, and billing cycles into structured JSON transactions in $<2\text{s}$ with **zero private data sent to commercial third-party APIs**.
- **Zero-Knowledge Encrypted Sync**:
  - Self-host a private CouchDB / Kinto sync server for encrypted transaction sync across Android, iOS, and WebApp.

---

### 🎮 5. StreamDeckClone (Mobile Controller & Rust Desktop Host)
- **Worldwide Remote Control (NAT-Traversal Cloud Relay)**:
  - Overcomes the limitation of requiring your phone and PC to be on the same local Wi-Fi network.
  - Host a lightweight Protobuf / WebSocket Relay on the VM: control your PC from anywhere in the world over 4G/5G mobile data (mute mic, launch apps, control Spotify, start OBS streams).
- **Web-Based Deck Studio**:
  - Web dashboard to design button profiles, icon layouts, and macros that sync instantly to your phone.

---

### 🌍 6. countries-compose (Space, Earth, Satellites & Solar System)
- **Universal Shared Redis (Upstash Replacement)**:
  - Migrate from Upstash's daily request limits to your dedicated OCI Redis 7 instance with unlimited throughput.
- **Live Space & Flight Telemetry Gateway**:
  - An automated worker on OCI aggregates real-time ISS orbital coordinates, satellite TLE trajectories, live flight radars, and earthquake feeds, broadcasting via SSE to the client app.

---

### 🏸 7. badminton-shuffle (Court Matchmaking & Player Rotation)
- **Live Court TV Kiosk / Scoreboard Display**:
  - Deploy a responsive, high-contrast scoreboard web app on Coolify.
  - Mount a tablet or TV at the court: displays active matches, countdown timers, "Next Up" player rotations, and automated audio buzzer announcements, while players control matches from their phones.

---

### 🌐 8. Personal Web & Developer Mission Control (`dirzaaulia.com`)
- **Centralized Multi-App Telemetry Widget**:
  - Showcase real-time status of your entire ecosystem:
    - ⚽ **FootballClips**: Active live matches & highlight coverage.
    - 🏎️ **Formula1**: Next race countdown & standings.
    - 🎒 **LootTrack**: Cloudflare D1 sync status.
    - 💳 **CreditCard**: Sync server uptime.
  - Provides a single web portal to manage staging environments and experimental prototypes.

---

## 5. VM Allocation & Memory Budget Blueprint

### Recommended Allocation: 1 Large Beast VM
Deploying a single **4 OCPU, 24 GB RAM, 160 GB NVMe SSD** instance running Ubuntu 24.04 ARM64.

```
┌─────────────────────────────────────────────────────────────────┐
│               OCI Ampere A1 (24 GB Total RAM)                   │
├─────────────────────────────────────────────────────────────────┤
│ [OS + Docker Overhead]         : 1.0 GB                         │
│ [Coolify PaaS Engine + Traefik]: 0.8 GB                         │
│ [Shared Redis 7 Cache Hub]     : 1.0 GB (maxmemory 1gb)         │
│ [PostgreSQL 16 Analytical DB]  : 2.5 GB                         │
│ [WebSocket Hub (FC + F1)]      : 1.0 GB                         │
│ [Filebrowser / Syncthing Hub]  : 0.3 GB                         │
│ [Uptime Kuma Health Prober]    : 0.1 GB                         │
│ [Tailscale Private Mesh]       : 0.1 GB                         │
│ [GlitchTip (Sentry Alt)]       : 0.5 GB                         │
│ [Nightly Backup Coordinator]   : 0.2 GB                         │
│ [GitHub Actions Runner Burst]  : 4.0 GB (active during builds)  │
├─────────────────────────────────────────────────────────────────┤
│ TOTAL ACTIVE WORKING SET       : 11.5 GB (~48% RAM utilization) │
│ FREE HEADROOM FOR APPS/CONTAINERS: 12.5 GB (52% Available)      │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. Implementation Setup Steps

1. **Step 1: OCI Account & VM Creation**:
   - Register or login to [cloud.oracle.com](https://cloud.oracle.com/).
   - Select Home Region (e.g. `ap-singapore-1`, `ap-tokyo-1`, or `us-ashburn-1`).
   - Create Compute Instance:
     - Shape: **VM.Standard.A1.Flex** (Ampere) $\to$ 4 OCPUs, 24 GB RAM.
     - Image: **Ubuntu 24.04 LTS (aarch64)**.
     - Boot Volume: **160 GB**.
     - Networking: Assign Public IPv4 address.
2. **Step 2: Server Hardening & Base Stack**:
   ```bash
   # Update system
   sudo apt update && sudo apt upgrade -y

   # Install Docker
   curl -fsSL https://get.docker.com | sh
   sudo usermod -aG docker $USER

   # Install Tailscale (Private Mesh)
   curl -fsSL https://tailscale.com/install.sh | sh
   sudo tailscale up

   # Install Coolify PaaS (One-line automated installer)
   curl -fsSL https://cdn.coolify.io/coolify/install.sh | bash
   ```
3. **Step 3: Access Coolify Dashboard**:
   - Open `http://<YOUR_VM_IP>:8000` in your browser.
   - Configure Traefik domain and automated SSL.
4. **Step 4: Deploy Shared Services**:
   - Deploy **Redis 7** (1-click template in Coolify).
   - Deploy **PostgreSQL 16** (1-click template in Coolify).
   - Deploy **Uptime Kuma** (1-click template in Coolify).
   - Deploy **GlitchTip** (Sentry-compatible error tracker).
   - Connect GitHub repository for **GitHub Actions ARM64 Runner**.

---

*This document serves as your permanent strategic reference for scaling your entire application portfolio on a 100% free cloud architecture.*
