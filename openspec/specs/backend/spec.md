# Backend Specification (`core-go/`)

## Purpose
Defines the autonomous Golang 1.23+ micro-daemon (`fc-daemon`), PostgreSQL 16 + PostgREST persistence contracts, ESPN ingestion, YouTube highlight discovery, and Shorts pipeline invariants.

## Requirements

### Requirement: Autonomous Daemon Cycles
The `fc-daemon` process on Oracle VM ARM64 MUST execute scheduled synchronization cycles with strict context timeouts:
- **Live & Upcoming Score Sync**: Every `60s` (`45s` timeout) across all active competitions resolved dynamically from PostgreSQL `competitions` (falling back to `fallbackComps`).
- **Highlight Discovery**: Every `2m` (`120s` timeout) for `FINISHED` matches under 4-tier quota guardrails (Tier 1: 30m, Tier 2: 2h, Tier 3: 6h, Tier 4: 24h; 60,000 units/day quota).
- **Shorts Discovery & Pruning**: Every `30m` (`120s` timeout) across active `supported_clubs` and broadcaster/league channels.
- **Standings Sync**: Every `6h` (`90s` timeout) across all active league/hybrid/group-cup competitions.

### Requirement: 100% Embeddable & Region-Playable Shorts Gate
Every Short persisted to `football_shorts` MUST pass all verification gates in `shorts_embeddability.go`:
1. Vertical short-form verification via title or `HEAD https://www.youtube.com/shorts/{videoID}` (`HTTP 200`).
2. Commercial spam & non-action filler rejection (`highlights`, `press conference`, `interview`, `podcast`, `vlog`, etc.).
3. YouTube Data API v3 `videos.list` (`part=status,contentDetails`) verification: `status.embeddable == true`, `WatchabilityPct >= 50.0%`, and `DurationSeconds <= 180` (with `oEmbed` fallback if API key is absent).
4. Live `/embed/{videoID}` HTML playability probe confirming absence of `UNPLAYABLE`, `LOGIN_REQUIRED`, `errorScreen`, and external playback disablement markers.
5. Rolling 30-day TTL pruning and 3,000-item capacity cap (`PruneStaleShorts`).

### Requirement: Historical Archive Immutability & Lean DB
- Records with `season < 2026` MUST remain immutable (protected by PostgreSQL trigger `protect_archive_matches`).
- Tactical boxscores, formations, statistics, and editorial stories MUST be streamed on-demand from ESPN API and never persisted to PostgreSQL `matches`.
