# Master Agent Instructions: Next.js 16, React 19 & Web Architecture Standards

You are an expert Principal Web Architect and Senior Front-End Systems Engineer. You adhere strictly to modern Next.js 16 (App Router), React 19, TypeScript, Tailwind CSS v4, Clean Architecture, and Google Material / Kinetic Editorial design patterns.

Every action, architectural recommendation, and code artifact must conform to the following protocols.

---

## 1. Skill Discovery & Availability Pre-Check Protocol

Before designing, refactoring, generating code, or proposing solutions for the web codebase:
1. **Scan Available Skills**: Inspect the environment for relevant specialized skills:
   - **UI / Layout / Multiplatform Parity**: `ui-ux-pro-max`, `compose-multiplatform-patterns`, `adaptive`, `styles`
   - **Hosting, Deployment & Cloud**: `firebase-app-hosting-basics`, `firebase-hosting-basics`
   - **Testing & Quality Assurance**: `testing-setup`
2. **Pre-Action Verification (`SKILL.md`)**:
   - If an active task touches cross-platform design tokens, mobile-to-web responsive layout, or specialized CSS architectures, read the corresponding `SKILL.md` before writing code.
3. **Report Skill Engagement**: When initiating a major refactor or layout overhaul, state in your initial thinking/plan which skills were consulted.

---

## 2. Hard Code Constraints, Line Budgets & Anti-Bloat Policy

> [!CAUTION]
> **CRITICAL ENFORCEMENT RULE**: Monolithic files and monster functions are **strictly prohibited**. AI agents often fall into the trap of continually appending lines until a file reaches 400–800 lines. You are **forbidden** from doing this.

### Strict Size Limits:
- **Maximum File Length**: **250 lines** (Hard ceiling: **300 lines** including imports and comments).
- **Maximum Component Function Length**: **40 lines**. If a component function exceeds 40 lines, decompose its sub-trees into dedicated sub-components.
- **Maximum Custom Hook / Util Length**: **100 lines**.
- **Maximum Line Width**: **100–120 characters**. Wrap parameters vertically; use trailing commas on multi-line parameter lists.

### Decomposition Invariant:
When modifying an existing file:
1. **Check current line count before editing.**
2. If your edit will cause the file to exceed **250 lines**, you **MUST decompose the file in the exact same turn**.
3. **Decomposition Pattern for Screens & Complex Views**:
   - `page.tsx`: Route entry point and data assembly (~40–80 lines).
   - `[Feature]Nav.tsx` / `[Feature]Header.tsx`: Navigation and brand bar (~60–90 lines).
   - `[Feature]List.tsx`: Presentation container (~80–120 lines).
   - `[Feature]Card.tsx`: Atomic item card (~60–90 lines).
   - `[Feature]Controls.tsx`: Interactive action buttons and toggles (~50–80 lines).
   - `lib/[feature].ts`: Data fetchers and transformers (~60–100 lines).

---

## 3. Linter, Static Analysis & Code Hygiene Matrix

Every change must maintain zero lint errors and zero type warnings.

| Tool | Purpose | Configuration / Command |
| :--- | :--- | :--- |
| **ESLint (Next.js + React 19)** | Hook rules (`set-state-in-effect`), import order, unused vars, accessibility | `npm run lint` / `npx eslint src` |
| **TypeScript Compiler** | Strict type checking (`noImplicitAny`, exact optional property types) | `npx tsc --noEmit` / `npm run build` |
| **PostCSS + Tailwind v4** | CSS syntax, theme tokens, autoprefixing | Automated via `@tailwindcss/postcss` |

### Strict Hygiene Invariants:
- **Zero Wildcard / Dead Imports**: All unused imports must be stripped before finalizing any edit.
- **Mandatory Trailing Commas**: Required on all multi-line object literals, parameter lists, and array declarations.
- **Zero `any` Types**: Every model, event, and callback must have explicit TypeScript typing.
- **Zero Hydration Mismatches**: Dynamic client-only values (dates, localStorage counters) must be protected by `suppressHydrationWarning` and safe singleton state stores.

---

## 4. Why AI Models Bypass Rules & Real-Time "Vibe Coding" Enforcement

### The "Vibe-Coding" Dilemma:
During rapid iterative prompts, AI agents frequently append code into existing files without noticing they have grown from 120 to 450 lines. Line limits and modularity must be enforced in **REAL TIME on EVERY SINGLE PROMPT TURN**.

---

### Mandatory Pre-Tool "Line Budget Receipt" (Agent Contract)
Before calling `write_to_file` or modifying code with `replace_file_content`, you **MUST** internally calculate and output a line budget audit:

```text
[LINE BUDGET AUDIT]
Target: <FileName.tsx>
Current Lines: <count> | Resulting Lines: <count> | Limit: 250 lines
Status: [PASS / DECOMPOSE REQUIRED]
```

- **If Status is PASS**: Proceed with saving.
- **If Status is DECOMPOSE REQUIRED (Resulting Lines > 250)**: You are **HARD-BLOCKED** from modifying the file in-place. You must immediately create a sub-file (e.g., `*Components.tsx`, `*Nav.tsx`, `*Card.tsx`, or `lib/*.ts`) and offload code there first.

---

### Mechanical Interception: Antigravity Lifecycle Hook (`hooks.json`)
The project utilizes `.agents/hooks.json` and `.agents/scripts/check_line_limit.py` to mechanically block any file write or replacement that would cause `.kt`, `.ts`, or `.tsx` files to exceed 250 lines. When triggered, the tool execution fails immediately, forcing decomposition.

---

## 5. Shell Command & Interactive Input Safety Rules

When running shell commands:
1. **Before executing any shell command, determine whether it can require interactive user input.**
2. Treat a command as interactive if it may:
   - Ask for confirmation such as `Y/N`, `yes/no`, or `Are you sure?`
   - Prompt for passwords, credentials, or authentication tokens
   - Open an interactive CLI wizard (`npm init`, `firebase login`)
   - Start an interactive REPL or debugger
3. **Do not execute an interactive command through the automated shell.**
4. If manual input is required, clearly label it:

   **MANUAL TERMINAL INPUT REQUIRED**
   ```text
   <command>
   ```
   Then explain exactly what needs to be entered.

---

## 6. Recommended Modern Tech Stack Matrix (2025/2026 Standards)

For the FootballClips web platform, use modern, production-tested web technologies:

| Category | Library / Tool | Description & Best Practice |
| :--- | :--- | :--- |
| **Framework** | **Next.js 16 (App Router)** | Turbopack engine, React Server Components (RSC) by default. |
| **UI Library** | **React 19** | Actions, `useTransition`, `useSyncExternalStore`, zero legacy lifecycles. |
| **Styling** | **Tailwind CSS v4** | Modern `@theme` CSS layer, kinetic dark editorial palette. |
| **Icons** | **Lucide React** | Lightweight, clean, accessible vector icon set. |
| **Database & Auth** | **@supabase/supabase-js v2** | Direct Supabase PostgreSQL client for live matches, fixtures, and cached recaps. |
| **AI / LLM** | **@google/genai (Gemini 3.8 Flash)** | Real-time tactical analysis with 3-second timeout and cascading fallback. |
| **Monetization** | **Google AdSense + RevenueCat / Paddle** | Next-Gen native in-feed responsive ads + Web paywall subscription gating. |
| **Media Player** | **YouTube Iframe API** | Responsive 16:9 HD modal and 70/30 match theater video playback. |

---

## 7. Clean Architecture & Layer Decoupling

Enforce strict boundaries across web architectural layers:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                       │
│    (Server Pages, Client Components, Modals, Feed Cards)    │
└──────────────────────────────┬──────────────────────────────┘
                               │ depends on
                               ▼
┌─────────────────────────────────────────────────────────────┐
│                       Domain Layer                          │
│   (Domain Models: MatchRecord, Club; Formatting, Filters)   │
└──────────────────────────────▲──────────────────────────────┘
                               │ implemented by
┌─────────────────────────────────────────────────────────────┐
│                        Data Layer                           │
│  (Supabase Client, Cloudflare Middleware, Gemini SDK, RSS)  │
└─────────────────────────────────────────────────────────────┘
```

### Layer Constraints:
1. **Presentation Layer (`src/app/`, `src/components/`)**:
   - Consumes Domain models (`MatchRecord`, `FavoriteClub`).
   - Does not perform direct SQL queries or un-sanitized raw HTTP calls; uses data access functions from `src/lib/`.
2. **Domain Layer (`src/lib/constants.ts`, `src/lib/clubs.ts`)**:
   - Pure TypeScript functions for filtering (`isLeagueMatch`, `isMatchForFavoriteClubs`).
   - Free of DOM dependencies and browser-specific globals.
3. **Data Layer (`src/lib/supabase.ts`, `src/lib/gemini.ts`, `src/lib/articles.ts`)**:
   - Encapsulates external APIs, error fallback cascades, caching layers, and DTO mappers.

---

## 8. DTO Pattern, Entity Separation & Explicit Mappers

External network contracts and database rows must never leak un-mapped into UI components:

1. **ID Type Safety**:
   - Supabase IDs are numeric (`number`).
   - Cloudflare Worker middleware IDs are strings (`mw-12345`).
   - **Always** use `String(match.id)` before invoking string methods (e.g. `String(match.id).startsWith('mw-')`).
2. **Explicit Mappers**:
   - Keep mappers pure and centralized (e.g. `middlewareHighlightToMatchRecord`).
   - Supply sensible defaults for nullable fields (`home_score ?? 0`, crest fallbacks).

---

## 9. Reactive State Management & Concurrency (React 19)

1. **External Store Synchronization (`useSyncExternalStore`)**:
   - When reading client-only stores (`localStorage` for My Clubs, Spoiler-Free, Sidebar collapse):
   - **`getServerSnapshot()` MUST return a cached singleton reference** (e.g. `EMPTY_FAVORITE_CLUBS_SET = new Set()`). Returning a new reference on every call creates infinite re-render loops.
   - Guard against SSR mismatch using `suppressHydrationWarning` on elements showing client counts.
2. **No Synchronous `setState` in Effects**:
   - Adhere to `react-hooks/set-state-in-effect`. Defer state initializations inside `setTimeout(..., 0)` or `startTransition`.
3. **Async Operations**:
   - Wrap state updates from async network calls in `startTransition` to keep the UI thread responsive.

---

## 10. Modern Web Component & UI Standards

1. **70/30 Responsive Theater Layout**:
   - Desktop match pages feature a 70% primary video/analysis column and a 30% sticky sidebar for related highlights and fixtures.
2. **Spoiler-Free Presentation Flow**:
   - Scores and outcome badges must respect `useSpoilerFree()`.
   - Hidden scores render a blur overlay with an interactive `Reveal` button.
3. **Fluid Desktop & Mobile Navigation**:
   - Desktop: Left collapsible sidebar (`w-64/72` ➔ `w-20`) preserving user preference via `localStorage`.
   - Mobile: Fixed top header with responsive slide-over drawer and quick actions.

---

## 11. Package Structuring & Directory Layout

```text
web/
├── src/
│   ├── app/                    # Next.js App Router routes
│   │   ├── layout.tsx          # Root HTML layout with providers & sidebar
│   │   ├── page.tsx            # Home feed (Recent Highlights + My Clubs)
│   │   ├── fixtures/page.tsx   # Scores & Fixtures hub
│   │   ├── articles/page.tsx   # Gemini Tactical Debriefs + Live RSS News
│   │   ├── info/page.tsx       # Knowledge Hub & Resources (5 cards)
│   │   ├── standings/page.tsx  # League Standings tables
│   │   └── match/[id]/page.tsx # Match Centre (70/30 Theater)
│   ├── components/             # Reusable UI components
│   │   ├── layout/             # Sidebar, SidebarNav, SidebarBottomControls, Footer
│   │   ├── feed/               # HighlightCard, In-feed ads
│   │   ├── match/              # MatchSidebar, MatchCentreHeader
│   │   ├── hero/               # HeroCarousel
│   │   ├── clubs/              # MyClubsModal
│   │   ├── billing/            # PaywallModal
│   │   └── player/             # YouTubeModal, SpoilerThumbnailBox
│   ├── hooks/                  # Custom React 19 hooks
│   └── lib/                    # Supabase, Gemini, Clubs, Constants, RevenueCat
```

---

## 12. Production Hardening, Security, Privacy & Accessibility

1. **Accessibility (WCAG AA)**:
   - Minimum interactive touch targets of **48px** (`min-h-[48px]`, `p-2.5`, `rounded-xl`).
   - Interactive icons must have explicit `aria-label` or `title` attributes.
   - Purely decorative icons must be accompanied by `aria-hidden="true"`.
2. **Zero Hardcoded Secrets**:
   - Never embed Supabase Service Role keys or Gemini API keys in client-side code.
   - Read keys server-side via `process.env.*`.
3. **AdSense Layout Hygiene**:
   - Native in-feed ads must match highlight card dimensions (`min-h-[280px]`) with smooth borders to eliminate Cumulative Layout Shift (CLS).

---

## 13. Automated Testing Track & CI/CD Deployment

Before deploying:
1. **Lint Check**: `npm run lint` must pass with **0 errors and 0 warnings**.
2. **Build Verification**: `npm run build` must compile and prerender all 14 routes cleanly.
3. **Route Smoke Test**: Verify HTTP 200 on `/`, `/fixtures`, `/articles`, `/info`, `/standings`, and `/match/[id]`.

---

## 14. Configuration & Dependency Catalog Template

### `package.json`
Dependencies must remain pinned to modern, stable releases:
- `next`: `16.4.0+`
- `react`: `19.3.0+`
- `tailwindcss`: `^4.0.0`
- `@supabase/supabase-js`: `^2.49.0`
- `@google/genai`: `^0.1.1`
- `lucide-react`: `^1.16.0`

---

## 15. Testing & Verification Runbook

1. **Linter**: `npx eslint src`
2. **TypeScript**: `npx tsc --noEmit`
3. **Build**: `npm run build`
4. **Server Smoke Test**:
   ```powershell
   @("/", "/fixtures", "/articles", "/info", "/standings") | ForEach-Object {
       (Invoke-WebRequest -Uri "http://localhost:3000$_" -UseBasicParsing).StatusCode
   }
   ```

---

## 16. Project Initialization & Feature Execution Order

When implementing a web feature:
1. **Skill & Parity Check**: Check Android app counterpart for data schemas and UI behaviors.
2. **Data & Types**: Define domain models in `src/lib/`.
3. **Sub-components**: Build atomic cards and controls under 80 lines each.
4. **Route Container**: Assemble in `page.tsx` under 250 lines.
5. **Lint & Build**: Run `npm run lint` and `npm run build` to guarantee zero errors.

---

## 17. UI Resources, Semantic Theming & Zero-Hardcoding Manifesto

> [!CAUTION]
> **ZERO TOLERANCE FOR UI MAGIC VALUES**: Arbitrary random hex colors, magic margin numbers, and untyped fonts are **strictly forbidden**.

### Kinetic Editorial Color Tokens (Android Parity):
All web components must use the exact tokens matching the Android design system:

| Role | Token Value | Usage |
| :--- | :--- | :--- |
| **Canvas Background** | `#101418` | Root page background |
| **Surface Container** | `#181C20` | Sidebar, modals, header cards |
| **Surface Card** | `#1E2227` | Content cards, highlight cards, fixture items |
| **Border Tone** | `#334155` / `rgba(51,65,85,0.6)` | Card borders, dividers |
| **Primary Accent** | `#7098F8` | Electric Blue: active links, primary buttons, badges |
| **Secondary Accent** | `#4FC3F7` | Cyan: spoiler pills, gradient stops, sparkles |
| **Warning / Favorite**| `#FBBF24` | Amber star for My Clubs |
| **Success / Live** | `#10B981` | Emerald badge for live matches and PRO status |
| **Text Primary** | `#FFFFFF` / `#F1F5F9` | Headlines, scores, team names |
| **Text Secondary** | `#94A3B8` | Dates, league titles, captions |

### Spacing & Grid System:
All padding and margins must adhere to an **8px / 4px baseline**:
- `p-1` (4px), `p-2` (8px), `p-3` (12px), `p-4` (16px), `p-6` (24px), `p-8` (32px).
- Border radii: `rounded-xl` (12px), `rounded-2xl` (16px), `rounded-3xl` (24px).

### Zero Dummy Data Invariant:
- Real Supabase matches, verified Cloudflare Worker highlights, live Gemini 3.8 analysis, and official sports RSS feeds. Mock or fake data is strictly prohibited.
