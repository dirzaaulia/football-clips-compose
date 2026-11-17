import React from 'react';

export default function ArticlesLoading() {
  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 2xl:px-10 py-6 select-none space-y-8 animate-pulse">
      {/* Header 1:1 Skeleton */}
      <header className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pb-4 border-b border-white/10">
        <div className="space-y-2">
          <div className="w-36 h-4 bg-[#DFFF00]/20 rounded" />
          <div className="w-80 h-8 bg-white/15 rounded-lg" />
        </div>
        <div className="w-64 h-4 bg-white/10 rounded" />
      </header>

      {/* AnalysisMatchBar 1:1 Twin Skeleton */}
      <div className="w-full bg-[#1A1C22] rounded-2xl border border-white/10 p-4 sm:p-5 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div className="space-y-2">
          <div className="w-40 h-3 bg-[#DFFF00]/20 rounded" />
          <div className="w-64 h-6 bg-white/15 rounded" />
        </div>
        <div className="flex flex-wrap items-center gap-2">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className={`h-8 w-28 rounded-xl ${
                i === 1
                  ? 'bg-[#DFFF00]/20 border border-[#DFFF00]/40'
                  : 'bg-[#121316] border border-white/10'
              }`}
            />
          ))}
        </div>
      </div>

      {/* Tactical Lab Bento: Zone 14 & Scout Observations 1:1 Twin Skeleton */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-[#1A1C22] rounded-2xl border border-white/10 p-5 space-y-4">
          <div className="flex items-center justify-between">
            <div className="w-56 h-5 bg-white/15 rounded" />
            <div className="w-24 h-5 bg-[#DFFF00]/15 rounded-full" />
          </div>
          <div className="w-full h-3.5 bg-white/10 rounded" />
          <div className="w-full h-44 bg-[#0B0C10] rounded-xl border border-white/10" />
        </div>

        <div className="bg-[#1A1C22] rounded-2xl border border-white/10 p-5 space-y-3">
          <div className="flex items-center justify-between">
            <div className="w-52 h-5 bg-white/15 rounded" />
            <div className="w-24 h-5 bg-white/10 rounded-full" />
          </div>
          {[1, 2, 3].map((i) => (
            <div
              key={i}
              className="p-3 rounded-xl bg-[#121316] border border-white/5 flex items-center justify-between gap-3"
            >
              <div className="flex items-center gap-3 flex-1">
                <div className="w-9 h-7 rounded-lg bg-[#DFFF00]/15 shrink-0" />
                <div className="space-y-1.5 flex-1">
                  <div className="w-48 h-3.5 bg-white/15 rounded" />
                  <div className="w-full h-3 bg-white/10 rounded" />
                </div>
              </div>
              <div className="w-16 h-5 bg-white/10 rounded-full shrink-0" />
            </div>
          ))}
        </div>
      </div>

      {/* Horizontal AdSense Banner Skeleton */}
      <div className="w-full h-24 bg-[#121316]/80 border border-white/10 rounded-2xl" />

      {/* Original Feature Articles Section 1:1 Skeleton */}
      <section className="space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <div className="w-64 h-6 bg-white/15 rounded" />
          <div className="w-32 h-4 bg-[#DFFF00]/20 rounded" />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className="bg-[#1A1C22] border border-white/10 rounded-2xl p-5 space-y-3"
            >
              <div className="flex items-center justify-between">
                <div className="w-24 h-3.5 bg-[#DFFF00]/20 rounded" />
                <div className="w-16 h-3.5 bg-white/10 rounded" />
              </div>
              <div className="w-3/4 h-5 bg-white/15 rounded" />
              <div className="w-full h-10 bg-white/10 rounded" />
              <div className="pt-3 border-t border-white/5 flex items-center justify-between">
                <div className="w-28 h-3.5 bg-white/10 rounded" />
                <div className="w-24 h-3.5 bg-[#DFFF00]/20 rounded" />
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* Verified European Sports Journalism 1:1 Skeleton */}
      <section className="space-y-4">
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <div className="w-64 h-6 bg-white/15 rounded" />
          <div className="w-40 h-4 bg-white/10 rounded" />
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div
              key={i}
              className="bg-[#121316] border border-white/10 rounded-xl p-4 space-y-3"
            >
              <div className="flex items-center justify-between">
                <div className="w-20 h-3 bg-[#DFFF00]/20 rounded" />
                <div className="w-16 h-3 bg-white/10 rounded" />
              </div>
              <div className="w-full h-4 bg-white/15 rounded" />
              <div className="w-3/4 h-4 bg-white/10 rounded" />
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}
