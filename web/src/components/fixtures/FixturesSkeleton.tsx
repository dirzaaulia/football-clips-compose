'use client';

import React from 'react';

export default function FixturesSkeleton() {
  return (
    <div className="space-y-6">
      {/* Skeleton Competition Group */}
      {[...Array(2)].map((_, groupIdx) => (
        <div key={groupIdx} className="space-y-3">
          {/* Header shimmer */}
          <div className="flex items-center justify-between px-1">
            <div className="flex items-center gap-2">
              <div className="w-6 h-6 rounded-lg bg-white/5 animate-pulse" />
              <div className="h-5 w-48 rounded bg-white/5 animate-pulse" />
            </div>
            <div className="h-4 w-20 rounded-full bg-white/5 animate-pulse" />
          </div>

          {/* Cards shimmer */}
          <div className="space-y-2">
            {[...Array(3)].map((_, i) => (
              <div
                key={i}
                className="rounded-xl p-4 bg-[#1A1C22]/60 border border-white/5 flex flex-col md:flex-row md:items-center justify-between gap-4 animate-pulse"
              >
                <div className="w-20 h-10 rounded-lg bg-white/5" />
                <div className="flex-1 max-w-md mx-auto grid grid-cols-3 items-center gap-4">
                  <div className="h-4 bg-white/5 rounded justify-self-end w-24" />
                  <div className="h-8 bg-white/5 rounded-lg justify-self-center w-16" />
                  <div className="h-4 bg-white/5 rounded justify-self-start w-24" />
                </div>
                <div className="w-24 h-8 bg-white/5 rounded-lg" />
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
