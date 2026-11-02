# FootballClips Backend Architecture & Engineering Standards

## 1. Architectural Philosophy
FootballClips backend is an **AI-driven Data Extraction, Aggregation, and Media Verification Engine**. 
Rather than viewing it as loose scripts, it follows **Hexagonal Clean Architecture** principles paired with **Event-Driven Task Execution**.

```
┌────────────────────────────────────────────────────────────────────────┐
│                              API Adapters                              │
│       [FastAPI Webhooks]       [Cloud Tasks]       [Cloud Scheduler]   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                         Application Use Cases                          │
│   • FindMatchHighlightUseCase             • IngestLiveMatchesUseCase   │
│   • EvaluateCandidateVideosUseCase        • RenewWebsubUseCase         │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          Pure Domain Entities                          │
│   • MatchFixture (Pydantic v2)           • CandidateVideo (Pydantic)   │
│   • CandidateEvaluation (Pydantic)       • Mathematical Scorer         │
│   • Multilingual Normalizer              • 4-Tier Cooldown Guardrails  │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                          Outbound Adapters                             │
│   • YouTube Data API / RSS Adapter       • Oracle VM AI Gateway (LLM)  │
│   • ESPN Live Match Adapter              • PostgreSQL 16 via PostgREST │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Core Components & Responsibilities

### A. Domain Layer (`worker/domain/` & `worker/services/highlight/`)
* **`normalizer.py`**: Transliterates non-ASCII characters, Germanic umlauts (`ä` $\rightarrow$ `ae`), Nordic characters (`ø` $\rightarrow$ `oe`), Arabic club names, and canonical club aliases (`Manchester United` $\rightarrow$ `man utd`).
* **`filters.py`**: Eliminates non-highlights (press conferences, training sessions, full matches, fan reactions) and computes worldwide watchability percentages across 249 countries.
* **`scorer.py`**: Mathematical scoring matrix:
  * Official channel bonus: $+45\text{ to }+65$
  * Target keywords: $+35$
  * Scoreline match: $+40$
  * Upload decay penalty: $-10\text{ to }-35$
  * Instant rejection: $-100$ if neither team matches.
* **`cooldown.py`**: 4-Tier Quota Guardrail protecting unlinked matches from premature re-scraping:
  * **Tier 1 (30m after FT)**: First official upload wave.
  * **Tier 2 (2h after FT)**: Secondary broadcaster uploads.
  * **Tier 3 (6h after FT)**: Continental and international highlights.
  * **Tier 4 (24h after FT)**: Permanent fallback or terminal UNAVAILABLE marking.

### B. Use Cases (`worker/use_cases/`)
* **`FindMatchHighlightUseCase`**:
  * Receives an isolated `match_id`.
  * Checks cooldown & circuit breaker.
  * Queries official club, league, and broadcaster channels (RSS first, YouTube API second).
  * Evaluates candidates with Oracle VM AI Gateway (DeepSeek-Coder / Qwen 2.5) with Gemini Flash fallback.
  * Emits structured waterfall diagnostics and persists the verified video ID to PostgreSQL.

### C. The Visual Waterfall Diagnostics
Every highlight search produces a complete, millisecond-accurate audit tree:
1. **Match Information**: Match ID, Teams, Kickoff, Status, and Score.
2. **Channel Dispatch**: Official targets queried and API quota units consumed.
3. **Candidate Inspection**:
   * Video Title, Channel Handle, Duration, Upload Age, and Thumbnail.
   * AI Reranking Verdict:
     * `ACCEPTED`: Score and exact AI justification.
     * `REJECTED`: Score and specific reason (e.g. "press conference", "wrong match").
4. **Outcome**: Final YouTube Video ID linked, duration, and worldwide availability %.

---

## 3. Technology Stack & Why It Was Chosen

| Technology | Role | Why It Was Chosen |
| :--- | :--- | :--- |
| **Python 3.13** | Backend Runtime | World-standard ecosystem for LLMs, prompt engineering, text normalization, and web scraping. |
| **Pydantic v2** | Domain & Data Validation | Compiled in **Rust** (`pydantic-core`), providing strict compile-time-like validation at native speeds. Zero untyped dictionary bugs. |
| **FastAPI** | HTTP Microservice | High-performance asynchronous non-blocking I/O with automatic OpenAPI specs. |
| **Google Cloud Tasks** | Distributed Work Queue | Provides Android `WorkManager`-like per-match fault isolation, rate limiting, and exponential retries at **$0.00 cost** (1M operations/mo free). |
| **PostgreSQL 16 & PostgREST** | Primary Datastore & REST Layer | ACID-compliant relational storage on Oracle VM Cloud with instant REST endpoints. |
| **Compose Multiplatform** | Client UI (Android Native) | Shared declarative UI and state orchestration across Android Native (`composeApp`). |
| **Next.js 16 & React 19** | Web Portal & Telemetry Hub | High-performance App Router with Tailwind CSS, `@xyflow/react` pipeline graphs, ECharts, and real-time waterfall telemetry. |

---

## 4. Best Practices for Developers & AI Agents
1. **Never use raw `dict` lookups**: Always instantiate a Pydantic model (`CandidateVideo`, `MatchFixture`) before reading or modifying properties.
2. **Never swallow errors silently**: Always record structured error events with correlation IDs.
3. **Preserve Historical Archive Immutability**: Seasons before 2026 are immutable. Never run unconstrained updates or deletes against archive matches.
4. **Idempotency Invariant**: Any pipeline execution must be safe to run twice without duplicating YouTube API calls or corrupting state.
