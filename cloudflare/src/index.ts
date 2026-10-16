/**
 * FootballClips Cloudflare Edge Caching Proxy
 * 
 * Sits in front of Supabase PostgREST endpoints to protect against:
 * 1. Supabase direct connection limits (max 60 concurrent)
 * 2. Supabase 5 GB/month egress bandwidth limits
 * 
 * Provides dynamic multi-tier caching:
 * - Live Matches (IN_PLAY, PAUSED): 30s TTL
 * - Finished Matches: 24h (86,400s) TTL
 * - League Standings: 6h (21,600s) TTL
 * - Default / Fixtures: 60s TTL
 */

export interface Env {
  SUPABASE_URL: string;
  SUPABASE_ANON_KEY: string;
  PURGE_SECRET?: string;
}

export default {
  async fetch(request: Request, env: Env, ctx: ExecutionContext): Promise<Response> {
    const url = new URL(request.url);

    // 1. Handle CORS Preflight
    if (request.method === "OPTIONS") {
      const requestedHeaders = request.headers.get("Access-Control-Request-Headers") || "*";
      return new Response(null, {
        headers: {
          "Access-Control-Allow-Origin": "*",
          "Access-Control-Allow-Methods": "GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS",
          "Access-Control-Allow-Headers": requestedHeaders,
          "Access-Control-Expose-Headers": "Content-Range, Range, Content-Length, X-FC-Cache",
          "Access-Control-Max-Age": "86400",
        },
      });
    }

    // 2. Cache Purge Hook
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
        return new Response(JSON.stringify({ status: "Purged", target }), {
          headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
        });
      } catch (e) {
        return new Response(JSON.stringify({ status: "Purge error", error: String(e) }), {
          status: 500,
          headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
        });
      }
    }

    // Only cache GET and HEAD requests
    if (request.method !== "GET" && request.method !== "HEAD") {
      const resp = await forwardToSupabase(request, env, url);
      const headers = new Headers(resp.headers);
      headers.set("Access-Control-Allow-Origin", "*");
      headers.set("Access-Control-Expose-Headers", "Content-Range, Range, Content-Length, X-FC-Cache");
      return new Response(resp.body, {
        status: resp.status,
        statusText: resp.statusText,
        headers,
      });
    }

    // 3. Inspect request for Cache TTL Rules
    const cacheTtl = determineTtl(url);

    // 4. Edge Cache Lookup (Normalized URL Key for universal cache sharing)
    const cacheKey = new Request(url.toString(), { method: "GET" });
    const cache = caches.default;
    let cachedResponse = await cache.match(cacheKey);

    if (cachedResponse) {
      const newHeaders = new Headers(cachedResponse.headers);
      newHeaders.set("X-FC-Cache", "HIT");
      newHeaders.set("Access-Control-Allow-Origin", "*");
      newHeaders.set("Access-Control-Expose-Headers", "Content-Range, Range, Content-Length, X-FC-Cache");
      return new Response(cachedResponse.body, {
        status: cachedResponse.status,
        statusText: cachedResponse.statusText,
        headers: newHeaders,
      });
    }

    // 5. Cache MISS -> Fetch from Origin (Supabase)
    const originResponse = await forwardToSupabase(request, env, url, cacheTtl);

    // Only cache successful 200/206 responses
    if (originResponse.status === 200 || originResponse.status === 206) {
      const responseHeaders = new Headers(originResponse.headers);
      responseHeaders.delete("set-cookie");
      responseHeaders.set("X-FC-Cache", "MISS");
      responseHeaders.set("Access-Control-Allow-Origin", "*");
      responseHeaders.set("Access-Control-Expose-Headers", "Content-Range, Range, Content-Length, X-FC-Cache");
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

    // Even for errors (4xx, 5xx), return with CORS headers so client gets json error instead of opaque CORS failure
    const errorHeaders = new Headers(originResponse.headers);
    errorHeaders.set("Access-Control-Allow-Origin", "*");
    errorHeaders.set("Access-Control-Expose-Headers", "Content-Range, Range, Content-Length, X-FC-Cache");
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

  // A. Standings endpoints: 6 hours (21,600 seconds)
  if (path.includes("standings")) {
    return 21600;
  }

  // B. Live matches query: 30 seconds
  if (
    search.includes("in_play") ||
    search.includes("paused") ||
    search.includes("status=in.(in_play,paused)")
  ) {
    return 30;
  }

  // C. Finished matches query: 24 hours (86,400 seconds)
  if (
    search.includes("status=eq.finished") ||
    search.includes("status=eq.full_time")
  ) {
    return 86400;
  }

  // D. Upcoming fixtures / schedule: 5 minutes (300 seconds)
  if (
    search.includes("status=eq.timed") ||
    search.includes("status=eq.scheduled")
  ) {
    return 300;
  }

  // E. Default fallback: 60 seconds
  return 60;
}

async function forwardToSupabase(request: Request, env: Env, url: URL, cacheTtl: number = 60): Promise<Response> {
  const targetBase = (env.SUPABASE_URL || "https://eiomktvavndorazreyba.supabase.co").replace(/\/$/, "");
  
  // Normalize path: strip /rest/v1 prefix when routing to standalone PostgREST
  const normalizedPath = url.pathname.startsWith("/rest/v1") && !targetBase.includes("supabase.co")
    ? url.pathname.replace(/^\/rest\/v1/, "")
    : url.pathname;
  const targetUrl = `${targetBase}${normalizedPath}${url.search}`;

  const forwardHeaders = new Headers(request.headers);
  if (targetBase.includes("supabase.co")) {
    if (env.SUPABASE_ANON_KEY) {
      forwardHeaders.set("apikey", env.SUPABASE_ANON_KEY || forwardHeaders.get("apikey") || "");
      if (!forwardHeaders.has("Authorization")) {
        forwardHeaders.set("Authorization", `Bearer ${env.SUPABASE_ANON_KEY}`);
      }
    }
  } else {
    // Standalone PostgREST serves anonymous role web_anon without requiring JWT secret
    forwardHeaders.delete("authorization");
    forwardHeaders.delete("apikey");
  }

  return fetch(targetUrl, {
    method: request.method,
    headers: forwardHeaders,
    body: request.method !== "GET" && request.method !== "HEAD" ? request.body : undefined,
    cf: {
      cacheEverything: true,
      cacheTtl: cacheTtl,
    },
  });
}
