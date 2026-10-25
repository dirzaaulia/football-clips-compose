# FootballClips operations

## Open the app

- Website: https://fc.dirzaaulia.com
- Public status: https://fc.dirzaaulia.com/status
- Owner console: https://fc.dirzaaulia.com/telemetry/
- Standalone owner console: https://footballclips-telemetry.web.app
- Use Google sign-in for `dirzaaulia11@gmail.com`. Supabase dashboard sign-in is separate from app sign-in.
- Owner permission is checked by the server. Credentials are never included in the Wasm bundle.

## Where processing and data live

| Component | Responsibility |
|---|---|
| Oracle PostgreSQL, database `footballclips` | Canonical matches, additive video candidates, jobs, quota reservations, events, outbox, reviews |
| Oracle `footballclips-operations` | Immediate WebSub processing, RSS fallback, matching, country selection, metadata refresh, owner/catalog APIs |
| Cloud Run `fc-worker` | Bounded fixture/calendar runs, workload-identity quota reconciliation, Gemini review and existing FCM bridge |
| Cloudflare proxy | Country-aware delivery, short conditional caching and versioned cache namespaces |
| Existing Supabase | Sign-in and backward-compatible football data for older app versions |
| Firebase Hosting | Next.js static website and Compose Wasm console |
| n8n 2.43.6 | One private hourly maintenance workflow; no external messaging destinations |

YouTube hosts playback. This pipeline stores references and metadata, not video files. No Modal work or new paid infrastructure was added.

Oracle operations is limited to one CPU and 1 GiB RAM, with four outbound requests maximum and a worker reserved for immediate events. Data belongs to PostgreSQL, not the disposable container filesystem.

## Everyday owner controls

### Liga Indonesia

`IDN` now covers the senior top division, BRI Super League. The current 2026/27 season uses the official I.League fixtures and standings pages; ESPN `idn.1` is retained for the 2025/26 archive because its October 2026 feed is unavailable.

- The initial ESPN archive imported 306 fixtures. Existing protected historical rows were not overwritten.
- The official Liga1Match channel was approved after matching a video embedded by I.League to its verified YouTube channel ID. WebSub subscription confirmation is recorded.
- Current official fixture pages refresh every 30 minutes when idle and every two minutes around scheduled matches. The previous round is rechecked every six hours; standings refresh every 30 minutes.
- Only explicit provider status markers establish full time. Kickoff times must be published by the source or already verified in the catalog. Detail-page failures wait an hour between attempts, with eight attempts before owner retry is required.
- Initial current-season result backfill is incomplete while provider detail pages time out. One independently verified result and its official playable highlight were admitted during setup. Upcoming fixtures and all 18 standings rows are available.
- Fixture IDs are stable, namespaced canonical IDs derived from the season and full home/away fixture identity, with persisted aliases and collision checks. They do not depend on upload order or YouTube publication time.
- The private executor has limited table grants for this ingestion. Public anonymous database privileges were not expanded; row security restricts competition metadata writes to IDN.
- Do not use the older scratch RSS importer as the authoritative fixture source: it assigns IDs by upload order and does not verify actual kickoff times.

### Wasm loading recovery

Hosting canonicalizes `/telemetry/` to `/telemetry`. The loader must never redirect back to the slash version. Its asset base remains `/telemetry/`, and the HTML route uses `no-cache, must-revalidate`.

An already-open tab can retain the old redirect for its previous one-hour cache lifetime. A hard refresh, or opening `/telemetry?release=20261024`, loads the corrected document. Owner Google sign-in and live private data rendering were verified in the browser after this fix.

- **Overview:** actual freshness, failures, queues, measured latency and coverage.
- **Investigate:** filter structured events by fixture/job/trace, inspect a timeline, save a private view, export bounded CSV/JSON.
- **Coverage:** review competitions and source ownership, maintain provider/team identities and aliases, edit market policy, inspect archive coverage.
- **Quota & Storage:** inspect actual Google usage, pacing and reconciliation age; model capacity using measured database size and storage growth.
- **Operations:** retry an individual job or outbox delivery, inspect subscriptions, archives, backups and middleware comparison, advance a rollout stage.

New competitions and sources require approval. A verified channel ID establishes identity; ownership evidence and permission review are still required. Invalid fixture-provider mappings stay blocked for owner review.

Country completion uses the union of verified videos. External YouTube playback counts when embedding is disabled. Unknown metadata is not counted as playable. China-only remaining gaps are labeled platform limited and do not cause endless active searches; passive updates and metadata refresh continue.

## Discovery rollout

1. Observe a complete dry-run day after the final verified executor release.
2. In Operations, advance to 10%, then 50%, then 95%, with successful full-day checks between stages.
3. Failed jobs and stale quota reconciliation block advancement. Review blocked source/provider configuration separately.
4. The search target at full allocation is 47,500 of 50,000 calls per Pacific day; general API cap is 57,000 of 60,000 units. Application search ceiling is 60/minute.
5. Reconciliation occurs every five minutes. API discovery pauses after 15 minutes without it; RSS/WebSub continue.
6. Useful work determines actual use. Do not manufacture repeated queries to fill unused allowance.

The owner alone chooses **Use our catalog only**. Highlightly remains enabled during evaluation. The switch is reversible and old endpoint adapters stay available.

Database benchmarks at 17/50/100 competitions establish read-query behavior, not proven live ingestion throughput. WebSub p95 and publication-to-discovery measurements need real representative notifications; never report the latency targets as achieved without samples.

## Storage and archives

- Canonical historical seasons below 2026 are protected. Never delete canonical history automatically to free space.
- Pause optional discovery below 40 GiB free and optional writes below 25 GiB; maintain a 20 GiB floor.
- Compact operational searches after 90 days. Diagnostic samples last seven days. Refresh or remove YouTube metadata within the applicable retention period.
- Archive by actual competition/season using checksummed manifests and verified restoration. Archive files live under `/home/ubuntu/footballclips-operations/archives`.
- Storage estimates for 500 matches/competition/year over ten years: 100 competitions approximately 4.77 GiB compact live data, 250 approximately 11.92 GiB, 500 approximately 23.84 GiB. Backups, working space, logs and other services require additional capacity.

## Backups and recovery

- Existing daily SQL backups: `/home/ubuntu/jarvis/storage/backups`, 03:00 UTC, fourteen-day retention.
- A backup was restored and queried in an isolated database. Proof: `restoration-verification.json` in that directory.
- Pre-upgrade and pre-final backups: `/home/ubuntu/footballclips-upgrade-backups/20261023T082612Z` and `20261023-final`.
- Backups include PostgreSQL, configuration, n8n SQLite and encryption material. Keep their permissions private.
- Remote SQL backup copies are **not verified**. Unrelated Backblaze download jobs are not proof of a database backup.
- Before recovery, pause writers and inspect the backup manifest. Restore into an isolated database first; production restoration needs a deliberate recovery decision.
- Application rollback requires a compatible Cloud Run bridge and fixture-outbox trigger state. Do not independently disable the trigger or deploy an old direct-delivery worker while the other side remains active.

## Verified security boundaries

- Legacy Supabase `worker_cycles` and `worker_events`: anonymous reads denied; owner-only authenticated reads; service writes retained.
- Oracle private telemetry and operational APIs: anonymous requests denied and responses are never publicly cached.
- Oracle football catalog writes: service credential required, including both raw and `/rest/v1/` paths. Public reads remain available.
- n8n editor stays private. Keep its credential encryption material with its database backup.

## Verification tools

`worker/operations/verify_live.py` performs read checks and sends empty, denied anonymous write probes. `verify_isolated.py` uses connection-local temporary tables for matching, quota, API, transactional trigger, archive and scale checks; it performs no production fixture writes or external API requests.

Android sources compile, but this rollout does not publish an Android release to Google Play. Existing unrelated repository lint failures were preserved; the production dependency audit reported zero vulnerabilities at verification time.


## Browser database administration (pgAdmin)

Open **https://fc.dirzaaulia.com/database** and select **Open pgAdmin**. If asked, use the approved Google owner account. There is no separate pgAdmin password. Access lasts one hour; return to the entry page and reopen it to renew. Owner membership is enforced by the operations backend, not a browser-only check.

In the left tree: **Oracle databases → Oracle VM - PostgreSQL 16 (admin) → Databases → footballclips**. The other current databases are `loottrack` and `postgres`. Expand **Schemas → public → Tables** to browse football data; `fc_ops` contains private pipeline state. The registered connection has administrative rights, so intentional edits take effect in the live database.

- Public HTTPS reverse proxy: `https://api.fc.dirzaaulia.com/pgadmin/`; anonymous requests return to the owner entry page.
- Docker container `footballclips-pgadmin`, pinned official 9.18 ARM64 digest. No new host port; outbound SQL stays inside `jarvis_jarvis_net`.
- Files: `/home/ubuntu/footballclips-pgadmin/compose.yml`, `config_local.py`, `data/pgadmin4.db`, `data/storage`. Private bootstrap credential is unused for web login; do not share secret files or place them in Git.
- PostgreSQL data remains in the existing PostgreSQL service. pgAdmin stores connection settings, preferences, uploaded/exported files and query history; it does not move the football catalog into its own SQLite file.
- Settings snapshots: `/home/ubuntu/footballclips-pgadmin/backups/pgadmin_*.tar.gz`, 14-day local retention, invoked from the existing 03:00 UTC PostgreSQL backup job. Snapshot SQLite with its backup API and verify integrity before packaging. The initial snapshot was verified. No remote upload has been added.
- Restore: stop pgAdmin; preserve its current files; extract a selected snapshot to a private staging directory, restore its matching 9.18 state/configuration and UID 5050 ownership, then restart and verify login and server registration. Never run a newer setup image against an older running pgAdmin database.
- To withdraw access immediately, remove the owner from `FC_OWNER_EMAILS` and restart the operations service. Rotating `FC_INTERNAL_TOKEN` also invalidates existing database cookies; coordinate other internal clients before doing so.
- Caddy's `route` block intentionally orders identity sanitization before `forward_auth`; directive sorting without that block can erase the authenticated identity. Trusted proxy ranges include `::ffff:172.18.0.0/112` because Gunicorn sees mapped IPv6 peer addresses.

### Telemetry canonical URL

Use **https://fc.dirzaaulia.com/telemetry**. The HTML must not redirect to a trailing slash: Hosting already canonicalizes that slash away. Keep the document uncached and static bundle names/releases synchronized. The canonical browser reload was verified with live owner data. Loading failures show a retry action; no timer should report failure solely because it cannot see the Wasm canvas in the light DOM.


## Monitoring console

Open https://fc.dirzaaulia.com/telemetry and use the existing owner sign-in. The interface is dark only. The global range defaults to one hour and applies to event activity, logs and charts; outstanding job cards explicitly describe current state. All human dates use WIB (GMT+7), independently of the browser timezone.

Select Pipelines for job state, attempts, last activity and reasons; select a row for recorded stages and related logs. Select Logs for severity, pipeline, text and advanced identity filters. Scrolling into history pauses following; use the new-events action to return to current activity. Loaded rows and trace pages remain bounded. CSV/JSON exports cover loaded records and retain UTC alongside a readable WIB field.

Monitoring API additions live under owner-protected /api/ops/v1/monitor: summary, series, events, context/{event_id}, runs, runs/{job_id}, traces/{trace_id}. Summary/series/events share the same predicates. Anonymous access must remain denied; these responses must never enter public caches. No service credential belongs in Wasm assets.

For rollback, retain the verified monitoring-20261023T194151Z snapshot and compatible previous static artifacts. APIs are additive. Preserve the existing telemetry URL, owner allowlist and Highlightly control. Do not restore the user's deleted constants merely to build: use the documented isolated compatibility build until the owner resolves those imports.


## Cycle → match → result workspace

Pipelines defaults to YouTube discovery. Choose a real dispatch cycle, a considered match and its evidence. Instant uploads and fixture synchronization have separate tabs and counters. Desktop panes can be resized by dragging their dividers or using the divider buttons. Narrow screens follow the same hierarchy with breadcrumbs.

A dispatch may finish while child jobs wait or retry. Planned only means no API search ran; skipped outcomes explain coverage, cooldown, approval or admission limits. Shared channel feed work and unmatched uploads belong to the cycle without fabricated match identities. Logs default to cycle/match grouping, with chronological mode available. Evidence and history have bounded pagination; exports describe loaded records.

Owner APIs under /api/ops/v1/monitor:
- GET cycles: time range, kind and timestamp/UUID cursor; default 50, maximum 100.
- GET cycles/{uuid}: dispatch facts, outcome counts and downstream job state.
- GET cycles/{uuid}/matches: team/reason text, outcome, competition and match cursor; includes compact skipped decisions.
- GET evaluations/{uuid}: decisions, events, attempts and older-event cursor.
- GET matches/{id}/history: evaluations and skipped decisions across cycles.
- Existing summary/series/events accept cycle_id and evaluation_id; all use the same scope predicates.

Structured history is retained for 90 days before daily compaction. Candidate metadata continues to expire under the existing 29-day refresh policy. Keep the 20 GiB disk floor and optional-work thresholds. Do not equate a linked video, completed job or FCM acceptance with verified device delivery.

Rollback keeps the pre-cycle backup and previous Hosting artifacts. The schema is additive; do not restore football tables or quota state just to undo a UI release. Reverting the fixture producer also requires its matching receiver; current receiver intentionally requires structured records.
