# FootballClips — OpenSpec Project Context

## Overview
FootballClips is a multi-platform football live scores, highlights, tactical analytics, and vertical Shorts OTT platform backed by an autonomous Golang micro-daemon on Oracle VM ARM64 and PostgreSQL 16 + PostgREST.

## Architecture Pillars
1. **Backend (`core-go/`)**: Hexagonal Go 1.23+ micro-daemon (`fc-daemon`, `fc-ingest`, `fc-deploy`), ESPN Soccer API adapter, YouTube Data API v3 / RSS / `/embed/` adapters, Oracle AI Gateway (`jarvis-gateway`), FCM, and WebSub push receiver.
2. **Telemetry (`web/src/app/telemetry/` + `core-go`)**: Per-match waterfall trace diagnostics (`[CHANNELS]`, `[CANDIDATES]`, `[AI_VERDICTS]`, `[OUTCOME]`), coverage metrics deck, and pipeline health monitoring.
3. **Android (`composeApp/` + `androidApp/`)**: Kotlin Multiplatform (KMP) + Jetpack Compose (SDK 37, AGP 9.4.0, Kotlin 2.4.21) with Koin DI, MVI `StateFlow` + `Channel<UiEffect>`, inline `YouTubePlayerView`, RevenueCat billing, and AdMob Next-Gen Native Ads.
4. **Next.js Web (`web/`)**: Next.js 16 App Router static export (`web/out/`), Tailwind CSS v4 Obsidian/Volt UI, TV broadcast `LiveScoreTicker`, and Firebase Hosting deployment.

## Spec-Driven Development (SDD) Workflow
- **Living Specs**: `openspec/specs/<pillar>/spec.md` + frozen JSON contract schemas in `openspec/specs/contracts/`.
- **Delta Changes**: `openspec/changes/<change-id>/` (`proposal.md`, `design.md`, `tasks.md`, `specs/`).
- **Verification**: All changes must pass `cd core-go && go test -race ./...`, `cd web && npm run build`, and `./gradlew :composeApp:compileAndroidMain`.
