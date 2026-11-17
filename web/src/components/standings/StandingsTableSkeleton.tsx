'use client';

import React from 'react';

export default function StandingsTableSkeleton() {
  return (
    <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 overflow-hidden shadow-xl animate-pulse">
      {/* 9-Column Table Header Twin */}
      <div className="bg-[#121316]/80 border-b border-white/10 px-4 py-3 flex items-center justify-between gap-4">
        <div className="flex items-center gap-4 flex-1">
          <div className="w-8 h-3 rounded bg-white/15" />
          <div className="w-24 h-3 rounded bg-white/15" />
        </div>
        <div className="flex items-center gap-6">
          {[1, 2, 3, 4, 5, 6].map((col) => (
            <div key={col} className="w-6 h-3 rounded bg-white/10 hidden sm:block" />
          ))}
          <div className="w-10 h-3 rounded bg-[#DFFF00]/20" />
        </div>
      </div>

      {/* 12 Club Standings Rows */}
      <div className="divide-y divide-white/5">
        {[...Array(12)].map((_, i) => (
          <div
            key={i}
            className="px-4 py-3 flex items-center justify-between gap-4 bg-[#1A1C22]"
          >
            <div className="flex items-center gap-3 flex-1 min-w-0">
              <div
                className={`w-6 h-5 rounded text-center ${
                  i < 4 ? 'bg-[#DFFF00]/20' : 'bg-white/10'
                }`}
              />
              <div className="w-6 h-6 rounded-full bg-white/10 shrink-0" />
              <div className="w-36 sm:w-48 h-4 rounded bg-white/15" />
            </div>
            <div className="flex items-center gap-6">
              {[1, 2, 3, 4, 5, 6].map((col) => (
                <div key={col} className="w-6 h-4 rounded bg-white/10 hidden sm:block" />
              ))}
              <div className="w-10 h-5 rounded bg-white/20" />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
