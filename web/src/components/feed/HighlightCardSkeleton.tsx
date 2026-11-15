'use client';

import React from 'react';

export default function HighlightCardSkeleton({ className = '' }: { className?: string }) {
  return (
    <div
      className={`group relative rounded-2xl bg-[#121316] border border-white/10 flex flex-col overflow-hidden select-none animate-pulse ${className}`}
    >
      {/* 16:9 Cinema Thumbnail Container (Exact match to HighlightRailCard) */}
      <div className="relative w-full pt-[56.25%] bg-[#0A0A0C] overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-r from-white/5 via-white/10 to-white/5 animate-pulse" />

        {/* Top-Left: Duration Pill */}
        <div className="absolute top-2.5 left-2.5 h-5 w-16 rounded-full bg-black/80 border border-white/10" />

        {/* Top-Right: Competition Pill */}
        <div className="absolute top-2.5 right-2.5 h-5 w-20 rounded-full bg-black/80 border border-white/10" />

        {/* Center: Play Trigger */}
        <div className="absolute inset-0 flex items-center justify-center">
          <div className="w-12 h-12 rounded-full bg-white/5 border border-white/10 flex items-center justify-center">
            <div className="w-4 h-4 rounded-full bg-white/15" />
          </div>
        </div>
      </div>

      {/* Card Body: Home Crest + Name | Scoreboard Pill | Away Name + Crest */}
      <div className="p-3.5 flex flex-col justify-between flex-1 gap-2.5">
        <div className="flex items-center justify-between gap-2">
          {/* Home Team */}
          <div className="flex items-center gap-2 flex-1 min-w-0">
            <div className="w-6 h-6 rounded-full bg-white/15 shrink-0" />
            <div className="h-3.5 w-20 rounded bg-white/20" />
          </div>

          {/* Stadium Scoreboard Pill */}
          <div className="shrink-0 px-2.5 py-1 rounded-lg bg-[#1A1C22] border border-white/10 flex items-center justify-center">
            <div className="h-3.5 w-12 rounded bg-[#DFFF00]/20" />
          </div>

          {/* Away Team */}
          <div className="flex items-center justify-end gap-2 flex-1 min-w-0 text-right">
            <div className="h-3.5 w-20 rounded bg-white/20" />
            <div className="w-6 h-6 rounded-full bg-white/15 shrink-0" />
          </div>
        </div>

        {/* Footer: Date & Full Highlights Badge */}
        <div className="flex items-center justify-between pt-2 border-t border-white/5">
          <div className="h-3 w-14 rounded bg-white/10" />
          <div className="h-3 w-20 rounded bg-[#38BDF8]/20" />
        </div>
      </div>
    </div>
  );
}
