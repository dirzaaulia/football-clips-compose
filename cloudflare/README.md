# ⚡ FootballClips Cloudflare Edge Caching Proxy (`fc-edge-proxy`)

High-performance global edge proxy deployed on Cloudflare Workers providing sub-15ms edge caching and origin connection protection for FootballClips.

- **Live Production URL**: `https://fc-edge-proxy.dirzaaulia11.workers.dev`
- **Origin Backend**: Supabase PostgREST (`eiomktvavndorazreyba.supabase.co`)
- **Cost**: **$0.00 / Month** (100% Perpetual Free Tier — 100,000 requests/day free)

---

## 🎯 Architecture Purpose

1. **Supabase Connection Pool Protection**: Consolidates thousands of concurrent client requests into single origin fetches, preventing pool exhaustion on Supabase's 60-connection free limit.
2. **Egress Bandwidth Offloading**: Cuts up to **95%** of repeated reads, easily preserving the 5 GB/month free egress budget for over 100,000+ daily active users.
3. **Sub-15ms Global Latency**: Responses are cached and served directly from hundreds of Cloudflare Points of Presence (PoPs) worldwide.

---

## 🕒 Multi-Tier Caching Matrix

| Route / Pattern | Resource Description | Edge TTL | Revalidation Strategy |
| :--- | :--- | :---: | :--- |
| `?status=in.(IN_PLAY,PAUSED)` | Live match scores & clocks | **30 seconds** | High-frequency near-live update |
| `?status=eq.FINISHED` | Completed match highlights & results | **24 hours** | Immutable match data |
| `/rest/v1/standings` | Official league tables | **6 hours** | Periodic table updates |
| `?status=eq.TIMED` | Upcoming match schedules | **5 minutes** | Kickoff proximity refresh |
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
