# ⚽ Football Highlights & Clips Web Portal

The official web platform for **Football Highlights & Clips**, built with **Next.js 16 (App Router)**, **React 19**, **TypeScript**, and **Tailwind CSS**, deployed globally via **Firebase Hosting** at [fc.dirzaaulia.com](https://fc.dirzaaulia.com).

---

## ✨ Features & Architecture

- **Next.js 16 App Router & Turbopack**: High-performance rendering with Static Site Generation (SSG) and Incremental Static Regeneration (ISR).
- **Match Explorer & Live Scores**: Real-time scoreboards, match statuses (`LIVE`, `FINISHED`, `SCHEDULED`), league filters, and team crests.
- **Match Details & Video Highlights**: Dedicated match pages (`/match/[id]`) with responsive official highlight video player, lineups, and match stats.
- **Web Push Notifications (FCM)**:
  - Background Service Worker (`public/firebase-messaging-sw.js`) handling real-time goal alerts, kickoff reminders, and highlight releases.
  - Subscribes to zero-cost FCM topics (`club_{club_id}`) in sync with user favorites.
- **Contextual "My Clubs" Opt-In**:
  - Material-inspired educational opt-in modal (`NotificationOptInModal.tsx`).
  - Adheres to best practices: No cold-start permission popups; prompt triggers naturally only when a user stars a club.
- **Google AdSense Monetization**: Clean, policy-compliant display ad integration.
- **SEO & Articles Knowledge Hub**: Editorial articles (`/articles`), tournament insights, and official policy pages (`/privacy`, `/terms`, `/contact`).
- **Telemetry Integration**: Seamless routing to the Kotlin/Wasm Telemetry & Observability dashboard at `/telemetry`.

---

## 📁 Directory Structure

```
web/
├── public/
│   ├── firebase-messaging-sw.js # FCM Background Web Push Service Worker
│   ├── ads.txt                  # Authorized Digital Sellers configuration
│   └── robots.txt               # Search engine crawler directives
├── src/
│   ├── app/                     # Next.js App Router pages
│   │   ├── page.tsx             # Home: Highlights & Today's Matches
│   │   ├── fixtures/            # Fixtures & Schedule calendar
│   │   ├── standings/           # League tables & standings
│   │   ├── match/[id]/          # Deep-linked match details & video player
│   │   ├── articles/            # SEO football articles & news
│   │   ├── privacy/             # Privacy Policy page
│   │   ├── terms/               # Terms of Service page
│   │   └── contact/             # Contact page
│   ├── components/              # Modular React 19 UI components
│   │   ├── ads/                 # Google AdSense wrappers
│   │   ├── clubs/               # My Clubs drawer & NotificationOptInModal
│   │   ├── fixtures/            # Match cards, scoreboards, and league pills
│   │   └── layout/              # Navbar, footer, and navigation sheets
│   ├── lib/                     # Utilities & API clients
│   │   ├── firebase/            # Firebase client & FCM messaging setup
│   │   └── supabase/            # Supabase Postgrest client & queries
│   └── types/                   # TypeScript interfaces (Match, Highlight, Club)
├── next.config.ts               # Next.js configuration (static export, headers)
└── package.json                 # Dependencies & scripts
```

---

## 🛠️ Environment Configuration

Create a `.env.local` file in the `web/` directory with the following variables:

```env
NEXT_PUBLIC_SUPABASE_URL=https://your-project.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your-supabase-anon-key
NEXT_PUBLIC_FIREBASE_API_KEY=your-firebase-api-key
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=highlightdata.firebaseapp.com
NEXT_PUBLIC_FIREBASE_PROJECT_ID=highlightdata
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=your-sender-id
NEXT_PUBLIC_FIREBASE_APP_ID=your-app-id
NEXT_PUBLIC_ADSENSE_CLIENT_ID=ca-pub-xxxxxxxxxxxxxxxx
```

---

## 🚀 Development & Build

```bash
# Install dependencies
npm install

# Run local development server with Turbopack
npm run dev

# Run TypeScript check & production static export
npm run build
```

---

## 🚢 Deployment to Firebase Hosting

The production build outputs static assets to `web/out`. Deployments are managed via the root deployment utility:

```bash
# From repository root
python scripts/deploy_web.py
```

This builds Next.js, bundles the Kotlin/Wasm Telemetry dashboard into `/telemetry`, and deploys both to Firebase Hosting (`footballclips` and `footballclips-telemetry` targets).
