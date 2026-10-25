'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import type { MatchRecord } from '@/lib/database';
import { enrichDatabaseMatch } from '@/lib/middleware-highlights';

interface CatalogPage { items: MatchRecord[]; next_cursor: string | null; has_more: boolean }
export interface UseInfiniteHighlightsResult {
  highlights: MatchRecord[]; isLoading: boolean; isLoadingMore: boolean;
  hasMore: boolean; error: unknown; loadMore: () => void; refresh: () => void;
}

export function useInfiniteHighlights(selectedComp = 'ALL', season = 2026, pageSize = 20, query = ''): UseInfiniteHighlightsResult {
  const [highlights, setHighlights] = useState<MatchRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [error, setError] = useState<unknown>(null);
  const cursor = useRef<string | null>(null);
  const pending = useRef<AbortController | null>(null);
  const fetchPage = useCallback(async (reset = false) => {
    if (pending.current && !reset) return;
    pending.current?.abort(); const controller = new AbortController(); pending.current = controller;
    setLoading(true); setError(null);
    const params = new URLSearchParams({
      competition: selectedComp,
      season: String(season),
      limit: String(pageSize),
      include_unavailable: 'true',
    });
    if (query.trim()) params.set('q', query.trim());
    if (!reset && cursor.current) params.set('cursor', cursor.current);
    try {
      const response = await fetch(`https://fc-edge-proxy.dirzaaulia11.workers.dev/api/v2/feed?${params}`, { signal: controller.signal });
      if (!response.ok) throw new Error(`Highlights unavailable (${response.status}); retry this page.`);
      const page = await response.json() as CatalogPage;
      page.items = page.items.map(enrichDatabaseMatch);
      if (controller.signal.aborted) return;
      cursor.current = page.next_cursor; setHasMore(page.has_more);
      setHighlights(previous => [...new Map((reset ? page.items : [...previous, ...page.items])
        .map(item => [item.id, item])).values()]);
    } catch (failure) { if (!controller.signal.aborted) setError(failure); }
    finally { if (!controller.signal.aborted) { setLoading(false); pending.current = null; } }
  }, [selectedComp, season, pageSize, query]);
  useEffect(() => {
    const timer = window.setTimeout(() => { cursor.current = null; void fetchPage(true); }, query ? 350 : 0);
    return () => { window.clearTimeout(timer); pending.current?.abort(); pending.current = null; };
  }, [fetchPage]);
  return { highlights, isLoading: loading && highlights.length === 0,
    isLoadingMore: loading && highlights.length > 0, hasMore, error,
    loadMore: () => { if (hasMore) void fetchPage(); }, refresh: () => { void fetchPage(true); } };
}
