let namespace = { value: '', expires: 0 };

export function privateRoute(path: string): boolean {
  return /\/(?:telemetry_state|worker_cycles|worker_events|app_sessions|app_crashes)(?:\/|$)/.test(path)
    || path.startsWith('/api/ops/') || path.startsWith('/internal/');
}

export async function catalogCacheKey(url: URL, origin: string, country: string): Promise<Request | null> {
  if (namespace.expires < Date.now()) {
    try {
      const response = await fetch(`${origin}/api/v2/cache-version`, { cf: { cacheTtl: 0 } });
      if (!response.ok) return null;
      const data = await response.json() as { version: string };
      namespace = { value: data.version, expires: Date.now() + 10_000 };
    } catch { return null; }
  }
  const key = new URL(url);
  key.searchParams.set('__catalog_version', namespace.value);
  key.searchParams.set('__country', country);
  return new Request(key, { method: 'GET' });
}
