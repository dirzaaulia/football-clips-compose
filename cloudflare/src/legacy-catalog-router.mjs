import original from './shim.js';

const origin = 'https://api.fc.dirzaaulia.com';

export default {
  async fetch(request, env, context) {
    if (request.method === 'OPTIONS') {
      return new Response(null, { status: 204, headers: {
        'Access-Control-Allow-Origin': '*', 'Access-Control-Allow-Headers': '*',
        'Access-Control-Allow-Methods': 'GET,HEAD,OPTIONS',
      } });
    }
    let policy;
    try {
      const response = await fetch(`${origin}/api/v2/config`, { cf: { cacheTtl: 0 } });
      if (!response.ok) throw new Error('Policy unavailable');
      policy = await response.json();
    } catch {
      return new Response(JSON.stringify({ error: 'Catalog policy unavailable; retry later' }), { status: 503 });
    }
    if (!policy.catalog_only) return original.fetch(request, env, context);
    if (!['GET', 'HEAD'].includes(request.method)) return new Response('Method not allowed', { status: 405 });
    const target = new URL(`${origin}/api/legacy/highlights`);
    target.search = new URL(request.url).search;
    const response = await fetch(target, { method: request.method, headers: {
      'CF-IPCountry': request.headers.get('CF-IPCountry') || 'ID',
    }, cf: { cacheTtl: 0 } });
    const headers = new Headers(response.headers);
    headers.set('Access-Control-Allow-Origin', '*');
    headers.set('Access-Control-Expose-Headers', 'ETag');
    return new Response(response.body, { status: response.status, headers });
  },
};
