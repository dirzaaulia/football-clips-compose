import { readFile } from 'node:fs/promises';
import { test } from 'node:test';
import assert from 'node:assert/strict';

const source = (await readFile(new URL('../src/legacy-catalog-router.mjs', import.meta.url), 'utf8'))
  .replace("import original from './shim.js';", "const original = {fetch: async () => new Response('original')};");
const router = (await import(`data:text/javascript;base64,${Buffer.from(source).toString('base64')}`)).default;

test('owner cutover routes to the owned adapter without calling the upstream', async () => {
  const calls = []; const saved = globalThis.fetch;
  globalThis.fetch = async url => {
    calls.push(String(url));
    return String(url).endsWith('/config')
      ? Response.json({ catalog_only: true }) : Response.json({ data: [], source: 'footballclips' });
  };
  try {
    const response = await router.fetch(new Request('https://old.example/?limit=20&offset=40'), {}, {});
    assert.equal((await response.json()).source, 'footballclips');
    assert.equal(calls.length, 2);
    assert.ok(calls[1].includes('/api/legacy/highlights?limit=20&offset=40'));
    assert.equal(response.headers.get('Access-Control-Allow-Origin'), '*');
  } finally { globalThis.fetch = saved; }
});

test('provider remains reversible and unknown policy never silently calls it', async () => {
  const saved = globalThis.fetch;
  try {
    globalThis.fetch = async () => Response.json({ catalog_only: false });
    assert.equal(await (await router.fetch(new Request('https://old.example/'), {}, {})).text(), 'original');
    globalThis.fetch = async () => new Response('unavailable', { status: 503 });
    assert.equal((await router.fetch(new Request('https://old.example/'), {}, {})).status, 503);
  } finally { globalThis.fetch = saved; }
});
