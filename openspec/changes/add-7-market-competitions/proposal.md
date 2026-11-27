# Change Proposal: Add 7 High-Market Competitions + Full Club & YouTube Audit

## Why
Expand FootballClips coverage to 7 high-market global & Asian competitions (`ACL`, `ACL2`, `ENG2`, `JPN1`, `TUR1`, `MEX1`, `BEL1`) with verified ESPN Scoreboard, Standings, Archive Season (`2024/2025`), and audited embeddable club/broadcaster YouTube channels.

## Impacted Pillars
- **Backend (`core-go`)**: `adapters/espn/client.go`, `cmd/fc-daemon/main.go`, PostgreSQL `competitions` + `supported_clubs` migration, archive backfill via `fc-ingest`.
- **Telemetry**: Automatic waterfall & coverage tracking for all 38 active competitions.
- **Web (`web/`)**: `src/lib/competitions.ts`, `src/hooks/useCatalogHighlights.ts`, `src/lib/standingsQualification.ts`.
- **Android (`composeApp/`)**: `util/LeagueUtils.kt`, `ui/score/components/StandingsQualification.kt`.
