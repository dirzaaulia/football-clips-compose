# Web Development Rules (Next.js 16, React 19 & Kinetic Editorial)

> Master Rules Document: [`.agents/rules/WEB_NEXTJS_REACT19_MASTER_RULES.md`](file:///d:/Android/Projects/FootballClips/.agents/rules/WEB_NEXTJS_REACT19_MASTER_RULES.md)

## Core Architectural Invariants
- **Framework & Runtime**: Next.js 16 (App Router), React 19, TypeScript 5 (strict), Tailwind CSS v4.
- **Android App Parity**: Kinetic Editorial dark tokens (`#101418`, `#181C20`, `#1E2227`, `#7098F8`, `#4FC3F7`) adhering to FootballClips Compose Multiplatform design language.
- **Strict Size Limits**:
  - Max file size: **250 lines** (hard ceiling **300 lines**). Decompose files approaching the limit into sub-components (`*Nav.tsx`, `*Controls.tsx`, `*Card.tsx`).
  - Max function/component length: **40 lines**.
- **Data Integrity & Zero Dummy Data**:
  - Only real data from Supabase, Cloudflare Worker middleware, Gemini 3.8 Flash, and verified sports RSS feeds (BBC, Sky).
  - Strict ban on placeholder mockups, lorem ipsum, and synthetic scores.
- **State & Concurrency**:
  - `useSyncExternalStore` must return cached singleton snapshots (`getServerSnapshot`) to avoid infinite re-render loops.
  - Defer client state initialization inside `setTimeout(..., 0)` or `startTransition` to adhere to `react-hooks/set-state-in-effect`.
  - Protect dynamic client counts and timestamps with `suppressHydrationWarning`.
- **Code Quality**:
  - `npm run lint` must pass with **0 errors and 0 warnings** at all times.
  - `npm run build` must compile 14/14 static & dynamic routes cleanly.

<!-- BEGIN:nextjs-agent-rules -->

## This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` (resolved from this file's directory; in monorepos the `next` package may not be visible from the repo root) before writing any code. Heed deprecation notices.

This block is written and re-added by `next dev` — verify at `node_modules/next/dist/server/lib/generate-agent-files.js`. Removing it from a diff only re-creates the uncommitted change; committing it with your work keeps the tree clean.

<!-- END:nextjs-agent-rules -->
