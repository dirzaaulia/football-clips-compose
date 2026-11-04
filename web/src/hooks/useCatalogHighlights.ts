'use client';

import { useMemo } from 'react';
import { useInfiniteQuery } from '@tanstack/react-query';
import {
  MatchRecord,
  HighlightCursor,
  getRecentHighlights,
  getTopHighlightsGroupedByCompetition,
} from '@/lib/database';
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

const PRIORITY_COMPETITIONS = [
  'CL', 'EL', 'ECL', 'PL', 'PD', 'SA', 'BL1', 'FL1',
  'FAC', 'CDR', 'CIT', 'DFB', 'EFL',
  'DED', 'PPL', 'SPL', 'MLS', 'BRA', 'ARG',
];

export function useInfiniteHighlights(
  selectedComp = 'ALL',
  season = 2026,
  pageSize = 20,
  query = ''
): UseInfiniteHighlightsResult {
  const queryResult = useInfiniteQuery({
    queryKey: ['highlights', selectedComp, season, pageSize, query],
    queryFn: async ({ pageParam }) => {
      if (selectedComp === 'ALL' && !query && !pageParam) {
        const grouped = await getTopHighlightsGroupedByCompetition(
          PRIORITY_COMPETITIONS,
          season,
          20
        );
        const allMatches = Object.values(grouped).flat();
        return allMatches.map(enrichDatabaseMatch);
      }
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
      if (selectedComp === 'ALL' && !query) return undefined;
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
