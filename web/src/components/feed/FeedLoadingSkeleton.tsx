'use client';

import React from 'react';
import HighlightCardSkeleton from '@/components/feed/HighlightCardSkeleton';

function CarouselAdSkeleton() {
  return (
    <div className="w-[280px] sm:w-[320px] shrink-0 h-[320px] rounded-2xl bg-[#121316] border border-white/10 p-4 flex flex-col justify-between">
      <div className="flex items-center justify-between pb-2 border-b border-white/10">
        <span className="text-[10px] font-tech font-bold uppercase tracking-wider text-white/50">
          Advertisement
        </span>
      </div>
      <div className="flex-1 rounded-xl bg-white/5 my-2 flex items-center justify-center">
        <div className="h-6 w-24 rounded bg-white/10" />
      </div>
      <div className="pt-2 border-t border-white/5 flex items-center justify-end">
        <span className="text-[10px] font-tech text-white/40">Google AdSense</span>
      </div>
    </div>
  );
}

function FeedAdSkeleton() {
  return (
    <div className="min-h-[310px] w-full rounded-2xl bg-[#121316] border border-white/10 p-4 flex flex-col justify-between">
      <div className="flex items-center justify-between pb-2 border-b border-white/10">
        <span className="text-[10px] font-tech font-bold uppercase tracking-wider text-white/50">
          Advertisement
        </span>
      </div>
      <div className="flex-1 rounded-xl bg-white/5 my-2 flex items-center justify-center">
        <div className="h-6 w-24 rounded bg-white/10" />
      </div>
      <div className="pt-2 border-t border-white/5 flex items-center justify-end">
        <span className="text-[10px] font-tech text-white/40">Google AdSense</span>
      </div>
    </div>
  );
}

function ShortsRailSkeleton() {
  return (
    <section className="w-full py-6 space-y-4 select-none">
      <div className="w-full flex items-end justify-between px-1">
        <div className="space-y-1.5">
          <div className="flex items-center gap-2">
            <span className="w-5 h-5 rounded bg-[#DFFF00]/30" />
            <div className="h-6 w-48 rounded bg-white/20" />
          </div>
          <div className="h-3.5 w-72 rounded bg-white/10 hidden sm:block" />
        </div>
        <div className="h-4 w-32 rounded bg-white/10 shrink-0" />
      </div>

      <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-6 gap-3 sm:gap-4">
        {Array.from({ length: 6 }).map((_, i) => (
          <div
            key={`shorts-skel-${i}`}
            className="aspect-[9/16] rounded-xl overflow-hidden bg-[#121316] border border-white/10 p-3 flex flex-col justify-end relative"
          >
            <div className="absolute inset-0 bg-gradient-to-t from-[#0A0A0C] via-transparent to-transparent" />
            <div className="relative z-10 space-y-1.5">
              <div className="h-4 w-16 rounded bg-[#DFFF00]/25 mb-1" />
              <div className="h-3 w-full rounded bg-white/15" />
              <div className="h-3 w-3/4 rounded bg-white/15" />
              <div className="h-2.5 w-12 rounded bg-white/10 mt-1" />
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

export default function FeedLoadingSkeleton({ isGrouped = true }: { isGrouped?: boolean }) {
  if (!isGrouped) {
    return (
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4 sm:gap-5 my-6 animate-pulse select-none">
        {Array.from({ length: 8 }).map((_, i) => (
          <React.Fragment key={`grid-skel-${i}`}>
            <HighlightCardSkeleton />
            {i === 4 && <FeedAdSkeleton />}
          </React.Fragment>
        ))}
      </div>
    );
  }

  return (
    <div className="space-y-8 my-6 select-none animate-pulse">
      {/* 1. Recent Highlights Section Skeleton */}
      <section className="space-y-3.5 pb-4 border-b border-white/10">
        <div className="flex items-center justify-between pb-1.5 border-b border-white/10">
          <div className="flex items-center gap-2.5">
            <span className="w-1 h-5 rounded-full bg-[#DFFF00]" />
            <div className="h-5 w-44 rounded bg-white/20" />
            <div className="h-4 w-24 rounded-full bg-[#DFFF00]/15 border border-[#DFFF00]/30 hidden sm:block" />
          </div>
          <div className="h-4 w-20 rounded bg-white/10" />
        </div>

        <div className="flex gap-4 sm:gap-5 overflow-x-hidden py-2 px-0.5">
          {Array.from({ length: 4 }).map((_, i) => (
            <React.Fragment key={`recent-skel-${i}`}>
              <div className="w-[280px] sm:w-[320px] shrink-0">
                <HighlightCardSkeleton />
              </div>
              {i === 2 && <CarouselAdSkeleton />}
            </React.Fragment>
          ))}
        </div>
      </section>

      {/* 2. First Competition Rail Skeleton (e.g. UCL) */}
      <section className="w-full py-4 space-y-3">
        <div className="w-full flex items-end justify-between px-1">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-[#121316] border border-[#DFFF00]/30 shrink-0" />
            <div className="space-y-1.5">
              <div className="flex items-center gap-2">
                <div className="h-5 w-48 rounded bg-white/20" />
                <div className="h-4 w-14 rounded bg-[#DFFF00]/10 border border-[#DFFF00]/20" />
              </div>
              <div className="h-3 w-40 rounded bg-white/10" />
            </div>
          </div>
          <div className="h-8 w-28 rounded-lg bg-[#121316] border border-white/10" />
        </div>

        <div className="flex gap-4 sm:gap-5 overflow-x-hidden py-2 px-1">
          {Array.from({ length: 4 }).map((_, i) => (
            <React.Fragment key={`rail-0-card-${i}`}>
              <div className="w-[280px] sm:w-[320px] md:w-[360px] shrink-0">
                <HighlightCardSkeleton />
              </div>
              {i === 3 && <CarouselAdSkeleton />}
            </React.Fragment>
          ))}
        </div>
      </section>

      {/* 3. 9:16 Shorts Rail Skeleton (1:1 with HomeShortsRail) */}
      <ShortsRailSkeleton />

      {/* 4. Second Competition Rail Skeleton */}
      <section className="w-full py-4 space-y-3">
        <div className="w-full flex items-end justify-between px-1">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-lg bg-[#121316] border border-[#DFFF00]/30 shrink-0" />
            <div className="space-y-1.5">
              <div className="flex items-center gap-2">
                <div className="h-5 w-48 rounded bg-white/20" />
                <div className="h-4 w-14 rounded bg-[#DFFF00]/10 border border-[#DFFF00]/20" />
              </div>
              <div className="h-3 w-40 rounded bg-white/10" />
            </div>
          </div>
          <div className="h-8 w-28 rounded-lg bg-[#121316] border border-white/10" />
        </div>

        <div className="flex gap-4 sm:gap-5 overflow-x-hidden py-2 px-1">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={`rail-1-card-${i}`} className="w-[280px] sm:w-[320px] md:w-[360px] shrink-0">
              <HighlightCardSkeleton />
            </div>
          ))}
        </div>
      </section>

      {/* 5. Interleaved AdSense Banner Skeleton */}
      <div className="h-24 w-full rounded-2xl bg-[#121316] border border-white/10 my-4" />
    </div>
  );
}
