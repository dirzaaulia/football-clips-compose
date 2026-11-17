import React from 'react';

export default function ToolsLoading() {
  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6 select-none animate-pulse">
      {/* Header 1:1 Twin Skeleton */}
      <div className="space-y-2">
        <div className="flex items-center gap-2">
          <div className="w-48 h-5 rounded bg-[#DFFF00]/20 border border-[#DFFF00]/30" />
          <div className="w-32 h-4 rounded bg-white/10" />
        </div>
        <div className="w-80 sm:w-96 h-9 bg-white/15 rounded-lg" />
        <div className="w-full max-w-xl h-4 bg-white/10 rounded" />
      </div>

      {/* 5-Tab Tools Switcher Bar 1:1 Twin Skeleton */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 border-b border-white/10">
        {[1, 2, 3, 4, 5].map((i) => (
          <div
            key={i}
            className={`h-10 rounded-xl shrink-0 ${
              i === 1
                ? 'w-48 bg-[#DFFF00]/20 border border-[#DFFF00]/40'
                : 'w-40 bg-[#121316] border border-white/10'
            }`}
          />
        ))}
      </div>

      {/* Zone 14 Overload Engine 1:1 Twin Canvas Skeleton */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        {/* Left: Obsidian Pitch Canvas (8 cols) */}
        <div className="lg:col-span-8 bg-[#1A1C22] rounded-2xl border border-white/10 p-5 space-y-4">
          <div className="flex items-center justify-between">
            <div className="space-y-1.5">
              <div className="w-56 h-6 bg-white/15 rounded" />
              <div className="w-72 h-3.5 bg-white/10 rounded" />
            </div>
            <div className="w-28 h-6 bg-[#DFFF00]/15 rounded-full" />
          </div>
          <div className="w-full aspect-[16/10] rounded-xl bg-[#0B0C10] border border-white/10 p-4 flex flex-col justify-between">
            <div className="flex justify-between">
              <div className="w-28 h-6 rounded bg-white/5" />
              <div className="w-32 h-6 rounded bg-[#DFFF00]/10" />
            </div>
            <div className="self-center w-40 h-24 rounded border border-dashed border-[#DFFF00]/30 bg-[#DFFF00]/5" />
            <div className="flex justify-between">
              <div className="w-36 h-4 rounded bg-white/10" />
              <div className="w-24 h-4 rounded bg-white/10" />
            </div>
          </div>
        </div>

        {/* Right: Corridor Selector & Telemetry Readout (4 cols) */}
        <div className="lg:col-span-4 bg-[#1A1C22] rounded-2xl border border-white/10 p-5 space-y-4">
          <div className="w-44 h-5 bg-white/15 rounded" />
          <div className="space-y-2.5">
            {[1, 2, 3, 4].map((i) => (
              <div
                key={i}
                className={`h-14 rounded-xl p-3 flex items-center justify-between ${
                  i === 1
                    ? 'bg-[#121316] border border-[#DFFF00]/40'
                    : 'bg-[#121316] border border-white/5'
                }`}
              >
                <div className="space-y-1.5">
                  <div className="w-36 h-3.5 bg-white/15 rounded" />
                  <div className="w-24 h-2.5 bg-white/10 rounded" />
                </div>
                <div className="w-4 h-4 rounded bg-[#DFFF00]/20" />
              </div>
            ))}
          </div>
          <div className="grid grid-cols-2 gap-3 pt-2 border-t border-white/10">
            <div className="h-16 rounded-xl bg-[#121316] border border-white/5 p-3 space-y-1.5">
              <div className="w-16 h-2.5 bg-white/10 rounded" />
              <div className="w-20 h-5 bg-[#DFFF00]/20 rounded" />
            </div>
            <div className="h-16 rounded-xl bg-[#121316] border border-white/5 p-3 space-y-1.5">
              <div className="w-16 h-2.5 bg-white/10 rounded" />
              <div className="w-20 h-5 bg-white/15 rounded" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
