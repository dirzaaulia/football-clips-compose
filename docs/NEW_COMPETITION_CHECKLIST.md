# New Supported Competition Onboarding Checklist

Use this playbook whenever adding a new football league, cup, or international tournament to FootballClips so all 4 pillars (**Backend**, **Telemetry**, **Android**, **Next.js Web**) light up consistently with zero gaps.

---

## Step 1: ESPN API & Archive Feasibility Probe
Verify the ESPN Soccer API slug (e.g. `eng.2`, `afc.cup`, `jpn.1`) supports all required endpoints:
1. **Live & Upcoming Scoreboard**:
   `GET https://site.api.espn.com/apis/site/v2/sports/soccer/{slug}/scoreboard`
2. **Standings** (for leagues & group-stage cups):
   `GET https://site.api.espn.com/apis/v2/sports/soccer/{slug}/standings`
3. **Archive Seasons (`season < 2026`)**:
   `GET https://site.api.espn.com/apis/v2/sports/soccer/{slug}/standings?season=2024`
   `GET https://site.api.espn.com/apis/site/v2/sports/soccer/{slug}/scoreboard?dates=20250405`
4. **Light & Dark Emblems**:
   Extract `leagues[0].logos` (`500/{id}.png` and `500-dark/{id}.png`).

---

## Step 2: YouTube Channel & `/embed/` Playability Audit
1. Identify the official league/broadcaster YouTube channel AND **all participating club channels** via ESPN `teams` endpoint.
2. **Critical**: Do NOT rely solely on `youtube.com/oembed`. Probe `https://www.youtube.com/embed/{videoId}` HTML to confirm videos do not return `UNPLAYABLE` or `"Playback on other websites has been disabled"`.
   - If the league channel blocks embeds (e.g., `@TheAFCHub`), prioritize **official club channels** and regional embeddable broadcasters.

---

## Step 3: PostgreSQL 16 Migration (`oracle/migrations/`)
Create a dated SQL migration inserting/upserting into:
1. `public.competitions`:
   - `id`, `name`, `short_name`, `country_code`, `type` (`league` | `cup` | `hybrid`), `slug`
   - `emblem_url`, `emblem_dark_url`, `youtube_handle`, `youtube_channel_id`, `backup_channel_handles`, `backup_channel_ids`
   - `is_active = true`, `display_order`
2. `public.supported_clubs`:
   - Upsert all participating teams (`id`, `team_id`, `name`, `short_name`, `competition_code`, `competition_name`, `crest_url`, `youtube_handle`, `youtube_channel_id`, `channel_tier`, `backup_channel_handle`, `is_active = true`).

---

## Step 4: Golang Backend (`core-go/`) & Oracle VM
1. `core-go/adapters/espn/client.go`: Add competition entry to `defaultCompetitions`.
2. `core-go/cmd/fc-daemon/main.go`: Add competition ID to `fallbackComps` and `standingsComps`.
3. Cross-compile & deploy `fc-daemon-arm64` to Oracle VM (`sudo systemctl restart fc-daemon`).
4. Run archive backfill for `2024`/`2025` via `fc-ingest` if historical fixtures are desired.

---

## Step 5: Next.js Web (`web/`) & Android CMP (`composeApp/`) Fallbacks
Both clients load `competitions` dynamically from PostgREST, but update static fallback maps & standings zone colors:
- **Web**:
  - `web/src/lib/competitions.ts`
  - `web/src/hooks/useCatalogHighlights.ts`
  - `web/src/lib/standingsQualification.ts`
- **Android**:
  - `composeApp/src/commonMain/kotlin/com/dirzaaulia/footballclips/util/LeagueUtils.kt`
  - `composeApp/src/commonMain/kotlin/com/dirzaaulia/footballclips/ui/score/components/StandingsQualification.kt`

---

## Step 6: OpenSpec & Verification Gates
1. Create/update an OpenSpec change under `openspec/changes/<comp-expansion>/` and sync to `openspec/specs/` upon completion.
2. Run all automated gates:
   - `cd core-go; go test -race -v ./...`
   - `cd web; npm run build`
   - `./gradlew :composeApp:compileAndroidMain`
3. Verify `https://edge.fc.dirzaaulia.com/rest/v1/competitions` returns the new competition with `is_active: true`.
