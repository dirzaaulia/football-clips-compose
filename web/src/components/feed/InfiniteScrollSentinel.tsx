'use client';

import React, { RefObject } from 'react';
import { Loader2, CheckCircle2 } from 'lucide-react';
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
  hasMore,
  hasMatches,
  initialLoading,
}: InfiniteScrollSentinelProps) {
  return (
    <>
      {loadingMore && (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5 my-6 animate-pulse">
          {[...Array(3)].map((_, i) => (
            <HighlightCardSkeleton key={`more-skeleton-${i}`} />
          ))}
        </div>
      )}

      <div ref={sentinelRef} className="py-6 flex flex-col items-center justify-center">
        {loadingMore ? (
          <div className="flex items-center gap-2 text-xs text-[#7098F8] font-bold">
            <Loader2 className="w-4 h-4 animate-spin" />
            <span>Fetching more highlights...</span>
          </div>
        ) : !hasMore && hasMatches && !initialLoading ? (
          <div className="flex items-center gap-2 px-4 py-2 rounded-full bg-[#181C20] border border-[#334155]/60 text-xs text-[#94A3B8] font-medium shadow-sm">
            <CheckCircle2 className="w-3.5 h-3.5 text-[#4ADE80]" />
            <span>You&apos;re all caught up with recent highlights</span>
          </div>
        ) : null}
      </div>
    </>
  );
}
