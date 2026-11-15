'use client';

import React, { RefObject } from 'react';
import { Loader2 } from 'lucide-react';
import HighlightCardSkeleton from '@/components/feed/HighlightCardSkeleton';

interface InfiniteScrollSentinelProps {
  sentinelRef: RefObject<HTMLDivElement | null>;
  loadingMore: boolean;
  hasMore: boolean;
  hasMatches: boolean;
  initialLoading: boolean;
}

export default function InfiniteScrollSentinel({
  sentinelRef,
  loadingMore,
}: InfiniteScrollSentinelProps) {
  return (
    <>
      {loadingMore && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-5 my-6 animate-pulse">
          {[...Array(4)].map((_, i) => (
            <HighlightCardSkeleton key={`more-skeleton-${i}`} />
          ))}
        </div>
      )}

      <div ref={sentinelRef} className="py-6 flex flex-col items-center justify-center">
        {loadingMore && (
          <div className="flex items-center gap-2 text-xs text-[#DFFF00] font-tech font-bold">
            <Loader2 className="w-4 h-4 animate-spin text-[#DFFF00]" />
            <span>Fetching more highlights...</span>
          </div>
        )}
      </div>
    </>
  );
}
