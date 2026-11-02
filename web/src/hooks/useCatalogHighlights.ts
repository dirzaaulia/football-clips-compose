'use client';

import { useMemo } from 'react';
import { useInfiniteQuery } from '@tanstack/react-query';
import { MatchRecord, HighlightCursor, getRecentHighlights } from '@/lib/database';
import { enrichDatabaseMatch } from '@/lib/middleware-highlights';

export interface UseInfiniteHighlightsResult {
  highlights: MatchRecord[];
  isLoading: boolean;
  isLoadingMore: boolean;
  hasMore: boolean;
  error: unknown;
  loadMore: () => void;
  refresh: () => void;
}

export function useInfiniteHighlights(
  selectedComp = 'ALL',
  season = 2026,
  pageSize = 20,
  query = ''
): UseInfiniteHighlightsResult {
  const queryResult = useInfiniteQuery({
    queryKey: ['highlights', selectedComp, season, pageSize, query],
    queryFn: async ({ pageParam }) => {
      const records = await getRecentHighlights(
        pageSize,
        pageParam as HighlightCursor | undefined,
        selectedComp,
        season,
        query
      );
      return records.map(enrichDatabaseMatch);
    },
    initialPageParam: undefined as HighlightCursor | undefined,
    getNextPageParam: (lastPage) => {
      if (!lastPage || lastPage.length < pageSize) return undefined;
      const last = lastPage[lastPage.length - 1];
      if (!last || !last.utc_date || !last.id) return undefined;
      return { date: last.utc_date, id: String(last.id) };
    },
  });

  const highlights = useMemo(() => {
    if (!queryResult.data?.pages) return [];
    const flattened = queryResult.data.pages.flat();
    return [...new Map(flattened.map((item) => [item.id, item])).values()];
  }, [queryResult.data]);

  return {
    highlights,
    isLoading: queryResult.isLoading,
    isLoadingMore: queryResult.isFetchingNextPage,
    hasMore: Boolean(queryResult.hasNextPage),
    error: queryResult.error,
    loadMore: () => {
      if (queryResult.hasNextPage && !queryResult.isFetchingNextPage) {
        void queryResult.fetchNextPage();
      }
    },
    refresh: () => {
      void queryResult.refetch();
    },
  };
}
