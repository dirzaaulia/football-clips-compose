import React from 'react';

function ClubStoreCardSkeleton() {
  return (
    <div className="rounded-2xl bg-[#1A1C22] border border-white/10 p-4 flex flex-col justify-between space-y-3">
      <div className="relative aspect-[4/3] w-full rounded-xl bg-[#121316] border border-white/5 p-3 flex flex-col justify-between">
        <div className="flex items-center justify-between">
          <div className="w-7 h-7 rounded-full bg-white/10" />
          <div className="w-20 h-4 rounded-full bg-[#DFFF00]/20" />
        </div>
        <div className="self-center w-20 h-20 rounded-xl bg-white/5" />
      </div>
      <div className="space-y-2">
        <div className="w-3/4 h-4 bg-white/15 rounded" />
        <div className="w-1/2 h-3 bg-white/10 rounded" />
        <div className="flex items-center gap-1.5 pt-1">
          {[1, 2, 3, 4, 5].map((s) => (
            <div key={s} className="w-7 h-5 rounded bg-[#121316] border border-white/10" />
          ))}
        </div>
      </div>
      <div className="w-full h-9 rounded-xl bg-[#DFFF00]/20 border border-[#DFFF00]/30" />
    </div>
  );
}

export default function StoreLoading() {
  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 select-none space-y-6 animate-pulse">
      {/* Hero Banner 1:1 Twin Skeleton */}
      <div className="rounded-3xl p-6 sm:p-8 bg-[#1A1C22] border border-white/10 space-y-3">
        <div className="flex flex-wrap items-center gap-2">
          <div className="w-44 h-5 bg-[#DFFF00]/20 rounded-full" />
          <div className="w-36 h-5 bg-white/10 rounded-full" />
        </div>
        <div className="w-72 sm:w-96 h-9 bg-white/15 rounded-lg" />
        <div className="w-full max-w-xl h-4 bg-white/10 rounded" />
      </div>

      {/* Category Filter Tabs 1:1 Twin Skeleton */}
      <div className="flex items-center gap-2 overflow-x-auto pb-1">
        {[1, 2, 3, 4, 5].map((i) => (
          <div
            key={i}
            className={`h-8 rounded-xl shrink-0 ${
              i === 1 ? 'w-28 bg-[#DFFF00]/25' : 'w-32 bg-[#1A1C22] border border-white/10'
            }`}
          />
        ))}
      </div>

      {/* Followed Clubs Shelf 1:1 Twin Skeleton */}
      <div className="space-y-3 bg-[#121316] border border-white/10 rounded-2xl p-5">
        <div className="flex items-center justify-between pb-2 border-b border-white/5">
          <div className="w-48 h-5 bg-white/15 rounded" />
          <div className="w-16 h-4 bg-[#DFFF00]/20 rounded" />
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {[1, 2, 3, 4].map((i) => (
            <ClubStoreCardSkeleton key={i} />
          ))}
        </div>
      </div>

      {/* Quick League Filter Chips 1:1 Twin Skeleton */}
      <div className="flex items-center gap-2 overflow-x-auto py-1">
        {[1, 2, 3, 4, 5, 6, 7].map((i) => (
          <div
            key={i}
            className={`h-8 rounded-xl shrink-0 ${
              i === 1 ? 'w-28 bg-[#DFFF00]/25' : 'w-28 bg-[#121316] border border-white/5'
            }`}
          />
        ))}
      </div>

      {/* Search & League Dropdown 1:1 Twin Skeleton */}
      <div className="flex flex-col sm:flex-row items-center gap-3">
        <div className="flex-1 w-full h-10 bg-[#1A1C22] rounded-xl border border-white/10" />
        <div className="w-full sm:w-44 h-10 bg-[#121316] rounded-xl border border-white/10" />
      </div>

      {/* All Clubs Grid 1:1 Twin Skeleton */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
        {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
          <ClubStoreCardSkeleton key={i} />
        ))}
      </div>
    </div>
  );
}
