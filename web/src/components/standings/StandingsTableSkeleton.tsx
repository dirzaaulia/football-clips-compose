'use client';

import React from 'react';

export default function StandingsTableSkeleton() {
  return (
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-4 space-y-3">
      {/* Table Header shimmer */}
      <div className="h-8 bg-white/5 rounded-lg animate-pulse" />
      {/* 8 Rows of shimmer */}
      {[...Array(8)].map((_, i) => (
        <div
          key={i}
          className="h-12 bg-[#121316] rounded-xl border border-white/5 flex items-center justify-between px-4 animate-pulse gap-4"
        >
          <div className="flex items-center gap-3">
            <div className="w-6 h-6 rounded-full bg-white/5" />
            <div className="w-32 h-4 rounded bg-white/5" />
          </div>
          <div className="flex items-center gap-4">
            <div className="w-8 h-4 rounded bg-white/5" />
            <div className="w-8 h-4 rounded bg-white/5" />
            <div className="w-12 h-4 rounded bg-white/5" />
          </div>
        </div>
      ))}
    </div>
  );
}
