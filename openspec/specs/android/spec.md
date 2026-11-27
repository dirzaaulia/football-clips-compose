# Android KMP Specification (`composeApp/` & `androidApp/`)

## Purpose
Defines Kotlin Multiplatform (KMP) architecture, MVI state contracts, dynamic backend-driven competition support, inline playback, and UI invariants.

## Requirements

### Requirement: Clean MVI & Multiplatform Layering
- `commonMain` MUST contain zero `android.*` or `java.*` imports.
- ViewModels MUST remain $\le 150$ lines, exposing immutable `UiState` via `StateFlow` (`SharingStarted.WhileSubscribed(5_000)`), sealed `UiAction`, and single-shot `Channel<UiEffect>(Channel.BUFFERED)`.
- All Compose files MUST stay $\le 250$ lines (hard ceiling 300), composables $\le 40$ lines, zero wildcard imports, and mandatory trailing commas.

### Requirement: Dynamic Backend-Driven Competitions
- Home, Fixtures, Filters, and Standings screens MUST derive active competitions dynamically from PostgreSQL `competitions` (`is_active = true`, ordered by `display_order`).
- `LeagueUtils.kt` and `StandingsQualification.kt` provide graceful display-name and qualification-zone fallbacks without gating or hiding newly added backend competitions.

### Requirement: Playable Shorts & Live Match UX
- Shorts feed MUST query `order=published_at.desc,hype_score.desc` and play inline via `YouTubePlayerView` without `NOT_EMBEDDABLE` errors.
- `HomeLiveMatchSection` MUST auto-advance horizontally on `LazyRow` every 4s when multiple live matches are active, pausing whenever `listState.isScrollInProgress` is true.
- Zero raw Unicode emojis and zero `CircularProgressIndicator` in production UI; vector icons and 1:1 shimmer skeletons only.
