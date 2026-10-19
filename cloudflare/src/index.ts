/**
 * FootballClips Cloudflare Edge Caching Proxy & Universal Router
 * 
 * Provides backwards-compatible intelligent routing and multi-tier edge caching:
 * 1. High-Volume Match & Fixture Data (matches, standings, clubs, worker events):
 *    -> Routed to self-hosted PostgreSQL 16 + PostgREST on Oracle VM (Strict HTTPS, 0 egress limits)
 * 2. User Authentication & Profile Data (auth, profiles, storage):
 *    -> Seamlessly routed to Supabase (GoTrue Auth, Profiles, Storage) with intact JWTs
 * 
 * Edge Caching:
 * - Live Matches (IN_PLAY, PAUSED): 30s TTL
 * - Finished Matches: 24h (86,400s) TTL
 * - League Standings: 6h (21,600s) TTL
 * - Supported Clubs: 24h (86,400s) TTL
 * - Auth & Profiles: 0s (Cache BYPASS, strictly private)
 */

export interface Env {
  SUPABASE_URL: string;         // Self-hosted PostgREST (https://168.110.196.30.sslip.io)
  SUPABASE_ANON_KEY: string;    // Supabase Anon Key (for Auth/Profiles fallback)
  AUTH_SUPABASE_URL?: string;   // Supabase Auth Origin (default: https://eiomktvavndorazreyba.supabase.co)
  PURGE_SECRET?: string;
}

const POSTGRES_TABLES = [
  "matches",
  "standings",
  "supported_clubs",
  "competitions",
  "worker_cycles",
  "worker_events",
];

function isPostgresRoute(pathname: string): boolean {
  const clean = pathname.replace(/^\/rest\/v1\/?/, "").replace(/^\//, "");
  return POSTGRES_TABLES.some(table => clean === table || clean.startsWith(`${table}?`) || clean.startsWith(`${table}/`));
}

function applySecurityHeaders(headers: Headers, request?: Request) {
  headers.set("Access-Control-Allow-Origin", "*");
  headers.set("Access-Control-Expose-Headers", "Content-Range, Range, Content-Length, X-FC-Cache, X-User-Country");
  headers.set("X-Content-Type-Options", "nosniff");
  headers.set("X-Frame-Options", "DENY");
  headers.set("Referrer-Policy", "strict-origin-when-cross-origin");
  headers.set("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload");
  headers.set("Permissions-Policy", "geolocation=(), camera=(), microphone=()");
  if (request) {
    const cfCountry = request.headers.get("cf-ipcountry");
    if (cfCountry) {
      headers.set("X-User-Country", cfCountry);
    }
  }
}

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);

    // 1. Handle CORS Preflight
    if (request.method === "OPTIONS") {
      const requestedHeaders = request.headers.get("Access-Control-Request-Headers") || "*";
      const headers = new Headers({
        "Access-Control-Allow-Methods": "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS",
        "Access-Control-Allow-Headers": requestedHeaders,
        "Access-Control-Max-Age": "86400",
      });
      applySecurityHeaders(headers, request);
      return new Response(null, { headers });
    }

    // 2. User Geo-Detection Endpoint (Zero latency, 0 quota, Cloudflare Edge IP Country)
    if (url.pathname === "/api/geo" && (request.method === "GET" || request.method === "HEAD")) {
      const country = request.headers.get("cf-ipcountry") || "US";
      const headers = new Headers({ "Content-Type": "application/json" });
      applySecurityHeaders(headers, request);
      headers.set("Cache-Control", "public, max-age=3600");
      return new Response(JSON.stringify({ country }), { headers });
    }

    // 3. Cache Purge Hook
    if (url.pathname === "/api/purge-cache" && request.method === "POST") {
      const authHeader = request.headers.get("Authorization");
      if (env.PURGE_SECRET && authHeader !== `Bearer ${env.PURGE_SECRET}`) {
        return new Response(JSON.stringify({ error: "Unauthorized" }), { status: 401 });
      }
      try {
        const body = (await request.json().catch(() => ({}))) as { target_url?: string };
        const target = body.target_url || `${url.origin}/rest/v1/matches?status=eq.FINISHED`;
        const cache = caches.default;
        await cache.delete(new Request(target, { method: "GET" }));
        const headers = new Headers({ "Content-Type": "application/json" });
        applySecurityHeaders(headers);
        return new Response(JSON.stringify({ status: "Purged", target }), { headers });
      } catch (e) {
        const headers = new Headers({ "Content-Type": "application/json" });
        applySecurityHeaders(headers);
        return new Response(JSON.stringify({ status: "Purged error", error: String(e) }), {
          status: 500,
          headers,
        });
      }
    }

    // 3. Inspect request for Cache TTL Rules
    const cacheTtl = determineTtl(url);

    // Dynamic mutations (POST, PUT, PATCH, DELETE) or non-cacheable routes (Auth, Profiles)
    if (request.method !== "GET" && request.method !== "HEAD" || cacheTtl === 0) {
      const resp = await forwardRequest(request, env, url, 0);
      const headers = new Headers(resp.headers);
      applySecurityHeaders(headers, request);
      if (cacheTtl === 0) {
        headers.set("X-FC-Cache", "BYPASS");
      }
      return new Response(resp.body, {
        status: resp.status,
        statusText: resp.statusText,
        headers,
      });
    }

    // 4. Edge Cache Lookup
    const cacheKey = new Request(url.toString(), { method: "GET" });
    const cache = caches.default;
    let cachedResponse = await cache.match(cacheKey);

    if (cachedResponse) {
      const newHeaders = new Headers(cachedResponse.headers);
      newHeaders.set("X-FC-Cache", "HIT");
      applySecurityHeaders(newHeaders, request);
      return new Response(cachedResponse.body, {
        status: cachedResponse.status,
        statusText: cachedResponse.statusText,
        headers: newHeaders,
      });
    }

    // 5. Cache MISS -> Fetch from Origin
    const originResponse = await forwardRequest(request, env, url, cacheTtl);

    // Only cache successful 200/206 responses
    if (originResponse.status === 200 || originResponse.status === 206) {
      const responseHeaders = new Headers(originResponse.headers);
      responseHeaders.delete("set-cookie");
      responseHeaders.set("X-FC-Cache", "MISS");
      applySecurityHeaders(responseHeaders, request);
      responseHeaders.set(
        "Cache-Control",
        `public, max-age=${cacheTtl}, s-maxage=${cacheTtl}, stale-while-revalidate=60`
      );

      const responseToCache = new Response(originResponse.clone().body, {
        status: originResponse.status,
        statusText: originResponse.statusText,
        headers: responseHeaders,
      });

      // Write asynchronously to Cloudflare Edge Cache
      ctx.waitUntil(cache.put(cacheKey, responseToCache));

      return new Response(originResponse.body, {
        status: originResponse.status,
        statusText: originResponse.statusText,
        headers: responseHeaders,
      });
    }

    // Errors (4xx, 5xx): return with CORS & security headers
    const errorHeaders = new Headers(originResponse.headers);
    applySecurityHeaders(errorHeaders, request);
    return new Response(originResponse.body, {
      status: originResponse.status,
      statusText: originResponse.statusText,
      headers: errorHeaders,
    });
  },
};

function determineTtl(url: URL): number {
  const path = url.pathname.toLowerCase();
  const search = url.search.toLowerCase();

  // A. Never cache authentication, user profile queries, or storage
  if (path.startsWith("/auth/") || path.includes("profiles") || path.startsWith("/storage/")) {
    return 0;
  }

  // B. Standings endpoints: 6 hours
  if (path.includes("standings")) {
    return 21600;
  }

  // C. Supported clubs: 24 hours
  if (path.includes("supported_clubs")) {
    return 86400;
  }

  // D. Live matches query: 30 seconds
  if (
    search.includes("in_play") ||
    search.includes("paused") ||
    search.includes("status=in.(in_play,paused)")
  ) {
    return 30;
  }

  // E. Finished matches query: 24 hours (86,400 seconds)
  if (
    search.includes("status=eq.finished") ||
    search.includes("status=eq.full_time")
  ) {
    return 86400;
  }

  // F. Upcoming fixtures / schedule: 5 minutes (300 seconds)
  if (
    search.includes("status=eq.timed") ||
    search.includes("status=eq.scheduled")
  ) {
    return 300;
  }

  // G. Default fallback: 60 seconds
  return 60;
}

async function forwardRequest(request: Request, env: Env, url: URL, cacheTtl: number = 60): Promise<Response> {
  const isPostgres = isPostgresRoute(url.pathname);
  const forwardHeaders = new Headers(request.headers);

  if (isPostgres) {
    // 1. Route to Oracle VM PostgREST (PostgreSQL 16)
    const targetBase = (env.SUPABASE_URL || "https://168.110.196.30.sslip.io").replace(/\/$/, "");
    const normalizedPath = url.pathname.replace(/^\/rest\/v1/, "");
    const targetUrl = `${targetBase}${normalizedPath}${url.search}`;

    // PostgREST serves web_anon anonymously; strip client auth headers
    forwardHeaders.delete("authorization");
    forwardHeaders.delete("apikey");

    return fetch(targetUrl, {
      method: request.method,
      headers: forwardHeaders,
      body: request.method !== "GET" && request.method !== "HEAD" ? request.body : undefined,
      cf: {
        cacheEverything: cacheTtl > 0,
        cacheTtl: cacheTtl,
      },
    });
  } else {
    // 2. Route to Supabase Auth & Profiles (GoTrue, Profiles, Storage)
    const targetBase = (env.AUTH_SUPABASE_URL || "https://eiomktvavndorazreyba.supabase.co").replace(/\/$/, "");
    const targetUrl = `${targetBase}${url.pathname}${url.search}`;

    if (env.SUPABASE_ANON_KEY) {
      if (!forwardHeaders.has("apikey")) {
        forwardHeaders.set("apikey", env.SUPABASE_ANON_KEY);
      }
      if (!forwardHeaders.has("Authorization")) {
        forwardHeaders.set("Authorization", `Bearer ${env.SUPABASE_ANON_KEY}`);
      }
    }

    return fetch(targetUrl, {
      method: request.method,
      headers: forwardHeaders,
      body: request.method !== "GET" && request.method !== "HEAD" ? request.body : undefined,
      cf: {
        cacheEverything: cacheTtl > 0,
        cacheTtl: cacheTtl,
      },
    });
  }
}
