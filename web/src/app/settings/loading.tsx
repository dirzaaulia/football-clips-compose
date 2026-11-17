import React from 'react';

function SettingsHubSkeleton() {
  return (
    <div className="space-y-4">
      {/* Account Profile Card Skeleton */}
      <div className="p-5 sm:p-6 rounded-2xl bg-[#1A1C22] border border-white/10 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="w-14 h-14 rounded-2xl bg-[#121316] border border-[#DFFF00]/20 shrink-0" />
          <div className="space-y-2">
            <div className="flex items-center gap-2">
              <div className="w-36 h-6 bg-white/15 rounded-lg" />
              <div className="w-24 h-4 bg-[#DFFF00]/20 rounded-full" />
            </div>
            <div className="w-56 h-3.5 bg-white/10 rounded" />
          </div>
        </div>
        <div className="flex items-center gap-2.5">
          <div className="w-28 h-9 bg-[#DFFF00]/15 rounded-xl" />
          <div className="w-24 h-9 bg-white/10 rounded-xl" />
        </div>
      </div>

      {/* 2x2 Primary Preferences Grid Skeleton */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5">
        {[1, 2, 3, 4].map((i) => (
          <div
            key={i}
            className="p-4 rounded-2xl bg-[#1A1C22] border border-white/10 flex items-center justify-between gap-3"
          >
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-xl bg-white/5 border border-white/10 shrink-0" />
              <div className="space-y-1.5">
                <div className="w-28 h-4 bg-white/15 rounded" />
                <div className="w-40 h-3 bg-white/10 rounded" />
              </div>
            </div>
            <div className="w-12 h-6 bg-white/10 rounded-full shrink-0" />
          </div>
        ))}
      </div>

      {/* Merchandise Recommendations Card Skeleton */}
      <div className="p-4 sm:p-5 rounded-2xl bg-[#1A1C22] border border-white/10 flex items-center justify-between gap-4">
        <div className="flex items-center gap-3.5">
          <div className="w-10 h-10 rounded-xl bg-[#DFFF00]/10 border border-[#DFFF00]/20 shrink-0" />
          <div className="space-y-1.5">
            <div className="w-48 h-4 bg-white/15 rounded" />
            <div className="w-60 h-3 bg-white/10 rounded" />
          </div>
        </div>
        <div className="w-20 h-7 bg-[#DFFF00]/15 rounded-full shrink-0" />
      </div>

      {/* Match & Highlight Notifications Card Skeleton */}
      <div className="p-4 sm:p-5 rounded-2xl bg-[#1A1C22] border border-white/10 space-y-4">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-[#DFFF00]/10 border border-[#DFFF00]/20 shrink-0" />
            <div className="space-y-1.5">
              <div className="w-52 h-4 bg-white/15 rounded" />
              <div className="w-64 h-3 bg-white/10 rounded" />
            </div>
          </div>
          <div className="hidden sm:block w-20 h-5 bg-white/5 rounded-full" />
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 pt-2 border-t border-white/10">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className="p-3.5 rounded-xl bg-[#121316] border border-white/10 flex items-center justify-between"
            >
              <div className="space-y-1.5">
                <div className="w-28 h-3.5 bg-white/15 rounded" />
                <div className="w-40 h-2.5 bg-white/10 rounded" />
              </div>
              <div className="w-9 h-5 bg-white/10 rounded-full shrink-0" />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default function SettingsLoading() {
  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 select-none space-y-6 animate-pulse">
      {/* InfoHeroBanner 1:1 Twin Skeleton */}
      <div className="rounded-2xl border border-white/10 bg-[#1A1C22] p-6 sm:p-8 flex flex-col sm:flex-row sm:items-center justify-between gap-6">
        <div className="space-y-2.5 max-w-2xl">
          <div className="w-52 h-5 rounded-full bg-[#DFFF00]/20" />
          <div className="w-72 sm:w-96 h-8 sm:h-10 rounded-lg bg-white/15" />
          <div className="w-full max-w-xl h-4 rounded bg-white/10" />
        </div>
        <div className="hidden sm:block w-16 h-16 rounded-2xl bg-[#121316] border border-[#DFFF00]/20 shrink-0" />
      </div>

      {/* Main 12-Col Grid Skeleton (7 Left / 5 Right) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
        <div className="lg:col-span-7">
          <SettingsHubSkeleton />
        </div>

        <div className="lg:col-span-5 space-y-4">
          {/* Support Developer Expanded Card Skeleton */}
          <div className="rounded-2xl bg-[#1A1C22] border border-[#DFFF00]/30 p-5 space-y-3.5">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-white/5 border border-white/10" />
                <div className="w-36 h-4 bg-white/15 rounded" />
              </div>
              <div className="w-4 h-4 bg-white/10 rounded" />
            </div>
            <div className="w-full h-3.5 bg-white/10 rounded" />
            <div className="space-y-2.5">
              {[1, 2, 3].map((i) => (
                <div
                  key={i}
                  className="p-3.5 rounded-xl bg-[#121316] border border-white/10 flex items-center justify-between"
                >
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-lg bg-white/5" />
                    <div className="space-y-1.5">
                      <div className="w-28 h-3.5 bg-white/15 rounded" />
                      <div className="w-24 h-2.5 bg-white/10 rounded" />
                    </div>
                  </div>
                  <div className="w-4 h-4 bg-white/10 rounded" />
                </div>
              ))}
            </div>
          </div>

          {/* Data Source & Collapsible Legal Cards Skeleton */}
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className="rounded-2xl bg-[#1A1C22] border border-white/10 p-4 sm:p-5 flex items-center justify-between"
            >
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-white/5 border border-white/10" />
                <div className="w-40 h-4 bg-white/15 rounded" />
              </div>
              <div className="w-4 h-4 bg-white/10 rounded" />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
