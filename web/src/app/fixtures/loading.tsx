import React from 'react';
import FixturesSkeleton from '@/components/fixtures/FixturesSkeleton';

export default function FixturesLoading() {
  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 select-none space-y-6 animate-pulse">
      {/* Top AdSense Leaderboard Skeleton */}
      <div className="w-full h-24 bg-[#121316]/80 border border-white/10 rounded-2xl" />

      {/* Header + SeasonSelector Skeleton */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="space-y-2">
          <div className="w-48 h-3.5 bg-[#DFFF00]/20 rounded" />
          <div className="w-72 h-8 bg-white/15 rounded-lg" />
          <div className="w-96 h-4 bg-white/10 rounded" />
        </div>
        <div className="w-36 h-9 bg-[#121316] border border-white/10 rounded-xl" />
      </div>

      {/* FixturesFilterBar 1:1 Skeleton */}
      <div className="flex flex-wrap items-center justify-between gap-3 pt-2 border-t border-white/10">
        <div className="flex items-center gap-1.5 bg-[#121316] p-1 rounded-xl border border-white/10">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className={`h-8 rounded-lg ${
                i === 1 ? 'w-28 bg-[#1A1C22] border border-[#DFFF00]/30' : 'w-20 bg-white/5'
              }`}
            />
          ))}
        </div>
        <div className="flex items-center gap-2">
          <div className="w-36 h-8 bg-[#121316] border border-white/10 rounded-xl" />
          <div className="w-28 h-8 bg-[#121316] border border-white/10 rounded-xl" />
        </div>
      </div>

      {/* 7-Day Date Carousel 1:1 Skeleton */}
      <div className="w-full p-2 bg-[#121316] border border-white/10 rounded-2xl flex items-center justify-between gap-2">
        <div className="w-9 h-12 rounded-xl bg-white/5 shrink-0" />
        <div className="flex-1 grid grid-cols-7 gap-2">
          {[1, 2, 3, 4, 5, 6, 7].map((i) => (
            <div
              key={i}
              className={`h-14 rounded-xl ${
                i === 4
                  ? 'bg-[#DFFF00]/20 border border-[#DFFF00]/40'
                  : 'bg-[#1A1C22] border border-white/5'
              }`}
            />
          ))}
        </div>
        <div className="w-9 h-12 rounded-xl bg-white/5 shrink-0" />
      </div>

      {/* Grouped Competition Rows Skeleton */}
      <FixturesSkeleton />

      {/* Bottom Mobile App Promo Banner Skeleton */}
      <div className="rounded-2xl bg-[#121316] border border-white/10 p-5 sm:p-6 flex flex-col md:flex-row items-center justify-between gap-4">
        <div className="space-y-2">
          <div className="w-40 h-3 bg-[#DFFF00]/20 rounded" />
          <div className="w-80 h-6 bg-white/15 rounded" />
          <div className="w-96 h-3.5 bg-white/10 rounded" />
        </div>
        <div className="w-36 h-10 bg-white/10 rounded-xl" />
      </div>
    </div>
  );
}
