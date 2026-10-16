import React from 'react';

export default function HighlightCardSkeleton() {
  return (
    <div className="relative bg-[#1E2227] border border-[#334155] rounded-2xl overflow-hidden shimmer-effect flex flex-col justify-between">
      {/* Thumbnail Aspect Video Skeleton */}
      <div className="w-full aspect-video bg-[#101418] relative flex items-center justify-center">
        {/* Top Badges */}
        <div className="absolute top-2.5 inset-x-2.5 flex items-center justify-between">
          <div className="w-24 h-4 rounded-full bg-[#252B32] shimmer-effect border border-[#334155]/60" />
          <div className="w-8 h-4 rounded-full bg-[#252B32] shimmer-effect border border-[#334155]/60" />
        </div>

        {/* Center Play Icon Placeholder */}
        <div className="w-12 h-12 rounded-full bg-[#252B32]/80 shimmer-effect border border-[#7098F8]/20" />
      </div>

      {/* Card Info Body Skeleton */}
      <div className="p-4 space-y-3">
        {/* Teams & Score Row */}
        <div className="flex items-center justify-between gap-2">
          {/* Home Team */}
          <div className="flex items-center gap-2 flex-1 min-w-0">
            <div className="w-7 h-7 rounded-full bg-[#252B32] shrink-0 shimmer-effect" />
            <div className="w-20 h-4 rounded-md bg-[#252B32] shimmer-effect" />
          </div>

          {/* Score Pill */}
          <div className="w-12 h-6 rounded-lg bg-[#252B32] shrink-0 shimmer-effect" />

          {/* Away Team */}
          <div className="flex items-center justify-end gap-2 flex-1 min-w-0">
            <div className="w-20 h-4 rounded-md bg-[#252B32] shimmer-effect" />
            <div className="w-7 h-7 rounded-full bg-[#252B32] shrink-0 shimmer-effect" />
          </div>
        </div>

        {/* Bottom Metadata & Action Bar */}
        <div className="pt-2 border-t border-[#334155]/60 flex items-center justify-between">
          <div className="w-24 h-3.5 rounded-md bg-[#252B32]/70 shimmer-effect" />
          <div className="w-16 h-3.5 rounded-md bg-[#7098F8]/15 shimmer-effect" />
        </div>
      </div>
    </div>
  );
}
