import React from 'react';
import HeroCarouselSkeleton from '@/components/hero/HeroCarouselSkeleton';
import HighlightCardSkeleton from '@/components/feed/HighlightCardSkeleton';
import { Trophy } from 'lucide-react';

export default function Loading() {
  return (
    <div className="max-w-[1600px] w-full mx-auto px-3.5 sm:px-6 lg:px-8 2xl:px-10 py-4 space-y-8 animate-in fade-in duration-300">
      {/* Featured Banner Skeleton */}
      <HeroCarouselSkeleton />

      {/* Filter Bar Skeleton */}
      <section className="sticky top-0 z-30 bg-[#101418]/95 backdrop-blur-xl py-2 my-4 border-b border-white/5">
        <div className="flex items-center justify-between pb-2 border-b border-[#334155]/40 mb-3">
          <div className="flex items-center gap-2">
            <Trophy className="w-4 h-4 text-[#7098F8]" />
            <div className="w-36 h-5 rounded-md bg-[#252B32] shimmer-effect" />
          </div>
          <div className="w-24 h-6 rounded-xl bg-[#252B32] shimmer-effect" />
        </div>

        {/* Competition Pills Skeleton */}
        <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-2">
          {Array.from({ length: 8 }).map((_, i) => (
            <div
              key={`comp-pill-${i}`}
              className="w-28 h-9 rounded-full bg-[#1E2227] border border-[#334155]/60 shrink-0 shimmer-effect"
            />
          ))}
        </div>
      </section>

      {/* Highlights Grid Skeleton */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 2xl:grid-cols-4 gap-4 sm:gap-5 my-6">
        {Array.from({ length: 8 }).map((_, i) => (
          <HighlightCardSkeleton key={`page-skeleton-${i}`} />
        ))}
      </div>
    </div>
  );
}
