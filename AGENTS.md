# Android/Kotlin Development Rules

## 1. Master Android & Compose Multiplatform (CMP) Standards
Applies strictly to both **`composeApp` (Android Native)** and **`webApp` (WasmJs Telemetry)**:

### Hard Code Constraints & Line Budget Limits
- **Maximum File Length**: **250 lines** (Hard ceiling: **300 lines** including imports and comments).
- **Maximum Composable / Function Length**: **40 lines**. If a composable exceeds 40 lines, decompose into dedicated sub-composables.
- **Maximum ViewModel Length**: **150 lines**. ViewModels are pure state orchestrators; offload business logic to Domain UseCases and data mappers.
- **Decomposition Invariant**:
  - `FeatureScreen.kt`: Stateful Route + Stateless Root layout (~80–120 lines).
  - `FeatureComponents.kt`: Reusable sub-composables (~100–150 lines).
  - `FeatureState.kt`: `UiState`, `UiAction`, `UiEffect` definitions (~40–80 lines).
  - `FeatureViewModel.kt`: State orchestration & UseCase invocation (~80–120 lines).

### Code Hygiene & Static Analysis
- **Zero Wildcard Imports**: `import foo.*` is strictly forbidden. Every import must be explicit.
- **Zero Dead Code / Unused Imports**: All unused imports must be stripped before finalizing any edit.
- **Mandatory Trailing Commas**: Required on all multi-line parameter lists, argument calls, and collection literals.

### Modern Architecture & Multiplatform Layering
- **Clean Architecture**:
  - **Presentation Layer**: Stateless composables with hoisted state, ViewModels, `UiState`, `UiAction`, `UiEffect`.
  - **Domain Layer**: Pure Kotlin in `commonMain` with ZERO `android.*` or `androidx.compose.*` dependencies. Single-action UseCases with `operator fun invoke`.
  - **Data Layer**: Repositories, Remote Sources (Ktor 3), Local DB (Room KMP / DataStore KMP), DTOs, explicit mappers (`toDomain()`, `toEntity()`).
- **Reactive State Management (MVI + Flow)**:
  - Single immutable `UiState` with sensible defaults.
  - Sealed interface for `UiAction`.
  - Buffered channel `Channel<UiEffect>(Channel.BUFFERED).receiveAsFlow()` for single-shot side effects.
  - Expose `StateFlow` via `stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = ...)`.
  - Suspend functions must be main-safe (`withContext(ioDispatcher)`).
- **Compose Multiplatform UI Guidelines**:
  - Stable list keys: Always pass stable keys to `items(..., key = { it.id })`.
  - Use `kotlinx.collections.immutable.ImmutableList` / `ImmutableSet` for stable collections.
  - Defensive edge-to-edge system insets (`statusBarsPadding()`, `navigationBarsPadding()`, `safeDrawing`).
  - Adaptive layouts adapting to `WindowWidthSizeClass` (Compact, Medium, Expanded).
  - Minimum **48.dp** touch targets (`Modifier.minimumInteractiveComponentSize()`).
  - Meaningful `contentDescription` on interactive icons; `null` only for decorative icons.

---

## 2. General Development Guidelines
- **Language & Paradigms**: Kotlin is the primary language. Use idiomatic Kotlin, Coroutines, StateFlow, and modern declarative Compose patterns.
- **UI Framework**: Prefer Jetpack Compose for Android and Compose Multiplatform (CMP) for shared UI.
- **Kotlin Multiplatform (KMP)**:
  - Keep shared business logic, data models, repositories, and ViewModels in `commonMain`.
  - Do not introduce Android-specific or Java-specific imports into `commonMain`.
  - Place platform-specific code in `androidMain`, `wasmJsMain`, or appropriate platform source sets using `expect`/`actual` definitions cleanly.
- **Architecture & DI**:
  - Maintain the existing MVVM + Repository pattern with Koin dependency injection (`appModules`).
  - Prefer existing project patterns over introducing new frameworks unnecessarily.
  - Reuse existing dependencies before adding new ones.
- **API & Documentation**:
  - Do not invent library APIs.
  - When API/library behavior may have changed, consult Context7 MCP or authoritative documentation (`search_android_docs`).
  - Use `find-skills` to discover specialized agent skills when embarking on new task domains.
- **Code Modifications & Verification**:
  - Prefer surgical, incremental changes over large rewrites.
  - Do not modify unrelated files.
  - Verify compilation (`compileKotlinWasmJs` or `compileDebugKotlinAndroid`) after significant changes.

---

## 3. Project-Specific Guidelines (FootballClips)
- **Target SDK**: Compile & Target SDK 37 (AGP 9.4.0, Kotlin 2.4.21).
- **Gradle Configuration**: `android.newDsl=false` is required in `gradle.properties` when pairing Kotlin Multiplatform plugin with `com.android.application` in AGP 9.
- **Dynamic Track-Based Version Naming**:
  - Production track: `$major.$minor.$patch ($buildNumber)` e.g. `3.0.1 (89832569)`.
  - Beta / Alpha / Internal tracks: suffixed with `-beta.$buildNumber`, `-alpha.$buildNumber`, `-internal.$buildNumber`.
- **Historical Archive Immutability**:
  - Historical archive seasons (`season < 2026`) must remain immutable. Never run unconstrained delete/purge operations against archive records in PostgreSQL or Supabase.
  - The database-level `protect_archive_matches` trigger silently rejects deletions on archive rows.
- **Secret Management**: Always use the `getSecret(key)` helper in `androidApp/build.gradle.kts` to fallback gracefully between `local.properties`, Gradle properties (`-PKEY=...`), and Environment Variables (`ENV['KEY']`).
- **RevenueCat Billing**: Ensure `BuildConfig.REVENUECAT_API_KEY` is checked for `isNotBlank()` before invoking `Purchases.configure(...)` in `Application.kt`.
- **AdMob Next-Gen Native Ads**:
  - Use `com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView`.
  - Maintain `MediaView` dimensions of at least `120dp x 120dp` to adhere to Google AdMob policy guidelines.
  - Use `FIT_CENTER` scaling to preserve media aspect ratios.

---

## 4. Worker & Telemetry Standards
- **Logging Language**: Strictly English across all worker logs, event messages, subsystem actions, and telemetry UI. No Indonesian or informal phrases in production telemetry.
- **Fixture Synchronization Logging**:
  - Do NOT dump raw database records into the telemetry view or make unnecessary large database queries.
  - Log specific granular match operations:
    - `[NEW FIXTURE]`: New upcoming fixtures or newly added matches with kickoff time.
    - `[LIVE SCORE]`: Live score changes and goal alerts with live score.
    - `[KICKOFF]`: Kickoff events when a match moves into `IN_PLAY`.
    - `[FULL TIME]`: Final whistle events with full time score.
    - `[STATUS UPDATE]`: Explicit match status updates.
    - `[REGRESSION BLOCKED]`: Protection events preventing state reversals.
- **Highlight Pipeline Logging**:
  - Strictly adhere to the step-by-step trace:
    1. `[PROCESS]`: Search start with query and official club channel targets.
    2. Candidate detection: Number of valid candidate videos found per channel.
    3. `[AI GEMINI]`: AI reranking evaluation of candidates using Gemini 3.8 Flash.
    4. `[FALLBACK SELECT]`: Title regex ranking if Gemini hits quota limits or timeouts.
    5. `[LINKED]` or `[NOT FOUND]`: Final YouTube video ID linked and saved to Supabase, or marked as unavailable.
- **Sync Intervals & YouTube Quota Tiering**:
  - Live matches: 1 minute (60s) tick for semi-live score and status updates.
  - Kickoff within 45 minutes: 30 minutes.
  - Standard idle / off-peak: 2 hours.
  - YouTube Highlight Scraping: Runs only after match status is `FINISHED` under 4-tier quota guardrails (Tier 1: 30m, Tier 2: 2h, Tier 3: 6h, Tier 4: 24h).
- **Mandatory Backward Compatibility**:
  - As of October 2026, because users on Google Play update at different cadences, all backend, pipeline, and API changes MUST preserve backward compatibility across all 3 historical endpoint tiers:
    1. Legacy Supabase Cloud tier (`eiomktvavndorazreyba.supabase.co`): must remain populated with fresh finished highlights.
    2. Transitional Cloudflare Edge tier (`fc-edge-proxy.dirzaaulia11.workers.dev`): must remain routed to PostgreSQL 16.
    3. Production Caddy / PostgREST tier (`edge.fc.dirzaaulia.com`): must strip client JWT headers (`header_up -Authorization`) to serve `web_anon` without HTTP 500 errors.
  - Never deprecate or shut down a legacy endpoint or auth bridge until active version telemetry proves <1% of active users remain on that tier.
