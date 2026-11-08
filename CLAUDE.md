<!--
Authoritative Rules Source: AGENTS.md
Do not diverge from AGENTS.md. All changes must be synchronized.
-->
# Universal Engineering Guidelines & Standards (Claude)

This repository enforces strict engineering invariants and a 4-Gate verification workflow across Android/CMP (Kotlin), Web/Telemetry (Next.js), and Backend (Python):

## 1. Hard Code Constraints & Line Budget Limits
- **Maximum File Length**: **250 lines** (hard ceiling **300 lines** including imports and comments).
- **Maximum Composable / Function Length**: **40 lines**. If a composable exceeds 40 lines, decompose into dedicated sub-composables.
- **Maximum ViewModel Length**: **150 lines**. ViewModels are pure state orchestrators; offload business logic to Domain UseCases and data mappers.
- **Zero Wildcard Imports**: `import foo.*` is strictly forbidden.
- **Mandatory Trailing Commas**: Required on all multi-line parameter lists, argument calls, and collection literals.
- **Historical Archive Immutability**: Seasons `< 2026` must remain immutable. Never run unconstrained delete/purge operations against archive records in PostgreSQL or Supabase (`protect_archive_matches`).
- **Zero Secret & IP Exposure**: Never hardcode production IP addresses (`168.110.196.30`) or credentials. Always read from environment variables or `getSecret()`.
- **Lean Database Storage**: Never persist tactical boxscore coordinates, formations, statistics, or editorial stories to PostgreSQL `matches`. Stream them 100% on-demand from ESPN API.

## 2. 4-Gate Engineering Workflow
Before completing any task or proposing changes:
- **Gate 1**: Verify line budgets, zero wildcard imports, trailing commas, and secret scrubbing.
- **Gate 2**: Update structured Implementation Plan and Walkthrough artifacts for multi-file changes.
- **Gate 3**: Run automated verification:
  - `./gradlew :composeApp:compileAndroidMain` (must pass with 0 errors)
  - `npm run build` in `web/` (must compile 100% of routes)
  - `uv run pytest worker/tests/` (must pass 100% of tests)
- **Gate 4**: Keep `AGENTS.md`, `CLAUDE.md`, `.cursorrules`, and `.github/copilot-instructions.md` synchronized.

For detailed domain rules and deployment grammar, consult [AGENTS.md](AGENTS.md).
