# ⚡ FootballClips Cloudflare Edge Caching Proxy (`fc-edge-proxy`)

High-performance global edge proxy deployed on Cloudflare Workers providing sub-15ms edge caching and origin connection protection for FootballClips.

- **Live Production URLs**:
  - Custom Domain: `https://edge.fc.dirzaaulia.com`
  - Workers Domain: `https://fc-edge-proxy.dirzaaulia11.workers.dev`
- **Origin Backends (Dual-Origin Routing)**:
  - **High-Volume Sports Data** (`matches`, `standings`, `supported_clubs`, `worker_cycles`, `worker_events`): Self-hosted PostgreSQL 16 + PostgREST on Oracle VM (`https://168.110.196.30.sslip.io`) with **zero egress limits**.
  - **Authentication & Profiles** (`auth`, `profiles`, `storage`): Supabase Cloud (`eiomktvavndorazreyba.supabase.co`) with intact JWT passthrough.
- **Security & Headers**: Strict HSTS (`max-age=31536000`), CSP, `X-Content-Type-Options: nosniff`, `X-Frame-Options: DENY`, `Referrer-Policy: strict-origin-when-cross-origin`.
- **Cost**: **$0.00 / Month** (100% Perpetual Free Tier — 100,000 requests/day free)

---

## 🎯 Architecture Purpose

1. **Dual-Origin Router**: Seamlessly routes high-throughput sports queries to self-hosted PostgreSQL 16 on the Oracle Super-Node, eliminating Supabase free-tier connection limits and egress ceilings.
2. **Egress Bandwidth Offloading**: Cuts up to **95%** of repeated reads, providing lightning-fast edge response times (<15ms) across global Cloudflare edge nodes.
3. **Sub-15ms Global Latency**: Responses are cached and served directly from hundreds of Cloudflare Points of Presence (PoPs) worldwide.

---

## 🕒 Multi-Tier Caching Matrix

| Route / Pattern | Resource Description | Edge TTL | Revalidation Strategy |
| :--- | :--- | :---: | :--- |
| `?status=in.(IN_PLAY,PAUSED)` | Live match scores & clocks | **30 seconds** | High-frequency near-live update |
| `?status=eq.FINISHED` | Completed match highlights & results | **24 hours** | Immutable match data |
| `/rest/v1/standings` | Official league tables | **6 hours** | Periodic table updates |
| `?status=eq.TIMED` | Upcoming match schedules | **5 minutes** | Kickoff proximity refresh |
| `/auth/v1/*` or `/rest/v1/profiles*` | User auth & profile data | **0 seconds** | Strictly BYPASS (private) |
| All other queries | Fallback default | **60 seconds** | `stale-while-revalidate` (60s) |

---

## 🧹 Instant Edge Cache Purge API

The worker exposes an authenticated cache invalidation endpoint:

```http
POST /api/purge-cache
X-Purge-Secret: <PURGE_SECRET>
```

- Triggered automatically by `worker/clients/edge_cache_client.py` whenever `HighlightService` links a newly verified video highlight clip to a match.
- Can also be triggered directly from the **WASM Telemetry Command Center** toolbar button ("🧹 Purge Cache").

---

## 🚀 Deployment Instructions

```bash
# Install dependencies
npm install

# Test worker locally
npx wrangler dev

# Deploy to Cloudflare Workers
npx wrangler deploy
```
