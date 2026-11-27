# Next.js Web OTT Specification (`web/`)

## Purpose
Defines Next.js 16 static export (`web/out/`), PostgREST data contracts, Obsidian/Volt OTT UI, TV Broadcast Live Ticker, and SEO invariants.

## Requirements

### Requirement: Static Export & Dynamic Competition Routing
- `npm run build` MUST compile all static/SSG routes (`next export`) with 0 TypeScript or build errors.
- Competitions across Home, Fixtures, Standings, Filters, and Competition Drawer MUST load dynamically from PostgREST `competitions` (`is_active = true`), with fallback metadata in `web/src/lib/competitions.ts` and `web/src/hooks/useCatalogHighlights.ts`.

### Requirement: TV Broadcast Horizontal Marquee Live Ticker
- `LiveScoreTicker.tsx` + `TickerMarqueeRow.tsx` MUST glide horizontally continuously via CSS `@keyframes marquee-glide` (`.animate-marquee`) defined in `web/src/app/globals.css`.
- Tracks MUST tile small match lists (`< 8` items) for gapless looping on 4K displays and pause synchronously on container hover (`group-hover:[animation-play-state:paused]`).

### Requirement: UI & Loading Invariants
- Zero raw Unicode emojis in UI; vector icons (`lucide-react`) only.
- Zero circular spinners (`animate-spin` loading borders); 1:1 structural shimmer skeletons (`.shimmer-effect`) only.
- Shorts feed MUST query `published_at DESC, hype_score DESC`.
