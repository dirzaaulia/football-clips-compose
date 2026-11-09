# ⚡ FootballClips Core Go Micro-Daemon & CLI Toolkit

High-performance, pure **Golang 1.23+** micro-daemon and operational CLI suite for FootballClips. Engineered with **Hexagonal Clean Architecture (Ports & Adapters)** for sub-millisecond execution, zero garbage collection latency spikes, and minimal memory footprint (<20 MB RAM).

---

## 🏗️ Architecture & Package Layout

```
core-go/
├── bin/                       # Compiled binaries (fc-daemon, fc-deploy, e2e-runner)
├── cmd/
│   ├── fc-daemon/main.go      # Daemon entrypoint (<100 lines): live scores, highlights, shorts, WebSub
│   ├── fc-deploy/main.go      # Production Android deployment CLI (<100 lines)
│   └── e2e-runner/main.go     # Single-command full-stack 4-layer E2E test runner
├── domain/                    # Pure domain layer (zero external framework imports)
│   ├── models.go              # Strict contract models (MatchFixture, VideoCandidate, ShortsCandidate)
│   ├── geo.go                 # Target market geo-cutoff & worldwide watchability calculation
│   └── cooldown.go            # 4-tier quota cooldown matrix
├── ports/                     # Input/Output interface contracts
│   └── repositories.go        # Database, ESPN, YouTube, and AI Gateway protocols
├── usecases/                  # Single-action application interactors
│   ├── sync_live.go           # Live match polling & FCM dispatch (60s tick)
│   ├── discover_highlights.go # Highlight search, multi-tier discovery & AI reranking (5m tick)
│   ├── ingest_shorts.go       # 0-quota Atom RSS shorts ingestion & Hype Score (10–100) (30m tick)
│   └── renew_websub.go        # PubSubHubbub subscription renewal (12h tick)
├── adapters/                  # Outbound technology adapters
│   ├── postgres/              # Direct pgx/v5 binary protocol connection pool
│   ├── espn/                  # ESPN live match & score REST client
│   ├── youtube/               # Zero-quota Atom XML RSS parser & YouTube Data API v3 client
│   ├── websub/                # HTTP callback server (:8094) for instant upload notifications
│   ├── aigw/                  # Oracle VM AI Gateway / Ollama ARM64 reranking adapter
│   ├── fcm/                   # Firebase Cloud Messaging HTTP v1 push notification adapter
│   └── edge/                  # Edge cache invalidation adapter
└── tests/                     # Unit & integration test suites
```

---

## 🚀 Binaries & Commands

### 1. `fc-daemon` (Autonomous Operations Micro-Daemon)
Runs continuously on the Oracle VM host via systemd (`fc-daemon.service`):
- **Live Match Polling**: Queries ESPN API every 60s for active matches and goal alerts.
- **Highlight Discovery**: Runs every 5m evaluating finished matches through Tier 1 (League), Tier 2 (Club), and Tier 3 (Broadcaster) channels.
- **Shorts Rotation**: Runs every 30m cycling 50 clubs per batch via zero-quota Atom RSS, assigning dynamic Hype Scores (10–100).
- **WebSub Push Hub**: Listens on port `8094` for real-time YouTube upload webhooks.

### 2. `fc-deploy` (Native Deployment CLI)
Replaces legacy deployment scripts with type-safe Go execution:
```bash
# Production flexible update prompt (In-App Update Level 2)
./bin/fc-deploy --production --prompt

# Production immediate blocking force update (In-App Update Level 5)
./bin/fc-deploy --production --force

# Custom update priority (0 = silent, 2 = flexible, 5 = immediate)
./bin/fc-deploy --production --priority 3

# Internal testing track release
./bin/fc-deploy --internal
```

### 3. `e2e-runner` (Full-Stack 4-Layer Verification)
Single-command orchestrator running across all repository tiers:
```bash
# Run all 4 layers (L1 Contracts, L2 Pipeline, L3 Parity, L4 UI E2E)
./bin/e2e-runner --all

# Run specific layer
./bin/e2e-runner --l1   # PostgREST JSON contract validation
./bin/e2e-runner --l2   # Live PostgreSQL & replay integration
./bin/e2e-runner --l3   # Evaluation parity tests
./bin/e2e-runner --l4   # Android Compose UI & Next.js Web E2E
```

---

## 🛠️ Compilation & Cross-Compilation

### Local Development Compilation (Windows / macOS / Linux)
```powershell
# From core-go/ directory:
go build -o bin/fc-daemon.exe ./cmd/fc-daemon
go build -o bin/fc-deploy.exe ./cmd/fc-deploy
go build -o bin/e2e-runner.exe ./cmd/e2e-runner
```

### ARM64 Cross-Compilation for Oracle VM (Linux ARM64)
```powershell
# Cross-compile Linux ARM64 binary:
$env:GOOS="linux"; $env:GOARCH="arm64"; go build -o bin/fc-daemon-arm64 ./cmd/fc-daemon

# Deploy to Oracle VM:
scp bin/fc-daemon-arm64 ubuntu@footballclips-oracle:/home/ubuntu/footballclips-operations/core-go/bin/fc-daemon
ssh ubuntu@footballclips-oracle "sudo systemctl restart fc-daemon"
```

---

## 🧪 Testing

```powershell
# Run all unit tests
go test ./...

# Run tests with race condition detector
go test -race ./...

# Run tests with verbose output
go test -v ./...
```

---

## 🔐 Environment Configuration

The daemon reads configuration from `/etc/footballclips/fc-daemon.env` or local `.env`:

```env
DATABASE_URL=postgres://postgres:<PASSWORD>@localhost:5432/footballclips?sslmode=disable
ESPN_API_URL=https://site.api.espn.com/apis/site/v2/sports/soccer
YOUTUBE_API_KEY=<SECURE_KEY>
AI_GATEWAY_URL=http://localhost:8080/v1
PORT=8094
LOG_LEVEL=info
```
