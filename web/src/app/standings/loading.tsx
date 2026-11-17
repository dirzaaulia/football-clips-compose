import React from 'react';
import StandingsTableSkeleton from '@/components/standings/StandingsTableSkeleton';

export default function StandingsLoading() {
  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 select-none space-y-6 animate-pulse">
      {/* Top AdSense Leaderboard Skeleton */}
      <div className="w-full h-24 bg-[#121316]/80 border border-white/10 rounded-2xl" />

      {/* Header + SeasonSelector Skeleton */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="space-y-2">
          <div className="w-48 h-3.5 bg-[#DFFF00]/20 rounded" />
          <div className="w-80 h-8 bg-white/15 rounded-lg" />
          <div className="w-96 h-4 bg-white/10 rounded" />
        </div>
        <div className="w-36 h-9 bg-[#121316] border border-white/10 rounded-xl" />
      </div>

      {/* 4-Tab Format Switcher + Competition Pills Skeleton */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 pt-2 border-t border-white/10">
        <div className="flex items-center gap-1.5 bg-[#121316] p-1 rounded-xl border border-white/10">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className={`h-8 rounded-lg ${
                i === 1
                  ? 'w-36 bg-[#1A1C22] border border-[#DFFF00]/30'
                  : 'w-28 bg-white/5'
              }`}
            />
          ))}
        </div>
        <div className="flex items-center gap-2 overflow-hidden">
          {[1, 2, 3, 4, 5].map((i) => (
            <div
              key={i}
              className="w-28 h-8 bg-[#121316] border border-white/10 rounded-xl shrink-0"
            />
          ))}
        </div>
      </div>

      {/* Swiss / Qualification Legend Cards Twin Skeleton */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
        {[1, 2, 3].map((i) => (
          <div
            key={i}
            className="bg-[#1A1C22] border border-white/10 rounded-xl p-3.5 flex items-start gap-3"
          >
            <div className="w-2.5 h-2.5 rounded-full bg-[#DFFF00]/40 mt-1 shrink-0" />
            <div className="space-y-1.5 flex-1">
              <div className="w-36 h-3.5 bg-white/15 rounded" />
              <div className="w-full h-3 bg-white/10 rounded" />
            </div>
          </div>
        ))}
      </div>

      {/* Standings Table 1:1 Twin Skeleton */}
      <StandingsTableSkeleton />
    </div>
  );
}
