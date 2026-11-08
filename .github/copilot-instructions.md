# GitHub Copilot Instructions for FootballClips
# Authoritative Source: AGENTS.md

## Strict Invariants
1. Maximum File Length: 250 lines (hard ceiling: 300 lines).
2. Maximum Composable / Function Length: 40 lines.
3. Maximum ViewModel Length: 150 lines.
4. Zero Wildcard Imports: Always use explicit imports.
5. Mandatory Trailing Commas on all multi-line parameter lists, argument calls, and collection literals.
6. Immutability of Archive Matches: Never purge or delete matches with season < 2026.
7. Zero Hardcoded Secrets or IPs: Never expose 168.110.196.30 or credentials in code or docs.
8. Lean Database: Do NOT persist tactical formations, statistics, 2D coordinates, or events to PostgreSQL. Stream on-demand from ESPN API.

## 4-Gate Workflow
- Gate 1: Check code budgets and invariants.
- Gate 2: Update implementation plan and walkthrough artifacts for major changes.
- Gate 3: Verify with `./gradlew :composeApp:compileAndroidMain`, `npm run build` (in web/), and `uv run pytest worker/tests/`.
- Gate 4: Mirror all updates to AGENTS.md, CLAUDE.md, .cursorrules, and .github/copilot-instructions.md.
