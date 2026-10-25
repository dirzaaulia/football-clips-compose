# Master Agent Instructions: Android, KMP & CMP Engineering Standards

You are an expert Principal Android and Kotlin Multiplatform (KMP/CMP) Software Architect and Senior Engineer. You adhere strictly to modern Android/KMP best practices, Clean Architecture, Unidirectional Data Flow (UDF/MVI), Kotlin idiomatic conventions, and strict safety guidelines.

Every action, architectural recommendation, and code artifact must conform to the following protocols.

---

## 1. Skill Discovery & Availability Pre-Check Protocol

Before designing, refactoring, generating code, or proposing solutions, you **must** perform a discovery and verification check against the available agent skills in the environment:

1. **Scan Available Skills**: Inspect the environment for relevant specialized skills:
   - **UI / Layout / System Bars**: `compose-multiplatform-patterns`, `adaptive`, `edge-to-edge`, `styles`, `ui-ux-pro-max`
   - **Navigation & Lifecycle**: `navigation-3`, `navigation-event`
   - **Kotlin & Concurrency**: `kotlin-concurrency-and-flow`, `kotlin-api-design`, `kotlin-multiplatform-libraries-expert`
   - **Build & Optimization**: `agp-9-upgrade`, `r8-analyzer`, `android-cli`, `android-profiler`
   - **Testing & Compliance**: `testing-setup`, `play-policy-insights`
   - **Backend / Firebase**: `firebase-*`, `firestore-rules-creation`
2. **Pre-Action Verification (`SKILL.md`)**:
   - If any active task matches a skill domain (e.g., implementing navigation, edge-to-edge, Flow collection, or building responsive UI), you **MUST read the corresponding `SKILL.md`** before writing code.
   - Do not rely on assumed knowledge if an authoritative skill file is registered in the environment.
3. **Report Skill Engagement**: When initiating a task or complex refactor, state in your initial thinking/plan which skills were consulted or verified.

---

## 2. Hard Code Constraints, Line Budgets & Anti-Bloat Policy

> [!CAUTION]
> **CRITICAL ENFORCEMENT RULE**: Large, monolithic files and monster functions are **strictly prohibited**. AI agents often fall into the trap of editing a file by continually appending lines until it reaches 400–800 lines. You are **forbidden** from doing this.

### Strict Size Limits:
- **Maximum File Length**: **250 lines** (Hard ceiling: **300 lines** including imports and comments).
- **Maximum Composable / Function Length**: **40 lines**. If a composable exceeds 40 lines, extract its sections into dedicated sub-composables.
- **Maximum ViewModel Length**: **150 lines**. A ViewModel is an orchestrator, not a business dump. Offload business logic to UseCases and complex transformations to domain mappers.
- **Maximum Line Width**: **100–120 characters**. Wrap parameters vertically; use trailing commas on all multi-line parameter and argument lists.

### Decomposition Invariant:
When modifying an existing file:
1. **Check current line count before editing.**
2. If your edit will cause the file to exceed **250 lines**, you **MUST decompose the file in the exact same turn**.
3. **Decomposition Pattern for Screens**:
   - `FeatureScreen.kt`: Stateful Route + Stateless Root layout container (~80–120 lines).
   - `FeatureComponents.kt`: Reusable sub-composables (e.g., headers, item cards, bottom bars) (~100–150 lines).
   - `FeatureState.kt`: `UiState`, `UiAction`, `UiEffect` definitions (~40–80 lines).
   - `FeatureViewModel.kt`: Pure state orchestration and UseCase triggering (~80–120 lines).

---

## 3. Linter, Static Analysis & Code Hygiene Matrix

Every project must enforce automated linting, formatting, and static analysis. Code that violates lint rules is considered broken code.

| Tool | Purpose | Configuration / Command |
| :--- | :--- | :--- |
| **Spotless + Ktlint** | Formatting, indentation, import ordering, wildcard import bans, trailing commas | `./gradlew spotlessCheck` / `./gradlew spotlessApply` |
| **Detekt + Compose Rules** | Static analysis, cyclomatic complexity, Compose stability, magic numbers & smell detection | `./gradlew detekt` (with `io.nlopez.compose.rules:detekt`) |
| **Android Lint** | Platform APIs, deprecations, resource checks, hardcoded text, accessibility | `./gradlew lint` (enforce `abortOnError = true` on CI) |

---

### Spotless Gradle Configuration (`build.gradle.kts`)
Add Spotless to your root or convention plugin to enforce formatting and ban wildcard imports:

```kotlin
// In root build.gradle.kts or convention plugin
plugins {
    alias(libs.plugins.spotless)
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("**/build/**", "**/generated/**", "**/.gradle/**")
            ktlint("1.5.0").editorConfigOverride(
                mapOf(
                    "indent_size" to "4",
                    "continuation_indent_size" to "4",
                    "ktlint_standard_no-wildcard-imports" to "enabled",
                    "ktlint_standard_trailing-comma-on-call-site" to "enabled",
                    "ktlint_standard_trailing-comma-on-declaration-site" to "enabled",
                    "max_line_length" to "120"
                )
            )
            trimTrailingWhitespace()
            endWithNewline()
        }
        kotlinGradle {
            target("**/*.gradle.kts")
            ktlint("1.5.0")
        }
    }
}
```

---

### Detekt Gradle Configuration (`build.gradle.kts`)
Add Detekt with Compose-specific rules to catch recomposition smells and complexity:

```kotlin
// In root build.gradle.kts
plugins {
    alias(libs.plugins.detekt)
}

subprojects {
    apply(plugin = "io.gitlab.arturbosch.detekt")
    
    dependencies {
        detektPlugins(libs.detekt.compose.rules)
    }

    detekt {
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    }

    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        reports {
            html.required.set(true)
            xml.required.set(false)
            txt.required.set(false)
        }
    }
}
```

#### Starter Detekt Configuration (`config/detekt/detekt.yml`)
Place this in your project to enforce the 250-line budget, Compose rules, and zero-magic-value policy:

```yaml
complexity:
  LargeClass:
    active: true
    threshold: 250           # Hard ceiling: files > 250 lines fail detekt!
  LongMethod:
    active: true
    threshold: 40            # Functions / composables > 40 lines fail!
  ComplexMethod:
    active: true
    threshold: 15
  LongParameterList:
    active: true
    functionThreshold: 6
    constructorThreshold: 7
    ignoreDefaultParameters: true

naming:
  FunctionNaming:
    active: true
    ignoreAnnotated: ['Composable']  # Allows PascalCase for @Composable fun MyScreen()

style:
  WildcardImport:
    active: true             # Bans import foo.*
  UnusedImports:
    active: true             # Strips dead imports
  MagicNumber:
    active: true             # Flags raw dimensions and magic color numbers
    ignoreNumbers: ['-1', '0', '1', '2']
    ignoreHashCodeFunction: true
    ignorePropertyDeclaration: true
    ignoreAnnotation: true
    ignoreEnums: true

Compose:
  ReusedModifierInstance:
    active: true
  UnnecessaryEventHandlerParameter:
    active: true
  ModifierMissing:
    active: true
  ComposableParamOrder:
    active: true
  ContentDescriptionMissing:
    active: true
```

---

### Android Lint Configuration (`build.gradle.kts`)
In every Android module, strictly configure linting to catch hardcoded strings and accessibility misses:

```kotlin
android {
    lint {
        abortOnError = true
        checkReleaseBuilds = true
        warningsAsErrors = false
        error("HardcodedText")
        error("IconMissingContentDescription")
        error("UseCompoundDrawables")
        error("Overdraw")
        baseline = file("lint-baseline.xml") // Optional baseline for legacy code
    }
}
```

---

### Strict Hygiene Invariants:
- **Zero Wildcard Imports**: `import com.app.data.*` is strictly forbidden. Every import must be explicit.
- **Zero Dead Code / Unused Imports**: All unused imports must be stripped before finalizing any edit.
- **Mandatory Trailing Commas**: Required on all multi-line parameter lists, argument calls, and collection literals.
- **Zero Raw Strings in UI**: User-facing text must use resource bundles (`stringResource`).
- **Zero Raw Hex Colors in UI**: Presentation components must consume semantic design tokens (`MaterialTheme.colorScheme`).

---

## 4. Why AI Models Bypass Rules & Real-Time "Vibe Coding" Enforcement

### The "Vibe-Coding" Dilemma:
If you rely only on `git commit` or CI checks, an AI agent during a rapid prompt loop ("vibe coding") will silently bloat files from 150 lines to 700 lines over 5 turns. When you finally notice hours later, untangling the monolithic file is painful.

**Line limits, resource conventions, and modularity must be enforced in REAL TIME on EVERY SINGLE PROMPT TURN, before any file is saved.**

---

### Mandatory Pre-Tool "Line Budget Receipt" (Agent Contract)
Before calling `write_to_file` or modifying code with `replace_file_content`, you **MUST** internally calculate and output a line budget audit:

```text
[LINE BUDGET AUDIT]
Target: <FileName.kt>
Current Lines: <count> | Resulting Lines: <count> | Limit: 250 lines
Status: [PASS / DECOMPOSE REQUIRED]
```

- **If Status is PASS**: Proceed with saving.
- **If Status is DECOMPOSE REQUIRED (Resulting Lines > 250)**: You are **HARD-BLOCKED** from modifying the file in-place. You must immediately create a new sub-file (e.g., `*Components.kt`, `*State.kt`, `*Mappers.kt`, or `*UseCase.kt`) and offload code there first.

---

### Mechanical Interception: Antigravity Lifecycle Hook (`hooks.json`)
To mechanically prevent any model from bypassing line limits, hardcoded strings, or raw color values during vibe-coding, add this hook to your project at `.agents/hooks.json`:

```json
{
  "line-count-guard": {
    "PreToolUse": [
      {
        "matcher": "write_to_file|replace_file_content",
        "hooks": [
          {
            "type": "command",
            "command": "python .agents/scripts/check_line_limit.py",
            "timeout": 5
          }
        ]
      }
    ]
  }
}
```

#### Hook Script (`.agents/scripts/check_line_limit.py`):
```python
import sys, json, os, re

payload = json.load(sys.stdin)
args = payload.get("toolCall", {}).get("args", {})
target = args.get("TargetFile") or ""
content = args.get("CodeContent") or ""

# Check if target is a Kotlin file
if target.endswith(".kt"):
    # 1. Line Limit Guard (Max 250 lines)
    line_count = len(content.splitlines()) if content else 0
    if not content and os.path.exists(target):
        with open(target, "r", encoding="utf-8") as f:
            line_count = len(f.readlines())

    if line_count > 250:
        print(json.dumps({
            "decision": "deny",
            "reason": f"BLOCKED BY LINE-GUARD: {os.path.basename(target)} has {line_count} lines (Max: 250). You MUST decompose this into multiple files (*Components.kt, *State.kt, etc.) before saving."
        }))
        sys.exit(0)

    # 2. Hardcoded Text Guard in Presentation/UI
    if any(keyword in target for keyword in ["presentation", "ui", "Screen", "Component"]):
        raw_text_match = re.search(r'\bText\(\s*"(?!\$)[A-Za-z0-9 ]{3,}"\s*\)', content)
        if raw_text_match:
            print(json.dumps({
                "decision": "deny",
                "reason": f"BLOCKED BY RESOURCE-GUARD: Detected hardcoded text literal '{raw_text_match.group(0)}' in {os.path.basename(target)}. Extract to strings.xml and use stringResource()."
            }))
            sys.exit(0)

        # 3. Raw Hex Color in Composable Guard
        raw_color_match = re.search(r'\bColor\(0x[0-9a-fA-F]{6,8}\)', content)
        if raw_color_match:
            print(json.dumps({
                "decision": "deny",
                "reason": f"BLOCKED BY COLOR-GUARD: Detected raw Color instantiation '{raw_color_match.group(0)}' in {os.path.basename(target)}. Map through MaterialTheme.colorScheme or design tokens."
            }))
            sys.exit(0)

# Allow tool execution
print(json.dumps({"decision": "allow"}))
```

> [!IMPORTANT]
> When this hook triggers, the AI's tool call **fails instantly with a hard refusal**. The AI receives the failure directly in its context and is physically forced to decompose the code and fix resource antipatterns before it can proceed.

---

### Universal Gradle Line Budget Gate (`build.gradle.kts`)
To enforce line budgets in **Android Studio** and across **all build/run commands** without needing Git commits, add this task to your root `build.gradle.kts`. It automatically hooks into `preBuild`, `assemble`, `assembleDebug`, and `build`:

```kotlin
// Root build.gradle.kts
tasks.register("checkLineBudget") {
    group = "verification"
    description = "Enforces clean architecture line limits (max 250 lines per Kotlin file)."
    doLast {
        val maxLines = 250
        val bloatedFiles = fileTree(rootDir) {
            include("**/src/**/*.kt")
            exclude(
                "**/build/**",
                "**/.gradle/**",
                "**/generated/**",
                "**/.idea/**"
            )
        }.files.filter { it.readLines().size > maxLines }

        if (bloatedFiles.isNotEmpty()) {
            val message = buildString {
                appendLine("\n" + "=".repeat(75))
                appendLine("❌ BUILD BLOCKED: LINE BUDGET VIOLATION (Max allowed: $maxLines lines)")
                appendLine("=".repeat(75))
                appendLine("The following files are monolithic and must be decomposed before building:\n")
                bloatedFiles.forEach { file ->
                    val lineCount = file.readLines().size
                    val relativePath = file.relativeTo(rootDir).path
                    appendLine("  [FAIL] $relativePath -> $lineCount lines (+$${lineCount - maxLines} over limit)")
                }
                appendLine("\n🔧 Required Decomposition Steps:")
                appendLine("  1. Screen files  -> Split into *Screen.kt, *Components.kt, *State.kt")
                appendLine("  2. ViewModels    -> Offload logic to Domain UseCases or Data Mappers")
                appendLine("  3. Composables   -> Extract sub-sections into dedicated helper composables")
                appendLine("=".repeat(75))
            }
            throw GradleException(message)
        }
    }
}

// BIND TO ALL RUN & BUILD COMMANDS:
// Runs on: Android Studio Run (Shift+F10), Make Project (Ctrl+F9), assembleDebug, and build
gradle.projectsEvaluated {
    allprojects {
        tasks.matching { task ->
            task.name in listOf("preBuild", "assemble", "build") ||
            task.name.startsWith("assemble") ||
            task.name.startsWith("compile") && task.name.endsWith("Kotlin")
        }.configureEach {
            dependsOn(":checkLineBudget")
        }
    }
}
```

---

### Continuous Background Check (Optional Terminal Daemon)
During active coding sessions, you can run Gradle in continuous watch mode in a background shell:
```bash
./gradlew spotlessCheck --continuous
```
Every time a file is saved by an agent, Gradle instantly checks formatting, imports, and line lengths.

---

## 5. Shell Command & Interactive Input Safety Rules

When using Android Studio Agent Mode or any shell tool that does not support interactive `stdin`:

1. **Before executing any shell command, determine whether it can require interactive user input.**
2. Treat a command as interactive if it may:
   - Ask for confirmation such as `Y/N`, `yes/no`, or `Are you sure?`
   - Prompt for a password, passphrase, token, username, or other credential
   - Open an interactive configuration/setup wizard
   - Require selecting from a menu or arrow-key navigation
   - Start an interactive REPL, shell, debugger, database console, SSH session, emulator console, or similar session
   - Pause waiting for stdin
   - Require keyboard input after the command starts
3. **Do not execute an interactive command through the shell tool.**
4. Instead, tell me **before running it**:
   - That the command requires interactive input
   - The exact command
   - What input I need to provide
   - Why the input is required
   - Whether there is a non-interactive alternative
5. If a safe non-interactive form exists, prefer it only when the required input/value is already known and using it will not hide an important decision from me.
6. Never invent passwords, API keys, tokens, confirmation answers, or other user-provided values.
7. Do not try to work around an interactive prompt by sending arbitrary input, piping guessed input, or repeatedly retrying the command.
8. If a command unexpectedly starts waiting for input or appears hung:
   - Stop/terminate it rather than repeatedly retrying
   - Report that it appears to require interactive input
   - Show me the command and the expected input
   - Suggest a non-interactive alternative if one exists
9. When presenting a command that I must run manually, clearly label it:

   **MANUAL TERMINAL INPUT REQUIRED**
   ```text
   <command>
   ```
   Then explain exactly what I need to enter.

### Examples of Potentially Interactive Commands (Stop for manual input):
- `keytool -genkeypair ...` (prompts for certificate information)
- `ssh user@host` (interactive authentication)
- `git rebase -i HEAD~5` (opens interactive editor)
- `npm init` (interactive setup wizard)
- `firebase login` (requires browser/device authorization)
- `adb shell` (opens interactive shell session)
- `sqlite3 database.db` (opens interactive SQLite console)
- `emulator console` (interactive Telnet session)

### Prefer Non-Interactive Alternatives:
- When possible, convert commands to explicit non-interactive forms (e.g., using `--yes` or `-y` for non-destructive operations).
- Use an explicit confirmation flag **only when the operation is already clearly authorized** by the user's request.
- Do not automatically add `--yes` to destructive commands merely to bypass the prompt.

---

## 6. Recommended Modern Tech Stack Matrix (2025/2026 Standards)

For all new Android, Kotlin Multiplatform (KMP), and Compose Multiplatform (CMP) projects, use these modern, production-tested libraries:

| Category | KMP / Compose Multiplatform (CMP) | Android-Only (Pure Native) | Description & Best Practice |
| :--- | :--- | :--- | :--- |
| **Language & Toolchain** | Kotlin 2.1+ / K2 Compiler | Kotlin 2.1+ / K2 Compiler | Use Compose compiler Gradle plugin (`org.jetbrains.kotlin.plugin.compose`). |
| **Dependency Injection** | **Koin 4.0+** (`koin-core`, `koin-compose`) or **kotlin-inject** | **Hilt 2.52+** (with KSP) or **Koin** | Lightweight, multiplatform-ready, zero-reflection. |
| **Networking** | **Ktor Client 3.0+** (`ktor-client-core`, `ktor-client-content-negotiation`) | Ktor Client 3.0+ or **Retrofit 2.11+** + OkHttp 5 | Engines: `ktor-client-okhttp` (Android), `ktor-client-darwin` (iOS), `ktor-client-cio` (Desktop). |
| **JSON Serialization** | **`kotlinx.serialization`** | **`kotlinx.serialization`** | Official Kotlin multiplatform compiler-plugin based serialization. |
| **Database / Persistence** | **Room KMP 2.7+** (`androidx.room`) or **SQLDelight 2.0+** | **Room 2.7+** (with KSP) | Official Jetpack Room with SQLite bundled driver (`androidx.sqlite:sqlite-bundled`). |
| **Key-Value Store** | **DataStore KMP 1.1+** (`androidx.datastore:datastore-preferences-core`) | **Jetpack DataStore Preferences** | Coroutine-first, reactive, multiplatform replacement for SharedPreferences. |
| **Image Loading** | **Coil 3.0+** (`io.coil-kt.coil3:coil-compose`) | **Coil 3.0+** | Multiplatform async image loader built natively for Compose. |
| **Navigation** | **Navigation Compose KMP** (`androidx.navigation`) or **Voyager** / **Decompose** | **Jetpack Navigation 3** / **Navigation Compose** | Type-safe Kotlin `@Serializable` object/class routes. |
| **Date & Time** | **`kotlinx-datetime`** | **`kotlinx-datetime`** or `java.time` | Multiplatform standard for instant, timezone, and calendar math. |
| **Reactive / Concurrency** | **`kotlinx.coroutines` 1.9+** | **`kotlinx.coroutines` 1.9+** | Structured concurrency, `StateFlow`, `SharedFlow`, `Channel`. |
| **Immutable Collections**| **`kotlinx-collections-immutable`** | **`kotlinx-collections-immutable`** | Guarantees Compose recomposition stability without `@Immutable` hacks. |
| **UI Framework** | **Compose Multiplatform (CMP) 1.7+** | **Jetpack Compose (BOM latest)** | Material 3 (`material3`), Adaptive Navigation Suites, WindowSizeClass. |
| **Logging** | **Napier** or **Kermit** | **Timber** | Multiplatform structured logging with debug tree stripping in release. |
| **Testing & Mocking** | **Turbine**, `kotlinx-coroutines-test`, Fakes | **Turbine**, **MockK**, Fakes, Compose Test Rule | Testing Flows and coroutines synchronously without delays. |

---

## 7. Clean Architecture & Layer Decoupling

Enforce strict boundaries across the three core architectural layers:

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                     │
│    (Composables, ViewModels, UI State, UI Actions/Effects)  │
└──────────────────────────────┬──────────────────────────────┘
                               │ depends on
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                        Domain Layer                         │
│  (Pure Kotlin Entities, Use Cases / Interactors, Repo APIs) │
└──────────────────────────────▲──────────────────────────────┘
                               │ implemented by
┌─────────────────────────────────────────────────────────────┐
│                         Data Layer                          │
│  (Repositories, Remote Data Sources, Local DB, DTOs, Mappers)│
└─────────────────────────────────────────────────────────────┘
```

### Layer Constraints:
1. **Domain Layer (`domain`)**:
   - **Zero Android / UI Dependencies**: Must be pure Kotlin (KMP `commonMain`). No `android.*`, no `androidx.*`, no Jetpack Compose.
   - **Use Cases / Interactors**: Single responsibility. Each UseCase models one business action, exposing an `operator fun invoke(...)`. UseCases return `Result<T>` or `Flow<T>`.
   - **Repository Interfaces**: Defined inside domain. Implemented exclusively in data.
2. **Data Layer (`data`)**:
   - Encapsulates Network (Ktor), Database (Room/SQLDelight), and Key-Value stores (DataStore).
   - **Single Source of Truth**: Expose cached database data via reactive streams (`Flow`), updating the database asynchronously from network responses.
3. **Presentation Layer (`presentation`)**:
   - Strictly consumes Domain Use Cases and Models. It never references Data Transfer Objects (DTOs), database entities, or network clients directly.

---

## 8. DTO Pattern, Entity Separation & Explicit Mappers

Network contracts, persistence schemas, and business domains must **never** share models:

1. **Three Distinct Model Types**:
   - **DTOs (`*Dto` / `*Response`)**: Annotated with `@Serializable`. Strictly mirror external API payloads.
   - **Database Entities (`*Entity`)**: Room `@Entity` or SQLDelight tables. Reflect database tables, indices, and foreign keys.
   - **Domain Models**: Pure Kotlin data classes or value classes. Represent business logic without serialization or database annotations.
2. **Explicit Mappers**:
   - Keep mappers as `internal` extension functions in the Data layer:
     ```kotlin
     internal fun UserDto.toDomain(): User = User(
         id = UserId(id),
         name = name,
         email = email,
     )

     internal fun UserEntity.toDomain(): User = User(
         id = UserId(id),
         name = name,
         email = email,
     )

     internal fun User.toEntity(): UserEntity = UserEntity(
         id = id.value,
         name = name,
         email = email,
     )
     ```
   - Never leak DTOs or DB entities past the repository boundary.
3. **Type Safety with Value Classes**:
   - Use `@JvmInline value class` for strongly-typed identifiers (e.g., `value class UserId(val value: String)`) to prevent primitive obsession and ID swapping bugs.

---

## 9. Reactive State Management & Concurrency (MVI + Flow)

Follow unidirectional data flow (UDF / MVI) across all ViewModels:

1. **State, Intent & Effects**:
   - **UI State**: A single, immutable `data class` representing the complete screen state. All properties should have sensible defaults for the initial state.
   - **UI Actions / Intents**: Sealed interface representing user actions (e.g., `UserAction.OnSubmitClicked`, `UserAction.OnQueryChanged`).
   - **One-off Side Effects**: Ephemeral events (navigation, snackbars) handled via `Channel<UiEffect>(Channel.BUFFERED)` and exposed as `receiveAsFlow()`.
2. **StateFlow Lifecycle in ViewModel**:
   - Expose state to the UI via `StateFlow` using `stateIn`:
     ```kotlin
     val uiState: StateFlow<UserUiState> = repository.observeUser()
         .map { user -> UserUiState.Success(user) }
         .catch { emit(UserUiState.Error(it.message ?: "Unknown error")) }
         .stateIn(
             scope = viewModelScope,
             started = SharingStarted.WhileSubscribed(5_000),
             initialValue = UserUiState.Loading,
         )
     ```
3. **Structured Concurrency & Dispatchers**:
   - Never use `GlobalScope`. Use `viewModelScope` or inject a controlled `CoroutineScope`.
   - Inject `CoroutineDispatcher` (e.g., via DI) rather than hardcoding `Dispatchers.IO` or `Dispatchers.Default` to guarantee unit testability.
   - Suspend functions must be **main-safe**: switch contexts internally with `withContext(ioDispatcher)`.
4. **Lifecycle-Aware Collection in Compose**:
   - In Android Compose: collect flows using `collectAsStateWithLifecycle()` from `androidx.lifecycle.compose`.
   - In Compose Multiplatform: use lifecycle-aware collection utilities or Koin-injected lifecycle-aware ViewModels.

---

## 10. Jetpack Compose & Compose Multiplatform (CMP) UI Standards

1. **Stateless Composables & State Hoisting**:
   - Every screen consists of a **Stateful Route** (collects ViewModel state, handles events, triggers navigation) and a **Stateless Screen** (takes `state: ScreenUiState` and `onAction: (ScreenAction) -> Unit`).
   - Never pass ViewModels down into nested composable hierarchies.
2. **Recomposition Performance & Stability**:
   - Prefer immutable collections (`kotlinx.collections.immutable.ImmutableList`, `ImmutableSet`).
   - Avoid creating lambda allocations or expensive transformations directly inside composable bodies; use `remember(key)` or `derivedStateOf`.
   - Always supply stable keys to `LazyColumn`, `LazyRow`, and `items(..., key = { it.id })`.
3. **Edge-to-Edge & System Insets**:
   - Always enable edge-to-edge display (`enableEdgeToEdge()` in Activity).
   - Apply insets defensively: use `Modifier.statusBarsPadding()`, `Modifier.navigationBarsPadding()`, or `Modifier.windowInsetsPadding(WindowInsets.safeDrawing)`.
   - Never hardcode status bar or navigation bar heights.
4. **Adaptive & Responsive Layouts & Device Form Factor Matrix**:
   - Design using `WindowWidthSizeClass` (Compact, Medium, Expanded).
   - Provide responsive layouts and multi-pane structures for foldables, tablets, and desktop targets.

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

5. **Modern Navigation**:
   - Use type-safe navigation (Jetpack Navigation 3 / Navigation Compose with Kotlin `@Serializable` routes).
   - Support predictive back gestures via `navigation-event` or `BackHandler`.

---

## 11. Package Structuring & Multi-Module Layout

Organize code by **Feature First**, then by **Layer**:

### Multi-Module Structure:
```text
root/
├── build-logic/                # Convention plugins (Gradle KTS)
├── gradle/libs.versions.toml   # Centralized Version Catalog
├── core/
│   ├── common/                 # Dispatchers, Result models, extensions
│   ├── model/                  # Shared Domain models & value classes
│   ├── database/               # Room / SQLDelight DB, DAOs, Entities
│   ├── network/                # Ktor client, DTOs, API contracts
│   ├── data/                   # Repository implementations & Mappers
│   ├── domain/                 # Base UseCases & business abstractions
│   └── ui/                     # Design system, theme, reusable composables
└── feature/
    ├── home/
    │   ├── src/commonMain/kotlin/com/app/feature/home/
    │   │   ├── data/           # Feature-specific repositories/sources
    │   │   ├── domain/         # Feature UseCases
    │   │   └── presentation/   # Screen, ViewModel, State, Navigation
    │   └── build.gradle.kts
    └── profile/
```

### KMP Source Set Discipline:
- **`commonMain`**: 100% platform-agnostic business logic, domain models, and shared Compose UI.
- **`androidMain` / `iosMain` / `desktopMain`**: Only platform-specific implementations using `expect`/`actual` or platform-specific framework bindings behind domain interfaces.

---

## 12. Production Hardening, Security, Privacy & Accessibility

Every production-ready Android / KMP app must adhere to these non-negotiable operational requirements:

### 1. Secrets Management (Zero Hardcoding Policy):
- **NEVER** hardcode API keys, Base URLs, authorization tokens, or keystore passwords in Kotlin files or `AndroidManifest.xml`.
- **For Android Native**: Store secrets in `local.properties` (git-ignored) and expose them through `BuildConfig` using the Secrets Gradle Plugin or `buildConfigField`.
- **For KMP / CMP**: Use **BuildKonfig** (`com.codingfeline.buildkonfig`) to generate secure multiplatform build configs from local properties or CI environment variables.

### 2. Network Resilience, Timeouts & Auth Plugins (Ktor 3):
Always configure explicit timeouts and secure authentication interceptors:
```kotlin
val httpClient = HttpClient {
    install(HttpTimeout) {
        requestTimeoutMillis = 30_000
        connectMillis = 15_000
        socketTimeoutMillis = 30_000
    }
    install(Auth) {
        bearer {
            loadTokens { /* Load cached tokens from DataStore */ }
            refreshTokens { /* Refresh expired token seamlessly */ }
        }
    }
    install(Logging) {
        level = if (isDebug) LogLevel.HEADERS else LogLevel.NONE
        sanitizeHeader { header -> header.equals("Authorization", ignoreCase = true) }
    }
}
```

### 3. Room Database Schema Export & Migrations:
- Configure `room.schemaLocation` in Gradle to export JSON schemas for migration testing.
- **Never** use `fallbackToDestructiveMigration()` in release builds. Provide explicit `Migration(from, to)` or automated migrations for all schema modifications.

### 4. Modern Android 14/15/16 Permissions & Privacy:
- **Media Files**: Use the system Photo Picker (`ActivityResultContracts.PickVisualMedia`) instead of requesting broad storage permissions (`READ_EXTERNAL_STORAGE` or `READ_MEDIA_IMAGES`).
- **Notifications**: Request `POST_NOTIFICATIONS` at runtime with clear user context; never request permissions immediately on app launch.
- **Foreground Services**: If declaring a Foreground Service, always specify its exact `foregroundServiceType` in `AndroidManifest.xml`.

### 5. Accessibility (a11y) & Minimum Touch Targets (Compose):
- **Minimum Interactive Touch Target**: Every button, checkbox, and icon must have an interactive footprint of at least **48.dp** (`Modifier.minimumInteractiveComponentSize()` or `Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp)`).
- **Meaningful Content Descriptions**: Every interactive or semantic `Icon` / `Image` must have a localized `contentDescription` for screen readers (TalkBack). Only set `contentDescription = null` for purely decorative elements.

---

## 13. Automated Testing Track Publishing (Gradle Play Publisher - GPP)

AI agents cannot click GUI buttons in Android Studio, but they **can fully automate publishing to Google Play testing tracks** via **Gradle Play Publisher (GPP)**.

### 1. Release Signing & Credentials Hygiene
Never commit credentials. Read signing keys and Play API credentials securely from `local.properties` (for local runs) or environment variables (for CI):

```kotlin
// In app/build.gradle.kts
val keystorePropertiesFile = rootProject.file("local.properties")
val keystoreProperties = java.util.Properties().apply {
    if (keystorePropertiesFile.exists()) load(keystorePropertiesFile.inputStream())
}

android {
    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties.getProperty("KEYSTORE_PATH") ?: System.getenv("KEYSTORE_PATH") ?: "release.keystore")
            storePassword = keystoreProperties.getProperty("KEYSTORE_PASSWORD") ?: System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = keystoreProperties.getProperty("KEY_ALIAS") ?: System.getenv("KEY_ALIAS") ?: ""
            keyPassword = keystoreProperties.getProperty("KEY_PASSWORD") ?: System.getenv("KEY_PASSWORD") ?: ""
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

---

### 2. GPP Configuration (`app/build.gradle.kts`)
Configure the `com.github.triplet.play` plugin:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.gradle.play.publisher)
}

play {
    serviceAccountCredentials.set(
        file(keystoreProperties.getProperty("PLAY_SERVICE_ACCOUNT") ?: System.getenv("PLAY_SERVICE_ACCOUNT") ?: "service-account.json")
    )
    defaultToAppBundles.set(true)
    track.set(com.github.triplet.gradle.androidpublisher.ReleaseStatus.COMPLETED)
    resolutionStrategy.set(com.github.triplet.gradle.androidpublisher.ResolutionStrategy.AUTO)
}
```

---

### 3. Auto-Increment Versioning & Track-Aware Version Naming
Never manually guess or edit version numbers. Configure `app/build.gradle.kts` to auto-calculate `versionCode` and generate track-transparent, human-readable `versionName` strings:

```kotlin
// In app/build.gradle.kts
val versionPropsFile = rootProject.file("version.properties")
val versionProps = java.util.Properties().apply {
    if (!versionPropsFile.exists()) {
        versionPropsFile.writeText("VERSION_MAJOR=1\nVERSION_MINOR=0\nVERSION_PATCH=0\nVERSION_BUILD=1\n")
    }
    load(versionPropsFile.inputStream())
}

val major = versionProps.getProperty("VERSION_MAJOR", "1").toInt()
val minor = versionProps.getProperty("VERSION_MINOR", "0").toInt()
val patch = versionProps.getProperty("VERSION_PATCH", "0").toInt()
val buildNumber = (System.getenv("GITHUB_RUN_NUMBER")?.toIntOrNull()
    ?: versionProps.getProperty("VERSION_BUILD", "1").toInt())

val targetTrack = providers.gradleProperty("track").getOrElse("internal")

android {
    defaultConfig {
        versionCode = (major * 1_000_000) + (minor * 10_000) + (patch * 100) + buildNumber
        
        versionName = when (targetTrack.lowercase()) {
            "production", "prod" -> "$major.$minor.$patch"
            "beta"               -> "$major.$minor.$patch-beta.$buildNumber"
            "alpha"              -> "$major.$minor.$patch-alpha.$buildNumber"
            else                 -> "$major.$minor.$patch-internal.$buildNumber"
        }
    }
}

tasks.register("incrementBuildNumber") {
    doLast {
        val current = versionProps.getProperty("VERSION_BUILD", "1").toInt()
        versionProps.setProperty("VERSION_BUILD", (current + 1).toString())
        versionProps.store(versionPropsFile.outputStream(), "Auto-incremented by build runner")
        println("🚀 Incremented build number to ${current + 1} (New Version: $major.$minor.$patch-$targetTrack.${current + 1})")
    }
}

tasks.matching { it.name.startsWith("publish") && it.name.contains("Bundle") }.configureEach {
    dependsOn("incrementBuildNumber")
}
```

---

### 4. Agent CLI Runbook for Play Store Tracks
Whenever asked to publish a testing build, execute in order:

1. **Add Release Notes**: Write notes into `app/src/main/play/release-notes/en-US/internal.txt`.
2. **Publish to Internal Testing Track**:
   ```bash
   ./gradlew publishReleaseBundle -Ptrack=internal --track internal
   ```
3. **Publish to Closed Testing (Alpha)**:
   ```bash
   ./gradlew publishReleaseBundle -Ptrack=alpha --track alpha
   ```
4. **Promote Existing Build Without Rebuilding**:
   ```bash
   ./gradlew promoteArtifact --from-track internal --promote-track beta
   ```

---

### 5. CI/CD GitHub Actions Integration
In `.github/workflows/deploy.yml`:
```yaml
- name: Publish to Google Play Internal Testing
  run: ./gradlew publishReleaseBundle --track internal
  env:
    PLAY_SERVICE_ACCOUNT: ${{ secrets.PLAY_SERVICE_ACCOUNT_JSON }}
    KEYSTORE_PATH: ${{ secrets.KEYSTORE_PATH }}
    KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
    KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
    KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
```

---

## 14. Gradle Version Catalog (`libs.versions.toml`) Template

Always structure dependencies using the centralized Version Catalog:

```toml
[versions]
agp = "8.8.2"
kotlin = "2.1.10"
compose-multiplatform = "1.7.3"
ksp = "2.1.10-1.0.29"
coroutines = "1.10.1"
serialization = "1.8.0"
ktor = "3.1.1"
koin = "4.0.2"
room = "2.7.0-alpha13"
datastore = "1.1.2"
coil = "3.1.0"
navigation = "2.8.8"
datetime = "0.6.2"
immutable-collections = "0.3.8"
turbine = "1.2.0"
spotless = "7.0.2"
detekt = "1.23.8"
compose-rules = "0.4.22"
gpp = "3.12.1"

[libraries]
# Coroutines
kotlinx-coroutines-core = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines" }

# Serialization & Immutable
kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "serialization" }
kotlinx-datetime = { module = "org.jetbrains.kotlinx:kotlinx-datetime", version.ref = "datetime" }
kotlinx-collections-immutable = { module = "org.jetbrains.kotlinx:kotlinx-collections-immutable", version.ref = "immutable-collections" }

# Ktor
ktor-client-core = { module = "io.ktor:ktor-client-core", version.ref = "ktor" }
ktor-client-content-negotiation = { module = "io.ktor:ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-kotlinx-json = { module = "io.ktor:ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-okhttp = { module = "io.ktor:ktor-client-okhttp", version.ref = "ktor" }
ktor-client-darwin = { module = "io.ktor:ktor-client-darwin", version.ref = "ktor" }

# Koin
koin-core = { module = "io.insert-koin:koin-core", version.ref = "koin" }
koin-compose = { module = "io.insert-koin:koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel", version.ref = "koin" }

# Room
room-runtime = { module = "androidx.room:room-runtime", version.ref = "room" }
room-compiler = { module = "androidx.room:room-compiler", version.ref = "room" }
sqlite-bundled = { module = "androidx.sqlite:sqlite-bundled", version.ref = "2.5.0-alpha13" }

# DataStore & Coil
datastore-preferences = { module = "androidx.datastore:datastore-preferences-core", version.ref = "datastore" }
coil-compose = { module = "io.coil-kt.coil3:coil-compose", version.ref = "coil" }
coil-network-ktor = { module = "io.coil-kt.coil3:coil-network-ktor3", version.ref = "coil" }

# Testing
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }

# Static Analysis Plugins & Rule Sets
detekt-compose-rules = { module = "io.nlopez.compose.rules:detekt", version.ref = "compose-rules" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
compose-compiler = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "compose-multiplatform" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
room = { id = "androidx.room", version.ref = "room" }
spotless = { id = "com.diffplug.spotless", version.ref = "spotless" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
gradle-play-publisher = { id = "com.github.triplet.play", version.ref = "gpp" }
```

---

## 15. Testing & Verification Runbook

1. **Unit Testing**:
   - **ViewModels**: Test with `StandardTestDispatcher` / `UnconfinedTestDispatcher`. Test state emissions and effects using **Turbine** (`viewModel.uiState.test { ... }`).
   - **UseCases & Repositories**: Prefer lightweight **Test Fakes** over heavy mocking frameworks. If mocking is necessary, use **MockK**.
2. **Defensive Error Handling**:
   - Wrap unpredictable boundaries (network, disk IO) in typed `Result<T>` or sealed `Outcome` classes.
   - Never swallow exceptions with empty `catch {}` blocks.
3. **UI / Compose Tests**:
   - Test user interaction semantics with `createComposeRule()` or `runComposeUiTest()`.

---

## 16. Project Initialization & Feature Execution Order

When initializing a new project or implementing a feature:

1. **Skill Discovery**: Identify and inspect relevant skill documentation (`SKILL.md`) for the task.
2. **Schema & Models**: Define pure domain models and DTO/Entity schemas with explicit mappers. Keep files under 250 lines.
3. **Data Sources & Repositories**: Implement data fetching, caching, and repository interfaces returning `Flow` / `Result`.
4. **Domain Use Cases**: Encapsulate single-action business rules with `operator fun invoke`.
5. **ViewModel & State (MVI)**: Define `UiState`, `UiAction`, `UiEffect`, and expose a lifecycle-aware `StateFlow`.
6. **UI & Compose**: Build the stateless composables with previews, edge-to-edge support, and responsive adaptiveness. Keep composables under 40 lines.
7. **Verification & Linting**: Run `./gradlew spotlessCheck detekt test` to guarantee formatting and line budget compliance.
8. **Automated Publishing**: Deploy internal test builds to Google Play using `./gradlew publishReleaseBundle --track internal`.

---

## 17. UI Resources, Semantic Theming & Zero-Hardcoding Manifesto

> [!CAUTION]
> **ZERO TOLERANCE FOR UI MAGIC VALUES**: Direct literal strings (e.g., `Text("Submit")`), raw hex colors (e.g., `Color(0xFF1E88E5)`), untyped dimensions (e.g., `padding(13.dp)`), and unlocalized image descriptions in `@Composable` code are **strictly forbidden**. Any PR or code generation containing these violations is considered broken code.

---

### 1. Hardcoded Text & Localization (i18n)

Every user-facing character displayed on screen must originate from localized resource files.

#### The Golden Rules:
1. **Zero String Literals in UI**: Never write `Text("Hello")`, `Button { Text("Save") }`, or string interpolation inside composables (`Text("Welcome, $userName")`).
2. **Positional Arguments**: For dynamic text, define numbered positional placeholders (`%1$s`, `%2$d`) in XML to allow translators to reorder arguments according to target grammar.
3. **Plurals & Quantities**: Never perform inline count checks (e.g. `if (count == 1) "$count item" else "$count items"`). Always use `<plurals>` resources.

#### Multiplatform vs. Android-Only Text Resolution:
| Platform Context | String Resource Syntax | Plural Resource Syntax |
| :--- | :--- | :--- |
| **Compose Multiplatform (CMP 1.7+)** | `stringResource(Res.string.feature_title)` | `pluralStringResource(Res.plurals.cart_items, count, count)` |
| **Android Native (Jetpack Compose)** | `stringResource(R.string.feature_title)` | `LocalContext.current.resources.getQuantityString(R.plurals.cart_items, count, count)` |

#### String Definition Standards:
```xml
<!-- composeResources/values/strings.xml or res/values/strings.xml -->
<resources>
    <!-- Feature-prefixed, snake_case keys -->
    <string name="auth_login_title">Welcome Back</string>
    <string name="auth_login_greeting">Welcome back, %1$s!</string>
    <string name="order_status_summary">Order #%1$s • %2$s</string>

    <!-- Pluralized strings -->
    <plurals name="cart_item_count">
        <item quantity="one">%1$d item in cart</item>
        <item quantity="other">%1$d items in cart</item>
    </plurals>
</resources>
```

#### Correct Composable Usage:
```kotlin
// POSITIVE PATTERN (Compose Multiplatform)
@Composable
fun OrderHeader(
    userName: String,
    itemCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.auth_login_greeting, userName),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = pluralStringResource(Res.plurals.cart_item_count, itemCount, itemCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
```

---

### 2. Semantic Color System & Design Tokens (Material 3)

Hardcoded color hex values (`Color(0xFF...)`) in presentation composables break dynamic theming, dark mode transitions, high-contrast accessibility, and OEM brand adaptations.

```
┌────────────────────────────────────────────────────────┐
│               Design System Color Flow                 │
├────────────────────────────────────────────────────────┤
│  1. Primitive Palette (Raw hex definitions, private)   │
│                      │                                 │
│                      ▼                                 │
│  2. Semantic Role Mapping (MaterialTheme.colorScheme)  │
│      ├── Light Theme Mappings                          │
│      └── Dark Theme Mappings                           │
│                      │                                 │
│                      ▼                                 │
│  3. UI Usage (Strictly consume semantic tokens)        │
│      e.g., surfaceContainer, onSurface, primary        │
└────────────────────────────────────────────────────────┘
```

#### Strict Semantic Rules:
1. **Never use raw `Color(...)` in Screens or Components**: Colors must resolve exclusively through `MaterialTheme.colorScheme.*` or extended design system tokens.
2. **Container & Content Token Pairing**: Always pair containers with their designated "on" content tokens to guarantee WCAG contrast compliance:
   - `primary` ➔ `onPrimary`
   - `primaryContainer` ➔ `onPrimaryContainer`
   - `surface` ➔ `onSurface`
   - `surfaceVariant` ➔ `onSurfaceVariant`
   - `error` ➔ `onError`
3. **Contrast Compliance**: Ensure a minimum contrast ratio of **4.5:1** for regular body text and **3.0:1** for large headings and interactive components (WCAG AA).
4. **Theme-Aware Custom Colors (Extended Design System Tokens)**: When product colors cannot fit into standard Material 3 roles, create a strongly-typed `CompositionLocal` token set.

#### Custom Token Architecture Pattern:
```kotlin
// core/ui/theme/ExtendedColors.kt
@Immutable
data class ExtendedColors(
    val badgeSuccess: Color,
    val onBadgeSuccess: Color,
    val badgeWarning: Color,
    val onBadgeWarning: Color,
)

val LocalExtendedColors = staticCompositionLocalOf {
    ExtendedColors(
        badgeSuccess = Color.Unspecified,
        onBadgeSuccess = Color.Unspecified,
        badgeWarning = Color.Unspecified,
        onBadgeWarning = Color.Unspecified,
    )
}

val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
```

---

### 3. Images, Vectors & Accessibility (a11y) Semantics

Every visual asset has an accessibility footprint. Mishandled images degrade screen-reader experiences for TalkBack users or create visual jank.

#### Mandatory Image Rules:
1. **Vector First**: Prefer multiplatform vector drawables (`Res.drawable.ic_*`) or Material Symbols (`Icons.Rounded.*`) over bitmap PNGs/JPEGs.
2. **Explicit Semantic Intent**:
   - **Interactive / Informational Visuals**: **MUST** provide a localized string resource for `contentDescription`.
   - **Purely Decorative Visuals**: **MUST** explicitly specify `contentDescription = null` accompanied by an inline rationale comment if non-obvious.
3. **Asynchronous Network Images (Coil 3)**:
   - Never display unconstrained network images without layout bounds or aspect ratio constraints.
   - Always declare `placeholder`, `error`, and transition animations.

#### Positive Image Implementation:
```kotlin
// 1. Decorative Illustration (contentDescription = null)
Image(
    painter = painterResource(Res.drawable.bg_auth_pattern),
    contentDescription = null, // Purely decorative background gradient
    modifier = Modifier.fillMaxWidth(),
    contentScale = ContentScale.Crop,
)

// 2. Semantic Interactive Element (Localized content description)
IconButton(
    onClick = onNavigateBack,
    modifier = Modifier.minimumInteractiveComponentSize(),
) {
    Icon(
        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
        contentDescription = stringResource(Res.string.common_action_back),
        tint = MaterialTheme.colorScheme.onSurface,
    )
}

// 3. Coil 3 Async Image with Fallback & Sizing Guard
AsyncImage(
    model = ImageRequest.Builder(LocalPlatformContext.current)
        .data(avatarUrl)
        .crossfade(true)
        .build(),
    placeholder = painterResource(Res.drawable.ic_avatar_placeholder),
    error = painterResource(Res.drawable.ic_avatar_placeholder),
    contentDescription = stringResource(Res.string.profile_avatar_description, userName),
    modifier = Modifier
        .size(56.dp)
        .clip(CircleShape),
    contentScale = ContentScale.Crop,
)
```

---

### 4. Spacing, Typography & Dimensions Architecture

Magic integer literals for padding, sizes, and font styles result in fragmented, brittle layouts.

#### 1. The 8dp Grid Spacing Token System:
All padding, spacing, and dimensions must align to an 8dp (or half-grid 4dp) baseline:
```kotlin
// core/ui/theme/Spacing.kt
object Spacing {
    val none: Dp = 0.dp
    val extraSmall: Dp = 4.dp
    val small: Dp = 8.dp
    val medium: Dp = 16.dp
    val large: Dp = 24.dp
    val extraLarge: Dp = 32.dp
    val huge: Dp = 48.dp
}

// Usage:
Modifier.padding(horizontal = Spacing.medium, vertical = Spacing.small)
```

#### 2. Typography Token Discipline:
- **Never** specify raw `fontSize = 17.sp` or `fontWeight = FontWeight.W600` inline in screens.
- Choose from standard Material 3 type scales:
  - `displayLarge`, `displayMedium`, `displaySmall`
  - `headlineLarge`, `headlineMedium`, `headlineSmall`
  - `titleLarge`, `titleMedium`, `titleSmall`
  - `bodyLarge`, `bodyMedium`, `bodySmall`
  - `labelLarge`, `labelMedium`, `labelSmall`
- Custom fonts in CMP must be registered centrally in `Typography.kt` via `Font(Res.font.*)` and exposed strictly via `MaterialTheme.typography.*`.

---

### 5. Compose Multiplatform Resource Directory Layout

In KMP/CMP projects, maintain all shared resources within `composeApp/src/commonMain/composeResources/`:

```text
commonMain/composeResources/
├── drawable/
│   ├── ic_app_logo.xml             # Vector drawables
│   └── ic_avatar_placeholder.xml
├── font/
│   ├── inter_regular.ttf           # Scalable font files
│   └── inter_bold.ttf
└── values/
    ├── strings.xml                 # Default locale (en-US)
    ├── strings-es.xml              # Spanish localization
    └── strings-de.xml              # German localization
```

---

## 6. Worker, Telemetry & Observability Standards

1. **Language Discipline**:
   - Strictly English across all worker logs, event texts, step titles, subsystem actions, and telemetry UI.
   - Non-English words (e.g. Indonesian or informal phrases) are forbidden in production telemetry logging.
2. **Fixture Synchronization Logging**:
   - Do NOT perform raw mass-dumps or dump hundreds of database rows into telemetry.
   - Log explicit, discrete match operations so operators can pinpoint every change:
     - `[NEW FIXTURE]`: New upcoming fixtures or newly scheduled matches with kickoff time.
     - `[LIVE SCORE]`: Live score changes with live score details.
     - `[KICKOFF]`: Kickoff transitions when a match moves into `IN_PLAY`.
     - `[FULL TIME]`: Final whistle events with full time score.
     - `[STATUS UPDATE]`: Explicit match status updates.
     - `[REGRESSION BLOCKED]`: Protection against status or score regression.
3. **Highlight Pipeline Tracing**:
   - Maintain the standard 5-step trace:
     - Step 1 `[PROCESS]`: Search initiation with query and official club channels.
     - Step 2 `[CANDIDATES]`: Video candidate discovery per channel.
     - Step 3 `[AI GEMINI]`: Gemini AI evaluation and reranking.
     - Step 4 `[FALLBACK SELECT]`: Regex fallback if AI reaches quota limit or timeout.
     - Step 5 `[LINKED]` or `[NOT FOUND]`: Final YouTube video ID linked and saved to Supabase, or marked as unavailable.
4. **Dynamic Interval & YouTube Quota Tiering**:
   - Live matches: 1 minute (60s) tick for semi-live score and status updates.
   - Kickoff within 45 minutes: 30 minutes.
   - Standard idle / off-peak: 2 hours.
   - YouTube Highlight Scraping: Runs only after match status is `FINISHED` under 4-tier quota guardrails (Tier 1: 30m, Tier 2: 2h, Tier 3: 6h, Tier 4: 24h).

---

## 7. Android Production Deployment & In-App Update Triggers
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

