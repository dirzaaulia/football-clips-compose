# Tasks: Add 7 High-Market Competitions

- [x] Verify ESPN Scoreboard, Standings, Archive (`2024/2025`), Logos, and YouTube `/embed/` feasibility.
- [x] Audit participating clubs from ESPN `teams` API and resolve official YouTube channels (156 unique clubs, 155 with YouTube channels).
- [x] Apply PostgreSQL migration (`20261128_add_7_market_competitions.sql`) for `competitions` and `supported_clubs` (`INSERT 0 7`, `INSERT 0 156`).
- [x] Update `core-go/adapters/espn/client.go` and `core-go/cmd/fc-daemon/main.go`, then deploy `fc-daemon-arm64` to Oracle VM.
- [x] Update Web and Android fallback metadata and qualification rules.
- [x] Run `go test -race ./...`, `npm run build`, and `./gradlew :composeApp:compileAndroidMain`.
