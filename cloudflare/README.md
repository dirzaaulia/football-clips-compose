# ⚡ FootballClips Cloudflare Edge Caching Proxy

This Cloudflare Worker provides a global, zero-cost edge caching layer in front of the Supabase PostgREST endpoints.

## 🎯 Architecture Purpose ($0 Ceiling Maximization)
- **Direct Connection Protection**: Supabase free-tier allows max 60 direct Postgres connection pools. Edge caching consolidates thousands of concurrent client requests into single origin fetches.
- **Egress Bandwidth Optimization**: Supabase free-tier limits egress to 5 GB/month. Edge caching offloads up to **95%** of repeated reads, supporting 100,000+ DAU.
- **Sub-15ms Global Latency**: Responses are served directly from hundreds of Cloudflare PoPs nearest to the user.

## 🕒 Cache Policy
| Query / Endpoint | Target Resource | TTL | Strategy |
| :--- | :--- | :---: | :--- |
| `?status=in.(IN_PLAY,PAUSED)` | Live match scores & clocks | **30 seconds** | High-frequency update |
| `?status=eq.FINISHED` | Completed match highlights & results | **24 hours** | Immutable match data |
| `/rest/v1/standings` | League table rankings | **6 hours** | Post-matchday updates |
| `?status=eq.TIMED` | Upcoming match schedules | **5 minutes** | Kickoff proximity updates |
| All other queries | Fallback default | **60 seconds** | Stale-while-revalidate (60s) |

## 🚀 Deployment Instructions
1. Install dependencies:
   ```bash
   cd cloudflare
   npm install
   ```
2. Login to Cloudflare:
   ```bash
   npx wrangler login
   ```
3. Deploy to Cloudflare Free Tier (100,000 requests/day free):
   ```bash
   npx wrangler deploy
   ```
4. Point client applications (Android app / Web app) to `https://fc-edge-proxy.<your-subdomain>.workers.dev` instead of the raw Supabase URL.
