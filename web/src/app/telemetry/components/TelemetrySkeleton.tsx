import React from 'react';

export default function TelemetrySkeleton() {
  return (
    <div className="w-full max-w-[1600px] mx-auto px-4 sm:px-6 lg:px-8 py-6 select-none space-y-6 animate-pulse">
      {/* Header Skeleton */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 pb-4 border-b border-white/5">
        <div className="space-y-2">
          <div className="w-64 h-7 bg-white/20 rounded-lg" />
          <div className="w-96 h-4 bg-white/10 rounded" />
        </div>
        <div className="flex items-center gap-2">
          <div className="w-28 h-9 bg-white/10 rounded-xl" />
          <div className="w-24 h-9 bg-white/5 rounded-xl" />
        </div>
      </div>

      {/* Tabs Skeleton */}
      <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-1">
        {[1, 2, 3, 4, 5, 6].map((i) => (
          <div key={i} className="w-28 h-9 bg-white/10 rounded-xl shrink-0" />
        ))}
      </div>

      {/* Quota Gauge & Flow Graph Skeletons */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-1 h-48 bg-[#181C20]/80 rounded-2xl border border-white/5 p-5 space-y-4">
          <div className="w-36 h-4 bg-white/20 rounded" />
          <div className="w-full h-4 bg-white/10 rounded-full" />
          <div className="flex justify-between">
            <div className="w-20 h-3 bg-white/10 rounded" />
            <div className="w-16 h-3 bg-white/10 rounded" />
          </div>
        </div>

        <div className="lg:col-span-2 h-48 bg-[#181C20]/80 rounded-2xl border border-white/5 p-5 space-y-4">
          <div className="w-44 h-4 bg-white/20 rounded" />
          <div className="flex items-center justify-around pt-4">
            {[1, 2, 3, 4].map((i) => (
              <div key={i} className="w-20 h-16 bg-white/10 rounded-xl" />
            ))}
          </div>
        </div>
      </div>

      {/* Filter Bar Skeleton */}
      <div className="h-12 bg-[#121316] rounded-xl border border-white/5 flex items-center px-4 justify-between">
        <div className="w-64 h-6 bg-white/10 rounded" />
        <div className="w-32 h-6 bg-white/10 rounded" />
      </div>

      {/* Waterfall Cards Skeletons */}
      <div className="space-y-3">
        {[1, 2, 3, 4].map((i) => (
          <div
            key={i}
            className="p-4 rounded-xl bg-[#121316] border border-white/5 space-y-3"
          >
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-20 h-5 bg-white/20 rounded-md" />
                <div className="w-48 h-4 bg-white/10 rounded" />
              </div>
              <div className="w-24 h-6 bg-white/10 rounded-full" />
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 pt-1">
              <div className="h-8 bg-white/5 rounded-lg" />
              <div className="h-8 bg-white/5 rounded-lg" />
              <div className="h-8 bg-white/5 rounded-lg" />
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
