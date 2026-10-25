# Unified FootballClips implementation



Started 23 October 2026. Existing work is preserved. Highlightly remains enabled until owner cutover.



## Backups

- Local source snapshot and working-tree patch: Codex workspace `backups/footballclips-*20261023T082612Z*`.

- Oracle database, n8n database/encryption configuration, and Jarvis configuration:

  `/home/ubuntu/footballclips-upgrade-backups/20261023T082612Z`.

- Database gzip verified; server backups are private (0600).



## Work sequence

1. Add durable PostgreSQL operations and separate quota accounting.

2. Fix entry-scoped WebSub parsing, country coverage, verified source ingestion and outbox.

3. Add private Oracle executor, owner APIs, public status, n8n coordination and safe rollout.

4. Add independent catalog/search APIs and client compatibility.

5. Revamp Wasm; migrate Android/Next.js to compact feeds.

6. Verify archive/retention, fixtures, benchmarks and deployment.



## Rollout constraints

- Initial stage is dry run: no new search traffic until the full-day gate is met.

- Search target 47,500 of 50,000/day; general cap 57,000 of 60,000/day; 60 searches/minute.

- All 65 markets, including 11 ASEAN members; external YouTube playback counts.

- New competitions/sources are staged. Existing archive rows stay unchanged.

- One owner schedules each pipeline; no external messages, paid infrastructure, or Modal jobs.



## Resumed 23 October, 14:25 UTC

- Oracle private operations executor is deployed; migrations 001–006, durable jobs, country selection, quota reconciliation, outbox, archive protection, and owner APIs are live.

- Cloud Run first upgraded image is live; final image with shared leases and fixture-policy integration still needs rollout.

- Initial dry run remains active. No automatic Highlightly cutover; full-day gates cannot be skipped.

- Unit verification: 15 tests passed (Atom parsing, markets, availability, DST, cursors, archive corruption/interruption).

- PostgreSQL backup restored successfully into an isolated temporary database (4,074 fixtures); restoration manifest saved on VM. Remote SQL backup remains unverified.

- Current changes connect the actual Next.js homepage to the shared feed, add saved-catalog search to Android/web, date filters, and cross-page fallback deduplication.

- Android and Wasm compiled previously; final distribution builds and patched Next.js 16.4.2 build are running.

- Isolated PostgreSQL contracts and 17/50/100-competition read benchmarks are running in temporary tables. These do not certify live ingestion capacity.

- n8n hourly maintenance workflow installation is running; no external messaging configured.



### Remaining rollout work

1. Resolve any final build/integration failures and record benchmark results.

2. Protect legacy telemetry routes in Caddy; deploy Cloudflare cache and compatibility router without changing owner mode.

3. Update final Cloud Run image; verify schedule owners and real health.

4. Publish rebuilt Wasm/Next.js hosting, verify browser behavior and owner denial.

5. Complete remaining UI controls and matching/identity work; document partial features honestly.

6. Observe a full dry-run day, then advance 10%, 50%, 95% only after successful full-day checks.


## Live deployment and verification, 23 October 15:05 UTC
- Published website/status/owner sign-in and Wasm console to both existing Firebase Hosting sites. Trailing-slash routing fixed.
- Public status verified in browser with real heartbeats. Private Wasm gate verified visually; anonymous owner requests return 401/no-store.
- Cloudflare proxy and legacy Rust/Wasm wrapper deployed. Catalog-only remains false; the owner switch is reversible.
- Cloud Run updated with secured administration/legacy metrics and shared leases. Dashboard redirects to owner console; health reports HTTP readiness only.
- Isolated 85,000/250,000/500,000-fixture read benchmarks: p95 7.95/7.40/8.41 ms. Temporary data rolled back, zero external API calls or production writes.
- Unit suite: 20 passed, including cached AI outcomes and explicit rejection. AI checks use advertised Flash models, short timeouts and cached decisions.
- Fixed repeated full-time queueing, outbox state reversal identities, and country-coverage reopening.
- Android compiled successfully. Web detail pages refresh viewer-specific availability; external-only video has a YouTube playback button.
- New clients are being rebuilt to use the existing Cloudflare proxy for actual viewer country; direct Oracle host has no geo headers.
- Next.js 16.4.2 build passed; production dependency audit has zero vulnerabilities. Existing unrelated whole-project lint failures remain.
- n8n manual workflow completed with actual result and maintenance heartbeat; success execution history is pruned. SQL restore proof valid; remote SQL copies unverified.
- Maintenance comparison sample and blocked provider review classification are being deployed.

### Security follow-up resolved
Legacy Supabase worker_cycles/worker_events permissions were repaired in the authenticated project SQL editor. Independent anonymous probes now return 401 for both tables; public matches still returns 200. Oracle telemetry and Cloud Run administration are also protected. No football rows were changed by the permission migration.

### Remaining acceptance work
- Finish final deployments and verify country routing, playback, source controls, public/private routes.
- Expand canonical provider/team alias registry and provider-backoff checks; do not call these complete yet.
- Observe the real 24-hour dry run and staged full-day ramps. Never skip gates or flip catalog-only automatically.
- Database reads are tested at 100 competitions; live ingestion throughput and WebSub p95 need representative production samples.

## Final implementation verification, 23 October 16:25 UTC

- Oracle migrations 001–010 deployed. Canonical/provider team registry, reviewed aliases, scoped source matching, batched metadata checks and platform-limited country handling are implemented.
- Cloud Run revision `fc-worker-00027-lx6` activated under the shared ingestion lease. Transactional fixture trigger enabled only after the compatible bridge became ready; a real fixture replication completed through the outbox.
- Live/calendar schedules remain authenticated with one scheduling owner. Old duplicate/renewal schedules stay paused.
- Oracle public football mutations now require the service credential; both raw and `/rest/v1/` anonymous empty-array POST probes return 401. Public reads remain 200.
- Unit suite: **27 passed**. Final isolated PostgreSQL contracts, transactional trigger/rollback, source identity, country detail and owner-route checks passed with zero production writes and zero external API requests.
- Concurrent-load read benchmark: 85,000 / 250,000 / 500,000 fixtures, p95 **39.74 / 9.50 / 8.31 ms**. Earlier runs varied; these are query benchmarks, not live ingestion certification.
- Live 20-item ID-country feed: **13,612 bytes** uncompressed, below 30 KiB. Owner/telemetry responses are 401 and private/no-store without owner authentication.
- Two invalid provider mappings were retried with the new handler and are now explicitly blocked for owner review, not crash-looping failed jobs.
- Android compilation, final Wasm distribution and Next.js production build passed. A transient build-time catalog connection reset prompted one bounded retry and deduplicated page/metadata resolution; the subsequent build passed.
- Supabase Site URL corrected from localhost to the existing production domain; exact `/owner-signin` callback added while preserving Android callback and existing web root. Web authentication uses PKCE.
- Final hosting publication and real owner browser sign-in are being verified. The public status page uses readable local timestamps and service names.
- Operations runbook: `docs/FOOTBALL_OPERATIONS_RUNBOOK.md`.

### Remaining real-world rollout gates

- Start a full observation day for the final verified executor image; then advance 10%, 50%, 95% only through successful full-day gates.
- Measure representative real WebSub receipt-to-selection and API visibility latency. Targets are not yet certified.
- Review missing channels, invalid provider mappings, new competitions and rights evidence; no automatic approvals.
- Verify an actual remote SQL backup destination and allowance before adding remote writes. Existing local backup restoration is verified.
- Android changes are compiled locally; no Google Play release was performed.
- Highlightly remains enabled (`catalog_only=false`). Only the owner decides when to switch.

## Resume checkpoint — 24 October, 00:53 WIB

- Rechecked production after the interruption. Latest website/Wasm hosting release completed, Cloud Run remains on the compatible revision, transactional fixture delivery is active.
- Stale selection handling was deployed after the previous checkpoint: an obsolete cache/replication/notification event is marked **superseded**, without sending its old payload or recording a false API-visible result.
- Final isolated checks including the stale-event/no-network contract passed. Read benchmarks at 85k/250k/500k fixtures: p95 7.03/6.92/9.50 ms. No production writes or external API calls in these tests.
- At the live check: 58 fixture replications and seven fixture notification jobs completed; no failed jobs or pending outbox deliveries. Two invalid competition mappings remain explicitly blocked for owner review.
- Supabase anonymous telemetry denial and public football reads were reverified. Oracle/edge/Cloudflare catalog reads return 200; empty service-authenticated writes return 201 and anonymous writes 401 across all three tiers. These write checks inserted no football rows.
- Cloudflare geo-aware catalog reports ID for the verification client. Twenty-item feed is approximately 13.6 KB.
- Dry-run observation began 23 October at 23:26 WIB. First full-day review is after **24 October at 23:26 WIB**, subject to actual successful checks. No automatic ramp or catalog-only switch.
- Production Supabase callback settings and PKCE website release are saved. The connected browser is currently unavailable, so complete Google owner sign-in has **not** been verified. User verification question remains pending; do not claim login success.
- No Google Play release. Remote SQL backup remains unverified. Real WebSub p95 and live 50/100-competition ingestion certification remain outstanding.
- Full owner runbook is in `docs/FOOTBALL_OPERATIONS_RUNBOOK.md`.

## Owner loading fix and Liga Indonesia — 24 October, approximately 01:35 WIB

- Fixed the actual loading loop: the HTML redirected `/telemetry` to `/telemetry/`, while Firebase canonicalized the latter back. Removed the redirect, retained the correct asset base, normalized the session-refresh iframe URL, and deployed no-cache headers for the console route.
- Positive owner authentication and a rendered Wasm dashboard with live private data are now verified in the browser. Screenshot: `telemetry-loading-fixed.png` in the Codex task workspace. Fresh recovery URL: `https://fc.dirzaaulia.com/telemetry?release=20261024`.
- Liga Indonesia (IDN / BRI Super League) explicitly approved per the user's request. Official current-season HTML adapter is deployed; stale ESPN October coverage is excluded from live fixture polling for this competition.
- Imported **306 season-2025 fixtures**, representing the 2025/26 ESPN archive. The current I.League feed has **18 upcoming fixtures plus one independently verified completed result** at this snapshot.
- Official source provenance verified through I.League's embedded video `gUSn5G8CGUI`, whose API channel ID matches the verified Liga1Match mapping. WebSub confirmation recorded; the first highlight is accepted and playable through the public app API with embedding enabled.
- Recognizes Indonesian mini-match/recap labels and reviewed canonical team spelling aliases, including PSIM Yogjakarta / Yogyakarta. Tests cover this actual eligibility case.
- Official standings parser validates all **18 clubs**, positions and arithmetic before committing. The public standings endpoint returns 18 current-season rows. Current club metadata is present.
- **33 unit tests passed**. Isolated PostgreSQL verification passed, including new fixture idempotency, club metadata and historical-update protection. No production writes or external calls in those isolated tests.
- Private pre-admission SQL backup verified at `/home/ubuntu/footballclips-upgrade-backups/20261024-indonesia/before-indonesia.sql.gz` (5,307,332 bytes).
- Current-season result backfill remains partially waiting: official result-detail HTTP requests time out. It never substitutes guessed kickoff times or upload timestamps. Retry interval one hour, eight attempts maximum before owner retry.
- Recommended browser-based PostgreSQL administrator: self-hosted pgAdmin Web, behind owner authentication. This was researched as an alternative to DBeaver; no database-admin service or public database port was deployed.
- Next rollout remains: resolve remaining source backfill issues, review the measured full-day run, then gated 10% / 50% / 95% discovery allocation. Highlightly cutover remains owner controlled.

### Final browser check

The new canonical Liga fixture deep link loads the correct Persebaya / PSIM result, official YouTube embed and verified availability through the existing generic match-page fallback. A pre-existing score-only recap template was found inventing tactics and labeling them verified; its fallback now reports catalog facts and explicitly lacks a sourced tactical report. The sourced-news lookup remains intact. Next.js production build passed; final hosting publication includes this correction.


## October 24 — canonical telemetry recovery and pgAdmin Web

- Reproduced the user's exact `/telemetry` URL in the original browser tab. The previously cached slash redirect no longer runs; verified the canonical URL reloads into measured, authenticated owner data without a query-string workaround.
- Deployed `Cache-Control: no-store, max-age=0, must-revalidate` for the telemetry document/assets, changed the bootstrap bundle release marker, and added a retry screen for actual bundle/Wasm loading errors. Do not infer startup failure from a missing light-DOM canvas: this renderer does not expose its canvas there.
- Deployed pgAdmin 4 **9.18**, official ARM64 image pinned by digest `sha256:c332c5f6dfba995d9ebc4af261d93506d6876085d712eaaa3defc8dd1a3f26de`.
- Browser entry: https://fc.dirzaaulia.com/database → **Open pgAdmin** → https://api.fc.dirzaaulia.com/pgadmin/browser/.
- Existing Google/Supabase owner verification issues a Secure, HttpOnly, SameSite=Strict, host-only cookie scoped to `/pgadmin`, expiring after one hour. No new user-facing password. Caddy checks this cookie before every pgAdmin request, sanitizes incoming identity headers before forward authentication, then injects its private shared secret. pgAdmin accepts only the private Docker proxy subnet (including IPv4-mapped IPv6) and the preconfigured owner account.
- Browser verified the connected server and its three databases: **footballclips**, **loottrack**, **postgres**. No database mutations were needed for verification.
- Unauthenticated/forged-header requests redirect to owner entry; unauthenticated session creation returns 401. Two focused tests verify owner/origin requirements, cookie flags, expiry, signature tampering, and non-owner rejection. Next production build and both Hosting deployments succeeded.
- pgAdmin has no host port mapping; existing PostgreSQL host mapping was unchanged. Direct public TCP 5432 was not reachable from this workstation during verification. This is a reachability observation, not a complete firewall audit.
- Persistent pgAdmin state: `/home/ubuntu/footballclips-pgadmin/data`; configuration/secrets beside it are private. Resource cap 0.75 CPU / 768 MiB, measured startup memory approximately 226 MiB. Existing VM only; no Modal credits, new paid service, or new external backup writes.
- Added a verified SQLite/settings backup to the existing daily PostgreSQL backup job, with 14-day local retention. Pre-deployment Caddy backup remains in `footballclips-upgrade-backups/pgadmin-*`. Remote backup verification and full-day discovery rollout gates remain outstanding; Highlightly was not disabled.


## Monitoring redesign — 24 October 2026

- Implemented the owner-only dark Wasm console: Dashboard, Pipelines, Logs, Coverage, Quota & Storage, Operations. Existing approvals and provider controls remain available.
- Added shared event filters, server-side time series, stable event/run cursors, trace/context endpoints and new-job correlation. Historical correlation stays explicitly unavailable when absent.
- Every displayed operational date uses Asia/Jakarta with an explicit WIB suffix. UTC machine values remain available. CSV/JSON exports include readable WIB timestamps.
- Dashboard shows measured stage activity, actual errors, current outstanding jobs and sampled latency. Missing measurements remain Unknown. Live aggregates refresh every 30 seconds; live logs every five seconds while visible.
- Logs offer bounded virtualized rows, history navigation, pause/catch-up, saved filters, trace details and export. Tables have bounded responsive widths and horizontal scrolling on narrow screens.
- Liga Indonesia emblem is served from /logos/competitions/idn-2026.png, sourced from the official I.League competition logo. Database emblem, website fallbacks/cache and Android match fallback updated.
- Verification: 32 focused Python tests passed; isolated PostgreSQL contracts passed without production writes or external API requests; WIB conversion checked across UTC, Los Angeles and Tokyo process timezones. Wasm production build passed.
- User explicitly requested keeping BigLeaguesConstants.kt and SupportedClubsConstants.kt deleted. The working-tree Android build remains blocked by imports of those types. Website constants.ts/clubs.ts are also already deleted. Isolated compatibility snapshots compiled Android and built the website successfully without restoring any working-tree deletion. No Google Play release.
- Verified pre-deployment backup: /home/ubuntu/footballclips-upgrade-backups/monitoring-20261023T194151Z (SQL gzip integrity checked, private configuration snapshot retained).
- Live failures are real: feed jobs reported upstream HTTP 404/500. Do not hide them or describe the entire pipeline as healthy. Source repair remains separate from this interface rollout.
- No new paid infrastructure, Grafana deployment, Modal usage or discovery quota ramp. Highlightly cutover remains owner controlled.

### Final production verification — 24 October, 13:46 WIB

Both Hosting sites deployed successfully; temporary narrow-screen test wrapper removed. The canonical telemetry URL rendered owner data after reload. Verified desktop/narrow layouts, readable event rows, ERROR filtering with matching histogram totals, and a real feed job drawer showing correlated start/retry events and WIB dates. Liga Indonesia standings logo loaded with 18 teams. Anonymous monitor summary returns 401, public /api/status/v1 returns 200, and versioned logo returns 200. Repeated focused suite: 32 passed. Upstream feed 404/500 failures remain visible and unresolved; the deliberate constants deletions remain unchanged.


## Cycle and match monitoring rebuild — 24 October 2026

- Owner requested a fresh monitoring history, not legacy imports. New UUID-backed counters start at 1 independently for YouTube dispatches, instant uploads and fixture synchronization. The old telemetry_state document was removed after backup; football records, archives, existing jobs, quota accounting and provider settings were preserved.
- Verified backup: /home/ubuntu/footballclips-upgrade-backups/cycles-20261024T072515Z (SQL gzip integrity verified; private configuration archive retained).
- Migration 016 adds cycles, evaluations, job associations, attempts and reusable skip snapshots. Association records preserve shared-job deduplication and child execution evidence. Dispatch completion is separate from downstream completion.
- Active Oracle executor and Cloud Run fixture producer updated. Cloud Run revision fc-worker-00028-8bf serves the structured fixture producer. Receiver rejects old string-only payloads; no old-cycle import adapter was introduced.
- Actual search rollout remains dry_run with search_fraction=0. Feeds and upload verification continue; planned API searches are explicitly labeled as not executed.
- Candidate decisions record source, content/identity/duration checks, AI accepted/rejected/unavailable, cache reuse, deterministic fallback, coverage and delivery evidence. New attempts and waiting reasons are preserved.
- Repeated skip cohorts share a content-addressed snapshot. First seven dispatches reused one snapshot instead of writing 14,000 repeated skip rows. Actual evaluation row size was approximately 225 bytes in the initial live sample, excluding indexes and event history.
- Focused suite: 42 tests passed, including the fixture sync contract. Temporary-table SQL checks passed for counters, deduplication, shared child jobs, outcomes, evidence and stable pagination. Synthetic 2,000-match page took 8–37 ms; cycle list approximately 5 ms. External requests and production writes in those SQL checks: zero.
- Live owner API verification: cycle, evaluation, history, summary, series and event endpoints returned 200; measured route timings approximately 23–92 ms in the local service client. Totals reconcile against the considered scope of 2,000 fixtures.
- No Android/website product redesign or Google Play release. Existing constants deletions remain. No new service, Grafana instance, Modal workload, quota ramp or Highlightly cutover.


### Production browser verification — cycle workspace

The normal telemetry URL reloads into authenticated owner data. Verified the three-pane cycle/match/evidence view, compact cycle-to-match navigation, country-name tiles, dark-only styling and WIB dates. A real Getafe–FC Lugano evaluation showed candidate rules, cached AI decision, candidate acceptance and outcome. Its related log scope reported exactly four matching events and four loaded records. General log scope is independent of workspace selection; selecting a match pins its original cycle during live refresh. Blank planned-only evidence explains that no search/AI request ran.

Operations uses the existing searchable run table with state filters, stable paging, inspection and retry controls, separated from the delivery outbox. Old failed/blocked jobs remain available even though monitoring cycles were reset. Canonical history starts at 1 per pipeline and advances normally. Search rollout remains dry_run, fraction 0.

Final verification includes 42 focused tests, production build success, owner route checks and temporary-table SQL tests for 90-day compaction, idempotent cycles, shared child work, evidence and pagination. Latest synthetic page: approximately 10 ms for 50 records from 2,000 match evaluations; cycle page approximately 5 ms. These are SQL read measurements, not certification of provider throughput. Existing upstream feed failures remain operational facts, not hidden or bulk-retried by this update.

The temporary responsive wrapper was removed. Screenshots and credential-free source snapshot are in the Codex task workspace. Android's existing deleted constants remain untouched; no Android or Play release was attempted for this Wasm-only change.

Final publication: both Hosting sites and the Oracle service deployed successfully. Browser verified the final Operations state filter against preserved failed jobs, with pagination and retry controls present; no retry was executed. Normal telemetry URL renders correctly. Anonymous monitoring returns 401 and public status returns 200. Temporary responsive wrapper removed. Cycle counters continue advancing from the fresh history boundary.


## Personal monitoring and searchable evidence — 24 October 2026

Owner console rebuilt around Fixtures & Live Scores, YouTube API Search and WebSub Uploads, with canonical team search, grouped cycle/match logs, candidate/check accordions and correction/Undo. Retained history is preserved. Dashboard distinguishes catalog totals, current-season coverage, verified country coverage and actual quota buckets. Live pipeline producer is Cloud Run revision fc-worker-00029-57t; processing and owner APIs run on Oracle.

Verified backup: /home/ubuntu/footballclips-upgrade-backups/personal-monitor-20261024T091743Z. Both Hosting targets and Oracle backend deployed. Focused tests: 56 passed. Real SQL temporary-table correction checks passed without canonical writes or external calls. Owner API checks passed; anonymous monitoring returns 401. Country-watchable totals reconcile with country summaries. Real published-video canary cycle #128 verified one candidate with Groq, one general metadata unit and zero searches; existing preferred highlight remains visible through the app API.

Search remains dry_run/zero allocation pending the existing full-day gate. Owner-authorized 10% activation is queued for 24 Oct 2026, 23:26:17 WIB, contingent on quota reconciliation, executor freshness, canary and recent critical failures. Further ramps remain owner-controlled. Historical failed feed jobs remain visible; this release does not hide or certify them.

Remaining compatibility limitation: old direct Supabase readers of immutable historical rows do not consume owner-rejection overrides. Owned endpoints and the legacy response adapter respect them. Intentional constants deletions and separate Android compilation blockers remain untouched. See docs/PERSONAL_MONITOR_GUIDE.md for the owner workflow, pipeline timing and deployment details.

Final continuation verification at approximately 17:55 WIB: final Wasm production build, Spotless and Detekt passed; both Hosting sites released successfully. Phone search fields stack at 390px and desktop uses four metric columns. Browser verified real canary candidate accordions, actual Groq model evidence, grouped team logs, separate WebSub/fixture logs, 90-day search, and the supported competition registry with the Liga Indonesia logo. Fresh API checks passed, including stale-live drilldown. Screenshots: build/monitor-proof. Pure domain contracts/models now live in commonMain. Existing build warnings and Android blockers are not described as resolved.


## Live Match Minutes, Competition Expansion & Oracle VM n8n Audit — 24 October, 22:50 WIB

- **Live Match Minute Across Platforms**:
  - Live ESPN probe confirmed `status.displayClock` returns real-time minute strings (e.g. `74'`, `90'+5'`, `HT`).
  - Android CMP (`composeApp`): Refactored `MatchStatusBadge` in `MatchCardComponents.kt` into decomposed sub-composables (`LiveMatchStatusBadge`, `FinishedMatchStatusBadge`, `UpcomingMatchStatusBadge`), strictly adhering to the <=40 lines composable rule, <=250 lines file budget (244 lines total), and zero wildcard imports. Dynamically displays `LIVE ${match.displayClock}` (or `HT` if paused).
  - Next.js Web (`web`): Updated `FixtureItemCard.tsx` and `MatchSidebar.tsx` to display real-time live minutes (`LIVE 74'`, `HT`). Production `npm run build` compiled 526/526 pages successfully with zero errors.
- **Liga Indonesia (IDN) Native ESPN Migration**:
  - Verified live ESPN `idn.1` endpoint returns active 2025/2026 fixtures, scores, and live clocks (`HTTP 200 OK`, `Indonesian Super League`).
  - Standardized `IDN` into primary ESPN client (`espn_client.py`), phasing out reliance on brittle HTML scraping of `ileague.id` while retaining official channel mappings (`@liga1match`).
- **Continental & South American Competition Expansion**:
  - Verified and integrated 6 high-impact tournaments on ESPN's zero-quota API (`HTTP 200 OK`):
    1. **AFC Champions League Elite (`ACL` / `afc.champions`)**: Primary `@TheAFCHub` (ID: `UCnj0TjaM0wyxkAWW_nz8_1g`), backups `@beINSPORTS_Asia`, `@SonySportsNetwork`.
    2. **CONMEBOL Libertadores (`LIB` / `conmebol.libertadores`)**: Primary `@libertadores` (ID: `UClgFf9mS1mZI2D_vo-ZTpmQ`), backups `@ESPNFans`, `@CazeTV`.
    3. **CONMEBOL Sudamericana (`SUD` / `conmebol.sudamericana`)**: Primary `@sudamericana` (ID: `UCw7eDYHnA-2PIr-qIldYeBQ`), backups `@ESPNFans`, `@CazeTV`.
    4. **CONMEBOL Recopa (`REC` / `conmebol.recopa`)**: Primary `@libertadores`, backup `@ESPNFans`.
    5. **Brasileirão Série A (`BRA` / `bra.1`)**: Primary `@brasileirao` (ID: `UCrf4Fr6uTCoU9RkYa5CbzcA`), backups `@CazeTV` (ID: `UCZiYbVptd3PVPf4f6eR6UaQ`), `@TNTSportsBR`.
    6. **Argentine Liga Profesional (`ARG` / `arg.1`)**: Primary `@ligaprofesional` (ID: `UCJmCVoUfCBQb9lcfXIS8nXQ`), backup `@ESPNFans` (ID: `UCFmMw7yTuLTCuMhpZD5dVsg`).
  - Added primary and verified backup broadcaster mappings to `constants.py` and `highlight_service.py` to ensure resilient 0-quota RSS discovery across jurisdiction embargoes.
- **Oracle VM Architecture & n8n Automation Audit**:
  - Audited live super-node (`168.110.196.30`): 21.6 GB RAM available (90% free headroom), 146 GB free NVMe disk.
  - Verified active n8n production workflow: `fcOpsMaintenance` ("FootballClips — hourly maintenance verification") triggered hourly at minute 7 (`7 * * * *`) and on-demand via authenticated POST to `http://footballclips-operations:8093/internal/maintenance`.
  - Documented `footballclips-operations` (:8093) microservice and pgAdmin forward-authentication architecture in `oracle/README.md` and `oracle/ORACLE_OPERATIONS_MANUAL.md`.
- **Verification Outcomes**:
  - Full Python worker test suite: **110 passed** in 19.65s (`uv run pytest`).
  - Next.js Web build: **526/526 pages generated** with zero TypeScript or Turbopack errors.
  - Kotlin WasmJs build: `./gradlew :webApp:compileKotlinWasmJs` **UP-TO-DATE** in 3s with zero warnings.
