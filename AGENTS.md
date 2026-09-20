# Android/Kotlin Development Rules

## General Development Guidelines
- **Language & Paradigms**: Kotlin is the primary language. Use idiomatic Kotlin, Coroutines, StateFlow, and modern declarative Compose patterns.
- **Android Guidance**: Follow official Google Android architecture guidelines, Material 3 design specifications, and current Jetpack APIs.
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
  - Verify compilation (`gradle_assemble_all` or `compileDebugKotlinAndroid`) after significant changes.
  - Run relevant unit/UI tests after modifying shared business logic.

## Project-Specific Guidelines (FootballClips)
- **Target SDK**: Compile & Target SDK 37 (AGP 9.4.0, Kotlin 2.4.20).
- **Gradle Configuration**: `android.newDsl=false` is required in `gradle.properties` when pairing Kotlin Multiplatform plugin with `com.android.application` in AGP 9.
- **Secret Management**: Always use the `getSecret(key)` helper in `app/build.gradle.kts` to fallback gracefully between `local.properties`, Gradle properties (`-PKEY=...`), and Environment Variables (`ENV['KEY']`).
- **RevenueCat Billing**: Ensure `BuildConfig.REVENUECAT_API_KEY` is checked for `isNotBlank()` before invoking `Purchases.configure(...)` in `Application.kt`.
- **AdMob Next-Gen Native Ads**:
  - Use `com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView`.
  - Maintain `MediaView` dimensions of at least `120dp x 120dp` to adhere to Google AdMob policy guidelines.
  - Use `FIT_CENTER` scaling to preserve media aspect ratios.
