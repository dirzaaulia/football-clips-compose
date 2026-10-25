# Personal pipeline monitor

Open https://fc.dirzaaulia.com/telemetry and use the existing owner sign-in.

## Find why a highlight is missing

1. Search for a team and select its identity. Suggestions include competition context; similarly named teams remain separate.
2. Choose Last 24 hours, or Search all retained history for up to 90 days.
3. Open Pipelines, choose YouTube API Search, then a cycle and match.
4. Expand each candidate video. Its checks show where it passed, was rejected, waited, or failed. AI evidence contains the actual provider/model and its returned classification explanation, not private reasoning.
5. Use Logs for searchable cycle/match groups. Message search also finds video IDs and channels. Clear the team filter to inspect unmatched uploads.
6. For an incorrect candidate, use Wrong highlight — find replacement. Undo restores eligibility; it does not guarantee that the same video becomes preferred again.

## How discovery runs

- Fixtures & Live Scores records provider changes, score/status comparisons, saves and delivery. Tracked live matches are due approximately every 60 seconds, subject to backoff. Provider confirmation, rather than elapsed match age, establishes full time.
- WebSub Uploads receives published-video notifications without a search request. Verification can still consume the general YouTube API bucket. RSS is explicitly identified as fallback.
- Automatic API search starts one hour after confirmed full time. Retries are 30 minutes through six hours, two hours through one day, six hours through three days, daily through seven days, and seven days for older incomplete work.
- Published uploads can be verified immediately. Fresh due work takes priority over approved incomplete archive fixtures from 2025 onward. Completed country coverage stops active searches.
- Verification checks source approval, content, duration, fixture identity and availability. At most five candidate AI reviews are admitted per search job. Each highlight-review gateway request allows three provider attempts within its 15-second caller deadline. Explicit AI rejection is never converted into acceptance by fallback.
- Selection commits before retryable replication/cache/notification work. FCM acceptance does not prove delivery to a device.

## Read the dashboard correctly

Catalog totals include stored history; coverage defaults to each competition's current season. Team/competition/season selections narrow those records. The operational time range limits event activity, rather than deleting older catalog matches from totals. Missing percentage uses finished matches as its denominator.

Country availability defaults to Indonesia and is based on fresh YouTube metadata. External YouTube playback, embedding, geographic blocking and unknown verification are separate concepts. Metadata cannot guarantee access through every network.

Every operational date is displayed in Asia/Jakarta (WIB, GMT+7). YouTube allowance resets follow the Pacific quota day; the reset is shown in WIB. Search and general units are separate buckets.

## Operations and remaining limits

- Operations preserves the existing owner controls, queue inspection/retries, source approvals and Highlightly switch.
- The requested initial 10% search rollout is queued for 24 October 2026 at 23:26:17 WIB. It remains a zero-allocation dry run until the full-day, quota reconciliation, executor and canary gates pass. Further increases require the owner.
- Existing failed RSS/feed jobs are preserved and visible. This monitoring release does not establish that all upstream sources are healthy.
- The owned APIs and compatibility adapter apply per-match owner exclusions. Old clients that read immutable historical rows directly from Supabase do not yet consume the additive override sidecar. That separate legacy-client limitation remains; archived football rows are not rewritten.
- Exports contain the records loaded in the view and say so explicitly. They are not an unbounded whole-database export.
- Android compilation remains blocked by intentional constants deletions and existing call-site/type errors. No Android or Play release is included.

## Deployment evidence

Backup: /home/ubuntu/footballclips-upgrade-backups/personal-monitor-20261024T091743Z.

The focused Python suite passed 56 tests. Temporary PostgreSQL checks passed exclusion, idempotence, Undo, stale-delivery protection and archive safety with no canonical writes or external API calls. The real published-video canary used one general metadata unit and no search calls; Groq accepted the candidate, and the existing preferred selection was verified through the app API.

Screenshots and build output: build/monitor-proof and build/personal-monitor-final-build.log. No new paid service, Modal workload or infrastructure expansion was added.
