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
  - Adaptive layouts adapting to `WindowWidthSizeClass` (Compact, Medium, Expanded) and the Device Matrix.
  - Minimum **48.dp** touch targets (`Modifier.minimumInteractiveComponentSize()`).
  - Meaningful `contentDescription` on interactive icons; `null` only for decorative icons.

### Device Form Factor & Responsive Layout Matrix
| Form Factor / Device Type | Window Width Class | Width (`dp`) | Aspect Ratio & Form Factor Characteristics | Layout Strategy | Highlights Grid Columns | Fixtures Grid Columns | Filter Bar, Navigation & Sheets |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Compact Phone** | `Compact` | `< 600 dp` | Standard Vertical (`0.5 <= AR < 0.75`) | Single-column vertical list | 1 column | 1 column (`StandardMatchCard`) | Bottom Floating Pill Nav + Compact 2-row filters + Modal Bottom Sheets |
| **Wide Portrait Phone** (e.g. Plus/Ultra/Max) | `Medium` | `600 dp - 839 dp` | Wide Vertical (`0.65 <= AR < 0.85`) | Multi-column Grid | 2 columns (`GridCells.Adaptive(minSize = 240.dp)`) | 2 columns (`GridCells.Adaptive(minSize = 320.dp)`) | Bottom Floating Pill Nav + Full horizontal filter bar + Modal Side Sheets |
| **Z Fold / Next-Gen Foldable (Cover Screen)** | `Compact` | `< 600 dp` | Tall / Wider Cover Screen (20:9 or 21:9) | Single-column list + Inset Padding | 1 column | 1 column | Bottom Floating Pill Nav + Compact 2-row filters + Modal Bottom Sheets |
| **Z Fold 8 / Next-Gen Foldable (Unfolded Main Canvas)** | `Medium` / `Expanded` | `>= 600 dp` | Unique Canvas (8"+ Near-Square 10:9 / 1:1 or 4:3) | Adaptive Dynamic Grid (`minSize` scaled) | 2–3 columns (`Adaptive(minSize = 220.dp)`) | 2 columns (`Adaptive(minSize = 300.dp)`) | Bottom Pill / Navigation Rail + Single-row filter bar + Modal Side Sheets |
| **Flip Phone (Outer Cover Screen)** | `Compact` | `< 600 dp` | Small Outer Widget Screen | Compact single-column | 1 column | 1 column | Compact Pill Nav |
| **Flip Phone (Unfolded Main Screen)** | `Compact` / `Medium` | `360 dp - 640 dp` | Tall Vertical (`0.45 <= AR < 0.6`) | Adaptive (1 col if <600dp; 2 cols if >=600dp) | 1–2 columns | 1–2 columns | Standard Bottom Pill Nav + Responsive filter bar + Bottom or Side Sheets |
| **Tablet / Desktop / Large Display** | `Expanded` | `>= 840 dp` | Wide Canvas / Desktop (`AR > 1.25` or Width >= 840) | Web/Desktop Grid + Navigation Rail | 3–4 columns (`Adaptive(minSize = 260.dp)`) | 2–3 columns (`Adaptive(minSize = 400.dp)`) | Side Navigation Rail + Web Filter Bar + Modal Side Sheets |
| **Landscape Phone / Flex Mode** | `Compact` / `Medium` | Any (`maxHeight < 500dp`) | Landscape (`maxHeight < 500dp`) | Dual-pane / Landscape Grid + Rail | PitchScope Split-Deck Dual Pane | PitchScope Dual Pane Control Desk | Left Navigation Rail + Modal Side Sheets |

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

---

## 5. Android Production Deployment & In-App Update Rules
Whenever the user instructs this agent or any other AI agent with deployment commands:

### Command Grammar & Priority Mapping
1. **"deploy production force update"**:
   - **Target**: Google Play Production Track (`track=production`).
   - **In-App Update Mode**: Priority **`5`** (`AppUpdateType.IMMEDIATE`).
   - **User Experience**: Google Play displays a mandatory, full-screen blocking modal preventing app usage until updated.
   - **Execution**: Run `python scripts/deploy_android.py --production --force` (or `python scripts/deploy_android.py --production --priority 5`).
2. **"deploy production show update prompt"**:
   - **Target**: Google Play Production Track (`track=production`).
   - **In-App Update Mode**: Priority **`2`** (`AppUpdateType.FLEXIBLE`).
   - **User Experience**: Google Play displays an optional update sheet with [Update] and [No thanks]. If accepted, it downloads in the background while the user continues using the app, and displays a "[Restart App]" dialog once downloaded.
   - **Execution**: Run `python scripts/deploy_android.py --production --prompt` (or `python scripts/deploy_android.py --production --priority 2`).
3. **"deploy production update level <0-5>"** (e.g. `level 0`, `level 4`, `level 5`):
   - **Target**: Google Play Production Track (`track=production`).
   - **In-App Update Mode**: Explicit numeric priority `0`, `1`, `2`, `3`, `4`, or `5`:
     - `0`: Silent / Default (standard Google Play background auto-update, no in-app prompt).
     - `1`: Low priority (no in-app prompt unless stale).
     - `2`: Standard flexible prompt (features and routine fixes).
     - `3`: Important flexible prompt (escalates to immediate after 14 days).
     - `4`: Immediate force update (major redesigns or breaking changes).
     - `5`: Immediate force update (critical zero-day security or API decommission).
   - **Execution**: Run `python scripts/deploy_android.py --production --priority <N>`.
4. **"deploy internal"**:
   - **Target**: Google Play Internal Testing Track (`track=internal`).
   - **Execution**: Run `python scripts/deploy_android.py --internal`.

### Deployment Safety Invariants
- Never deploy without verifying `./gradlew :androidApp:compileReleaseKotlin` pre-flight check.
- Never hardcode or reuse a `versionCode`. Always increment `VERSION_BUILD` in `version.properties` to ensure strict monotonicity.
