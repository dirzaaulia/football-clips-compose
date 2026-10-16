import React from 'react';

export default function HeroCarouselSkeleton() {
  return (
    <section className="relative w-full my-4">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-4 items-stretch">
        {/* Main Featured Banner Skeleton */}
        <div className="lg:col-span-7 relative rounded-3xl overflow-hidden shimmer-effect border border-[#334155] min-h-[340px] sm:min-h-[380px] p-5 sm:p-7 flex flex-col justify-end">
          {/* Top Pill Badges */}
          <div className="absolute top-5 left-5 flex items-center gap-2">
            <div className="w-24 h-6 rounded-full bg-[#101418]/80 shimmer-effect border border-[#334155]" />
            <div className="w-16 h-6 rounded-full bg-[#101418]/80 shimmer-effect border border-[#334155]" />
          </div>

          {/* Content Skeleton */}
          <div className="relative z-10 space-y-3 max-w-lg">
            {/* Title Skeleton */}
            <div className="w-3/4 h-8 rounded-xl bg-[#252B32] shimmer-effect" />
            <div className="w-1/2 h-5 rounded-lg bg-[#252B32]/70 shimmer-effect" />

            {/* Score & Play Action Row */}
            <div className="flex items-center gap-3 pt-2">
              <div className="w-28 h-10 rounded-xl bg-[#7098F8]/20 shimmer-effect border border-[#7098F8]/30" />
              <div className="w-24 h-10 rounded-xl bg-[#1E2227] shimmer-effect border border-[#334155]" />
            </div>
          </div>
        </div>

        {/* Right Trending Skeletons (5 cols) */}
        <div className="lg:col-span-5 flex flex-col justify-between gap-3 min-h-[340px] sm:min-h-[380px]">
          {/* Header Skeleton */}
          <div className="flex items-center justify-between pb-2 border-b border-[#334155]/60">
            <div className="w-32 h-5 rounded-lg bg-[#1E2227] shimmer-effect" />
            <div className="w-16 h-4 rounded-md bg-[#1E2227] shimmer-effect" />
          </div>

          {/* Trending Cards Skeleton */}
          <div className="flex-1 flex flex-col gap-3 justify-center">
            {[1, 2].map((i) => (
              <div
                key={i}
                className="flex items-center gap-3 p-2.5 rounded-2xl bg-[#1E2227] border border-[#334155] shimmer-effect"
              >
                {/* Mini Thumb Skeleton */}
                <div className="w-[110px] sm:w-[120px] h-[64px] sm:h-[68px] rounded-xl bg-[#101418] shrink-0 shimmer-effect" />

                {/* Info Lines */}
                <div className="flex-1 space-y-2 py-1">
                  <div className="w-3/4 h-4 rounded-md bg-[#252B32] shimmer-effect" />
                  <div className="w-1/2 h-3 rounded-md bg-[#252B32]/70 shimmer-effect" />
                  <div className="w-16 h-4 rounded-md bg-[#7098F8]/15 shimmer-effect" />
                </div>
              </div>
            ))}
          </div>

          {/* Bottom Ad / Info Banner Skeleton */}
          <div className="h-14 rounded-2xl bg-[#1E2227]/60 border border-[#334155] shimmer-effect" />
        </div>
      </div>
    </section>
  );
}
