'use client';

import React from 'react';

function FixtureItemRowSkeleton() {
  return (
    <div className="rounded-xl p-3.5 sm:p-4 bg-[#1A1C22] border border-white/5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 animate-pulse">
      {/* Left: Status Badge + Venue */}
      <div className="flex items-center gap-2.5 min-w-[140px]">
        <div className="w-12 h-6 rounded-lg bg-[#121316] border border-white/10 shrink-0" />
        <div className="w-24 h-3 rounded bg-white/10 hidden lg:block" />
      </div>

      {/* Center: Home Team — Score Box — Away Team */}
      <div className="flex-1 flex items-center justify-center gap-3 sm:gap-5">
        <div className="flex-1 flex items-center justify-end gap-2.5">
          <div className="w-24 sm:w-32 h-4 rounded bg-white/15" />
          <div className="w-7 h-7 rounded-full bg-white/10 shrink-0" />
        </div>

        <div className="px-3 py-1.5 rounded-lg bg-[#121316] border border-white/10 min-w-[68px] h-8 flex items-center justify-center gap-2 shrink-0">
          <div className="w-3 h-4 rounded bg-white/15" />
          <div className="w-1.5 h-3 rounded bg-white/10" />
          <div className="w-3 h-4 rounded bg-white/15" />
        </div>

        <div className="flex-1 flex items-center justify-start gap-2.5">
          <div className="w-7 h-7 rounded-full bg-white/10 shrink-0" />
          <div className="w-24 sm:w-32 h-4 rounded bg-white/15" />
        </div>
      </div>

      {/* Right: Watch Highlight / Boxscore Actions */}
      <div className="flex items-center justify-end gap-2 min-w-[140px]">
        <div className="w-24 h-7 rounded-lg bg-[#DFFF00]/15 border border-[#DFFF00]/20" />
        <div className="w-20 h-7 rounded-lg bg-[#121316] border border-white/10" />
      </div>
    </div>
  );
}

export default function FixturesSkeleton() {
  return (
    <div className="space-y-6">
      {[1, 2].map((groupIdx) => (
        <div key={groupIdx} className="space-y-3">
          {/* Competition Group Header */}
          <div className="flex items-center justify-between px-1 animate-pulse">
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 rounded-lg bg-white/10 border border-white/10" />
              <div className="h-5 w-48 rounded bg-white/15" />
            </div>
            <div className="h-6 w-24 rounded-full bg-[#121316] border border-white/10" />
          </div>

          {/* 3 FixtureItemCard Twin Rows */}
          <div className="grid grid-cols-1 gap-2.5">
            {[1, 2, 3].map((i) => (
              <FixtureItemRowSkeleton key={i} />
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
