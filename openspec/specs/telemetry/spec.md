# Telemetry Specification (`web/src/app/telemetry/` & `core-go`)

## Purpose
Defines observability, per-match waterfall diagnostic traces, and real-time pipeline health contracts between `fc-daemon` and the Next.js Telemetry Hub.

## Requirements

### Requirement: Mandatory Per-Match Waterfall Diagnostics
Every match highlight discovery execution MUST emit and persist a structured waterfall trace containing:
1. `[CHANNELS]`: Official club, league, and broadcaster targets queried.
2. `[CANDIDATES]`: Discovered videos (`video_id`, `title`, `channel_handle`, `duration_seconds`, `is_embeddable`, `watchability_pct`).
3. `[AI_VERDICTS]`: Per-candidate AI reranking score (`0-100`) and rationale, or `[DETERMINISTIC_FALLBACK]` verdict if AI Gateway is unreachable.
4. `[OUTCOME]`: Final state (`LINKED`, `NOT_FOUND`, `COOLDOWN_PROTECTED`), linked video ID, and worldwide watchability percentage.

### Requirement: Strict English Telemetry & Coverage Metrics
- All telemetry logs, event labels, and UI diagnostics MUST be strictly English.
- Telemetry Hub (`CoverageMetricsDeck`, `LiveScoresPipelineView`, `GroupedWaterfallsView`) MUST compute competition coverage, highlight hit-rate, and live sync cadence from PostgREST without dumping raw unbounded table rows.
- Loading states MUST use 1:1 structural shimmer skeletons (`TelemetrySkeleton.tsx`), never circular spinners.
