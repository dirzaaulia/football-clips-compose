# ⚽ Football Clips (Compose Multiplatform & Next.js Web Platform)

<div align="center">

<img src="androidApp/src/main/ic_launcher-playstore.png" width="128" height="128" alt="Football Clips Icon" style="border-radius: 28px;" />

### Watch Official Match Highlights, Follow Real-Time Scores & Receive Live Club Alerts

[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.21-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.12.1-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Target Android SDK](https://img.shields.io/badge/Android%20Target-SDK%2037%20(AGP%209.4)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Next.js](https://img.shields.io/badge/Web%20Portal-Next.js%2016%20%2F%20React%2019-000000?style=for-the-badge&logo=next.js&logoColor=white)](https://fc.dirzaaulia.com)
[![Next.js Telemetry](https://img.shields.io/badge/Telemetry-Next.js%20Console-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)](https://fc.dirzaaulia.com/telemetry)
[![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL%2016-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://postgresql.org)
[![Google Play](https://img.shields.io/badge/Google%20Play-Production%20%2F%20Internal-34A853?style=for-the-badge&logo=googleplay&logoColor=white)](https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips)

</div>

---

## 📱 Google Play Store Marketing Showcase

Here are the official high-resolution marketing banners featured on the Google Play Store listing:

<div align="center">
  <table>
    <tr>
      <td align="center" width="25%">
        <img src="screenshots/store_listing/01_highlights_banner.png" width="220" alt="Official Match Highlights" />
        <br />
        <b>🎬 Watch Match Highlights</b>
        <br />
        <sub>High-quality video recaps & decisive goals</sub>
      </td>
      <td align="center" width="25%">
        <img src="screenshots/store_listing/02_fixtures_banner.png" width="220" alt="Live Scores & Fixtures" />
        <br />
        <b>⚡ Live Scores & Fixtures</b>
        <br />
        <sub>Real-time updates across top leagues</sub>
      </td>
      <td align="center" width="25%">
        <img src="screenshots/store_listing/03_leagues_banner.png" width="220" alt="Explore Leagues & Competitions" />
        <br />
        <b>🏆 Filter Top Competitions</b>
        <br />
        <sub>Premier League, La Liga, UCL, Serie A & more</sub>
      </td>
      <td align="center" width="25%">
        <img src="screenshots/store_listing/04_premium_banner.png" width="220" alt="Pure Ad-Free Experience" />
        <br />
        <b>💎 Pure Football Experience</b>
        <br />
        <sub>Cross-device account sync & lifetime ad-free</sub>
      </td>
    </tr>
  </table>
</div>

---

## ✨ Key Features

- 🎥 **HD Official Match Highlights**: Curated, decisive goals, and extended match highlights from official club channels and authorized broadcast partners without clickbait.
- ⚡ **Live Scores, Match Clocks & Real-Time Events**: Real-time scoreboards with live match minute badges (`LIVE 74'`, `HT`, `FINISHED`, `SCHEDULED`), team crests, and kickoff schedules across both Android CMP and Web.
- 📺 **TV Broadcast Live News Ticker**: Smooth, continuous auto-scrolling live scoreboard ticker on Web (`LiveScoreTicker.tsx`) with hover-pause and frosted glass crests.
- 📂 **Collapsible Accordion Feed by Competition**: Grouped competition sections with smooth expand/collapse accordions on both Android CMP and Next.js Web for effortless scanning.
- 🛡️ **Universal Frosted Glass Logos & Crests (`GlassLogoBadge`)**: Standardized frosted glass backgrounds for all club crests and tournament emblems across Android and Web for premium visibility and high contrast.
- 🎛️ **Synchronized Navigation Rail**: Seamlessly synced collapsed side rail featuring Season Selector, Starred My Clubs with real-time counter, Spoiler-Free mode toggle, and Go Premium action dock.
- 🏆 **Broad Worldwide Competition Coverage**:
  - **European Elite**: Premier League, LaLiga, Serie A, Bundesliga, Ligue 1, Eredivisie, Liga Portugal.
  - **UEFA Tournaments**: UEFA Champions League, Europa League, Conference League, FA Cup, Carabao Cup, Copa del Rey, DFB-Pokal, Coppa Italia.
  - **Asian & Middle Eastern Giants**: AFC Champions League Elite, Saudi Pro League, Liga 1 Indonesia (native ESPN `idn.1` ingestion).
  - **South & North Americas**: CONMEBOL Libertadores, Copa Sudamericana, Recopa Sudamericana, Brasileirão Série A, Argentine Liga Profesional, Major League Soccer.
- 📅 **Dual-Season Multi-Platform Coverage (Active 2026/27 & Historical 2025/26)**:
  - **Active 2026/27 Campaign**: Real-time live scores, 2D interactive pitch radar, short-form video reels, and autonomous AI-reranked match highlights.
  - **Historical 2025/26 Archive**: Complete cross-platform browsing across 3,420+ immutable historical fixtures, official video highlights, and final standings, protected by database-level immutability triggers (`protect_archive_matches`).
- 📊 **Tactical Match Debrief & Boxscore Statistics**: In-depth match intelligence including authentic ESPN statistics (possession, shots, passes, corners, tackles), starting formations, upcoming match previews, and full-time journalistic summaries.
- 📰 **Multi-Source Football Journalistic Hub**: Accredited football news streams from Tier-1 sports journalism providers (BBC Sport, The Guardian) with real-time editorial feeds.
- 🔔 **Real-Time Club Push Notifications ($0.00 Cost Architecture)**:
  - Broadcasts across 5 critical match phases: **Highlight Available**, **Going to Play (Countdown)**, **In Play (Kickoff)**, **Score Updated (Live Goals)**, and **Finish Match (Full Time)**.
  - Delivered via **Firebase Cloud Messaging (FCM) Topics** (`club_{club_id}`) with zero database polling overhead or per-user token storage.
- ⭐ **My Clubs Pinning & Contextual Permission Flow**:
  - Save favorite clubs to personalize fixtures, scoreboards, and alert subscriptions.
  - Adheres strictly to modern permission guidelines: **Zero cold-start permission popups**. Contextual soft-ask rationale sheets are presented naturally when a user stars a club.
- 🎨 **Material 3 Expressive Design**: 
  - Dynamic edge-to-edge layout on Android (Target SDK 37, AGP 9.4).
  - Signature Droplet circular-reveal dark/light theme switching animation.
  - Floating pill navigation bar with dynamic badge indicators.
- 🌐 **Modern Multi-Platform Architecture**: 
  - **Android Native App**: Jetpack Compose & Compose Multiplatform, hardware-accelerated inline and fullscreen YouTube player, heads-up FCM notifications.
  - **Web Portal ([fc.dirzaaulia.com](https://fc.dirzaaulia.com))**: Built with **Next.js 16 (React 19 & Turbopack)** with Static Site Generation (SSG), Incremental Static Regeneration (ISR), live marquee scoreboard ticker, quick `Cmd+K` command palette, Web Push service worker, and Google AdSense integration.
  - **APM Command Center ([fc.dirzaaulia.com/telemetry](https://fc.dirzaaulia.com/telemetry))**: Ultra-fast, responsive **Next.js 16 Telemetry Console** with live 60,000 units/day YouTube API quota gauges, per-match waterfall diagnostics, granular candidate accept/reject verdict inspector, and real-time Cloud Tasks queue monitor.
- 💎 **Universal Account & In-App Purchase Sync**:
  - Google Native Sign-In with direct session management.
  - Google Play In-App Billing (RevenueCat SDK 10.26) for Android.
  - Cross-device entitlement synchronization stored securely in authoritative PostgreSQL 16 (`profiles` table).

---

## 🏛️ System Architecture

<div align="center">
  <a href="docs/assets/architecture.svg">
    <img src="docs/assets/architecture.svg" alt="Football Clips System Architecture" width="100%" />
  </a>
  <p align="center">
    <sub>🔍 <b>High-Resolution Vector Diagram</b>: Click diagram to open in full screen (or <a href="docs/assets/architecture.svg?raw=true">view raw vector</a> for infinite native zoom &amp; pan).</sub>
  </p>
</div>

### Architectural Tiers Breakdown

| Layer / Tier | Technology Stack | Responsibilities & Capabilities |
| :--- | :--- | :--- |
| **Presentation: Android** | Compose Multiplatform 1.12.1<br/>Jetpack Compose, SDK 37 | Material 3 Expressive UI, Hardware-accelerated YouTube player, FCM messaging service (`FootballClipsFirebaseMessagingService`), RevenueCat Google Play billing, NextGen AdMob. Dynamic track-based version naming (`3.0.1 (run_number)`). |
| **Presentation: Web Portal** | Next.js 16, React 19, TypeScript<br/>Tailwind CSS, Turbopack | Primary public web portal (`fc.dirzaaulia.com`), SSG/ISR static export, Web Push background Service Worker (`firebase-messaging-sw.js`), Google AdSense ads, custom glassmorphic dropdowns, and global season selector. |
| **Presentation: APM Command Center** | Next.js 16, React 19, Tailwind CSS<br/>Domain: `fc.dirzaaulia.com/telemetry` | Real-time observability dashboard (`/telemetry`), 60,000 units/day YouTube quota gauge, per-match waterfall traces, AI candidate evaluator cards, and Cloud Tasks queue monitor. |
| **Global Edge & REST API** | Caddy Auto-TLS & Edge Proxy<br/>Domain: `edge.fc.dirzaaulia.com` | High-speed global edge layer with PostgREST endpoint routing, rate limiting, and enterprise security headers (HSTS, CSP, X-Frame-Options, Nosniff). |
| **Authoritative Datastore & Super-Node** | Oracle Always Free (Ampere ARM64)<br/>PostgreSQL 16, PostgREST, n8n, Caddy | Dedicated 4-core, 24GB RAM super-node with Caddy auto-TLS hosting production PostgreSQL 16 datastore with `protect_archive_matches` immutability trigger preserving 3,420+ archive matches, AI Gateway (DeepSeek R1 / Ollama ARM64), n8n autonomous maintenance workflows, and automated Backblaze B2 sync. |
| **Push Notification Network** | Firebase Cloud Messaging (FCM)<br/>Topic Architecture: `club_{id}` | Global edge fanout delivering goal alerts, match schedules, and highlight releases to millions of Android devices and Web browsers at **$0 infrastructure cost**. |
| **Serverless AI Worker Engine** | Python 3.12/3.13, FastAPI, Asyncio<br/>Google Cloud Run (`fc-worker`) + Cloud Tasks | Containerized serverless worker on Cloud Run with Google Cloud Tasks per-match decoupled fanout, 60,000 quota management, Oracle VM AI Gateway reranking with Gemini Flash Lite fallback. |
| **Offsite Backup & Cloud Storage** | Backblaze B2 + Rclone Sync<br/>Automated Cron Engine | Nightly automated synchronization from Backblaze B2 to the super-node cloud vault with encrypted storage and automated retention. |
| **Cron Scheduling Fleet** | Google Cloud Scheduler<br/>3 Perpetual Free Jobs | Drives worker execution cadences (1m live match tick, 15m highlight sweep, Cloud Tasks auto-retry) strictly within the 100% free perpetual tier. |

---

## 🛠️ Modern Tech Stack

| Category | Technology | Version | Description |
| :--- | :--- | :--- | :--- |
| **Language** | **Kotlin** | `2.4.21` | Modern compiler with K2 engine & multiplatform targets |
| **Build System** | **Android Gradle Plugin (AGP)** | `9.4.0` | Latest Gradle configuration with Target SDK 37 |
| **UI Framework (Mobile)** | **Compose Multiplatform** | `1.12.1` | Declarative UI for Android Native (Material 3 Expressive, 2D Pitch Canvas) |
| **Web Portal Engine** | **Next.js (App Router)** | `16.4.2` | React 19, Turbopack, Tailwind CSS, Live Ticker, Quick Command Palette (`Cmd+K`) |
| **Target Platform (Android)** | **Android 16 / SDK 37** | SDK 37 | Min SDK 24, compile & target SDK 37 |
| **Dependency Injection** | **Koin** | `4.2.2` | `koin-core`, `koin-compose`, `koin-compose-viewmodel` |
| **Networking** | **Ktor Client** | `3.6.0` | HTTP client with OkHttp engine on Android |
| **Image Loading** | **Coil** | `3.6.3` | `coil-compose`, `coil-network-ktor3`, `coil-svg` |
| **Global Edge Proxy** | **Cloudflare Workers** | Edge | Sub-15ms edge caching proxy (`fc-edge-proxy`) with instant cache purge |
| **Primary Datastore** | **PostgreSQL 16 (Oracle Super-Node)** | `16.4` | Enterprise PostgreSQL 16 with PostgREST 3000 (0 egress limits) |
| **Dedicated Super-Node** | **Oracle Always Free ARM64** | Ubuntu 24.04 | 4 OCPUs, 24GB RAM, PostgreSQL 16, PostgREST API, DeepSeek AI Gateway |
| **Authentication & Profiles** | **Direct Google Sign-In & PostgREST** | Native / REST | Direct Google ID verification with native session management and PostgreSQL profiles |
| **Offsite Cloud Sync** | **Backblaze B2 & Rclone** | `1.60.1` | Automated disaster recovery synchronization to super-node cloud vault |
| **Push Notifications** | **Firebase Messaging (FCM)** | `35.0.0` / `7.7.0` | Android FCM SDK, Web Push Worker, Python `firebase-admin` |
| **In-App Billing** | **RevenueCat Purchases** | `10.26.0` | Google Play Store lifetime non-consumable billing |
| **Mobile Ads** | **Google Mobile Ads NextGen** | `1.5.0` | Policy-compliant native ads & adaptive banner views |
| **Web Ads** | **Google AdSense** | Cloud | Responsive display ads adhering to Google publisher policies |
| **Serverless Worker Fleet** | **Google Cloud Run** | Revision 37 (`fc-worker-00037-k4k`) | Serverless container (`fc-worker`) + Google Cloud Tasks asynchronous queue |
| **Scheduler Engine** | **Google Cloud Scheduler** | Cloud | Perpetual cron jobs (1m match sync, 15m highlights sweep) |
| **Sports Data Engine** | **ESPN Sports API** | Public | Zero rate limit, 0 API quota, full European 2026/2027 coverage |
| **Video Highlight Ingestion** | **YouTube Data API v3** | 60,000 units/day | Official approved enterprise quota + 2-stage embargo scheduler (T+15m, T+90m) |
| **AI Semantic Re-ranker** | **DeepSeek-R1 / Gemini Flash** | AI Gateway | Oracle VM Ollama DeepSeek-R1 with Gemini 2.5 Flash Lite zero-latency fallback |
| **Hosting & CDN** | **Firebase Hosting** | Multi-Site | Hosting targets for Next.js web portal and Next.js Telemetry Hub |

---

## 🤖 Serverless AI Worker & Event-Driven Engine

Match fixtures, live scores, and verified video highlights are synchronized autonomously via the containerized Python worker engine on Google Cloud Run:

```
                                  [Cloud Scheduler]
                                         │
                                         ▼ (HTTP POST /run-cycle)
[ESPN Sports API] ───────► [match_service.py] ──────► [PostgreSQL 16 DB (Oracle VM)]
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
                      [EdgeCacheClient (Purge)] ──► [Edge Caddy / PostgREST]
                                                    (edge.fc.dirzaaulia.com)
```

1. **Fixture & Live Score Synchronization (`match_service.py`)**:
   - Queries Tier-1 competitions from ESPN's public endpoints with zero rate-limit constraints and zero cost.
   - Prevents status and score regressions on delayed upstream updates.
2. **Event-Driven YouTube WebSub & RSS Discovery (`highlight_service.py`)**:
   - Ingests real-time upload webhooks via Google PubSubHubbub (`POST /websub`) for $<100\text{ms}$ instant match linking.
   - Scans official club and league YouTube RSS feeds with **0 YouTube API quota spent**. For complete technical architecture, worldwide audience guarantees, and multi-tier quota maximizer, see **[`worker/PIPELINE.md`](./worker/PIPELINE.md)**.
3. **AI Semantic Reconciliation (`gemini_client.py`)**:
   - Employs **Gemini 3.8 Flash** with structured JSON output and a 12 RPM sliding-window token bucket.
   - Seamless rule-based heuristic scoring fallback if AI quota hits rate limits.
4. **Real-Time Notification Dispatcher (`notification_service.py`)**:
   - Formats channel-specific payloads for goals, kickoff reminders, and highlight releases.
   - Utilizes an in-memory 24-hour event deduplicator preventing duplicate alerts.
5. **Self-Healing & Automated Maintenance (`maintenance_service.py`)**:
   - Recovers matches stuck in `IN_PLAY` beyond 180 minutes and rolls forward stale fixtures.
   - Continuously purges matches, highlights, and telemetry events older than 30 days.

---

## 🛡️ Multi-Tier Android Backward Compatibility & Active Version Telemetry

As of October 2026, because users update at different paces across Google Play, the system strictly mandates **100% backward compatibility** across all 3 historical endpoint tiers:

| Tier | Target URL | Underlying Datastore | Status & Guardrails |
| :--- | :--- | :--- | :--- |
| **Legacy (Pre-migration)** | `https://eiomktvavndorazreyba.supabase.co` | Supabase Cloud | Synchronized with latest highlights to ensure un-upgraded devices view fresh matches |
| **Transitional (v3.0.0)** | `https://fc-edge-proxy.dirzaaulia11.workers.dev` | PostgreSQL 16 (Oracle VM) | Sub-15ms Cloudflare Anycast edge caching with automatic cache invalidation |
| **Modern (v3.0.1+)** | `https://edge.fc.dirzaaulia.com` | PostgreSQL 16 (Oracle VM) | Caddy TLS reverse proxy stripping JWT headers (`header_up -Authorization`) for `web_anon` |

### Effortless Active Version Tracking (Zero Play Console Overhead)
To track what version number users are actively running without navigating the Google Play Console:
1. **Lightweight App Session Telemetry**: Android clients send a fire-and-forget ping to `${SUPABASE_URL}/rest/v1/app_sessions` containing `app_version`, `version_code`, and `platform`.
2. **Real-Time Distribution View**: The Mission Control Next.js Telemetry Dashboard (`https://fc.dirzaaulia.com/telemetry`) displays a live 7-day version distribution (e.g. `v3.0.1: 82%`, `v3.0.0: 12%`, `v2.x: 6%`), enabling safe, data-driven decisions before retiring legacy bridges.

---

## 📁 Project Structure

```
FootballClips/
├── .github/workflows/          # GitHub Actions CI/CD deployment pipelines
├── androidApp/                 # Android Native application module
│   ├── src/main/kotlin/        # Application class, MainActivity, and FCM Service
│   └── build.gradle.kts        # Android App build & signing configuration
├── cloudflare/                 # Cloudflare Workers Edge Caching Proxy (fc-edge-proxy)
│   ├── src/index.ts            # Multi-tier edge caching & instant cache purge API
│   └── wrangler.toml           # Cloudflare deployment configuration
├── composeApp/                 # Shared Kotlin Multiplatform codebase
│   ├── src/commonMain/         # Shared Compose Multiplatform UI, ViewModels, & Repositories
│   └── src/androidMain/        # Android-specific KMP implementations & Koin modules
├── web/                        # Next.js 16 Web Portal & Telemetry Hub (App Router)
│   ├── src/app/                # App Router (pages: /, /fixtures, /standings, /match/[id], /telemetry)
│   ├── src/components/         # React 19 UI components & interactive @xyflow graphs
│   ├── src/lib/firebase/       # Firebase Messaging client initialization
│   ├── public/                 # Web Push service worker (firebase-messaging-sw.js)
│   └── package.json            # Next.js dependencies & scripts
├── worker/                     # Python Serverless Worker & APM Engine
│   ├── clients/                # Supabase, ESPN, YouTube RSS, Gemini AI, and Edge Cache clients
│   ├── services/               # Match, Highlight, Notification, Standings, & Maintenance services
│   ├── tests/                  # Pytest unit tests (54/54 passing)
│   ├── server.py               # FastAPI server (/metrics, /health, /status, /run-cycle, /websub)
│   ├── Dockerfile              # Container definition for Google Cloud Run
│   └── main.py                 # Async worker loop & cycle orchestrator
├── docs/                       # Architectural documentation & high-resolution vector assets
│   └── assets/architecture.svg # Standalone vector architecture diagram
├── oracle/                     # Oracle Always Free ARM64 Super-Node Infrastructure
│   ├── docker-compose.yml      # Multi-service topology (Postgres, PostgREST, FileBrowser Quantum, Caddy)
│   ├── Caddyfile               # Auto-TLS reverse proxy configuration
│   ├── sync_backblaze.sh       # Rclone automated Backblaze B2 sync script
│   ├── backup_postgres.sh      # Automated nightly PostgreSQL backup script
│   └── README.md               # Node architecture & operations guide
├── scripts/                    # Deployment & ASO management utilities
│   ├── deploy_web.py           # Next.js Web & Telemetry build and deploy script for Firebase Hosting
│   └── deploy_production.py    # Production deployment utility
└── gradle/libs.versions.toml   # Centralized version catalog
```

---

## 🚀 Getting Started

### Prerequisites

- **JDK 17** or **JDK 21** (Temurin recommended)
- **Android Studio Ladybug (2024.2+)** with Kotlin Multiplatform plugin
- **Android SDK 37** build-tools installed
- **Node.js 20+** (for Next.js web application)
- **Python 3.12+** and [`uv`](https://docs.astral.sh/uv/) package manager

### 1. Build & Run Android App

```bash
# Build debug APK
./gradlew :androidApp:assembleDebug

# Verify file line budgets (< 250 lines per file)
./gradlew checkLineBudget

# Install and run on connected Android device/emulator
./gradlew :androidApp:installDebug
```

### 2. Run Next.js Web Application

```bash
cd web

# Install dependencies
npm install

# Start local development server with Turbopack
npm run dev

# Build production static export (SSG/ISR)
npm run build
```

The web application and telemetry hub will be accessible at `http://localhost:3000` (and `http://localhost:3000/telemetry`).

### 3. Run Background AI Worker Engine

```bash
cd worker

# Install dependencies via uv
uv sync

# Run test suite
uv run pytest

# Start worker engine
uv run python main.py
```

### 4. Deploy Web & Telemetry to Firebase Hosting

Run the deployment script to compile Next.js and deploy the web portal and Telemetry Hub to Firebase Hosting:

```bash
python scripts/deploy_web.py
```

---

## 🚢 CI/CD & Deployment

- **Android Tracks (Play Store)**: Automated publishing via Fastlane and GitHub Actions to **Internal**, **Alpha**, **Beta**, and **Production** tracks with automated keystore management.
- **Web & Telemetry (Firebase Hosting)**: Multi-site hosting targets:
  - **Main Web Portal**: [https://fc.dirzaaulia.com](https://fc.dirzaaulia.com) (Fallback: `https://footballclips.web.app`)
  - **Telemetry Dashboard**: [https://fc.dirzaaulia.com/telemetry](https://fc.dirzaaulia.com/telemetry) (Standalone: `https://footballclips-telemetry.web.app`)
- **Worker Daemon & Automation**: Containerized serverless worker running on **Google Cloud Run** (`fc-worker`) in decoupled dual-engine pipeline mode (Fixture Sync + YouTube Scraper) with real-time YouTube WebSub webhooks and Gemini AI reranking.
- **Production Edge Infrastructure**: High-performance architecture on Oracle Cloud & Cloudflare Anycast with Caddy TLS reverse proxy, hosting PostgreSQL 16, PostgREST (`https://edge.fc.dirzaaulia.com`), Operations API (`https://api.fc.dirzaaulia.com`), and automated Backblaze B2 disaster recovery sync.

---

## 📄 License

This project is proprietary and maintained by [dirzaaulia](https://github.com/dirzaaulia). All rights reserved.
