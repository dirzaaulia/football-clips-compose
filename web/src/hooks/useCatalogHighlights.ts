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
  isFetching: boolean;
  isPending: boolean;
  isLoadingMore: boolean;
  hasMore: boolean;
  error: unknown;
  loadMore: () => void;
  refresh: () => void;
}

const FALLBACK_COMPETITIONS = [
  'CL', 'PL', 'PD', 'SA', 'BL1', 'FL1', 'EL', 'ECL',
  'IDN', 'ACL', 'ACL2', 'SPL', 'MLS', 'JPN1',
  'FAC', 'EFL', 'CDR', 'DFB', 'CIT', 'DED', 'ENG2', 'BEL1', 'PPL', 'TUR1',
  'LIB', 'BRA', 'ARG', 'MEX1', 'SUD', 'REC',
  'UNL', 'WCQ_AFC', 'WCQ_UEFA', 'WCQ_CONMEBOL', 'CNL', 'WCQ_CONCACAF', 'WCQ_CAF', 'FRI',
];

export function useInfiniteHighlights(
  selectedComp = 'ALL',
  season = 2026,
  pageSize = 20,
  query = '',
  dynamicCompIds?: string[]
): UseInfiniteHighlightsResult {
  const queryResult = useInfiniteQuery({
    queryKey: ['highlights', selectedComp, season, pageSize, query, dynamicCompIds?.join(',')],
    queryFn: async ({ pageParam }) => {
      if ((selectedComp === 'ALL' || selectedComp === 'INTL') && !query && !pageParam) {
        const targetComps = selectedComp === 'INTL'
          ? ['UNL', 'CNL', 'WCQ_CONMEBOL', 'WCQ_AFC', 'WCQ_UEFA', 'WCQ_CONCACAF', 'WCQ_CAF', 'FRI']
          : (dynamicCompIds && dynamicCompIds.length > 0)
            ? dynamicCompIds.filter((id) => id && id !== 'ALL')
            : FALLBACK_COMPETITIONS;
        const grouped = await getTopHighlightsGroupedByCompetition(
          targetComps,
          season,
          20
        );
        const allMatches = Object.values(grouped)
          .flat()
          .sort((a, b) => new Date(b.utc_date || 0).getTime() - new Date(a.utc_date || 0).getTime());
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
      if ((selectedComp === 'ALL' || selectedComp === 'INTL') && !query) return undefined;
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
    isFetching: queryResult.isFetching,
    isPending: queryResult.isPending,
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
