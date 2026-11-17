import React from 'react';

export default function HeroBillboardSkeleton() {
  return (
    <section className="relative w-full overflow-hidden bg-[#0A0A0C] -mt-1 pt-4 pb-6 select-none animate-pulse">
      <div className="w-full px-4 sm:px-6 lg:px-8">
        <div className="relative w-full aspect-[21/9] min-h-[480px] lg:min-h-[560px] rounded-2xl overflow-hidden bg-[#121316] border border-white/5 flex flex-col justify-end p-6 sm:p-10 lg:p-12">
          {/* Top-Right Sponsored Badge Skeleton */}
          <div className="absolute top-6 right-6 hidden md:flex items-center gap-2">
            <div className="w-32 h-8 rounded-lg bg-white/10" />
            <div className="w-28 h-8 rounded-lg bg-[#DFFF00]/20" />
          </div>

          {/* Bottom Info Stack Skeleton */}
          <div className="relative z-10 max-w-3xl space-y-3">
            {/* Badges Row */}
            <div className="flex flex-wrap items-center gap-2">
              <div className="w-32 h-6 rounded bg-[#FF2E4D]/20" />
              <div className="w-40 h-6 rounded bg-white/10" />
              <div className="w-28 h-6 rounded bg-white/5" />
            </div>

            {/* Headline Title */}
            <div className="w-full max-w-xl h-12 bg-white/20 rounded-xl" />

            {/* Description */}
            <div className="w-full max-w-lg h-5 bg-white/10 rounded" />
            <div className="w-3/4 max-w-md h-5 bg-white/10 rounded" />

            {/* CTA Buttons */}
            <div className="flex items-center gap-3 pt-3">
              <div className="w-44 h-12 rounded-xl bg-[#DFFF00]/30" />
              <div className="w-28 h-12 rounded-xl bg-white/10" />
              <div className="w-36 h-12 rounded-xl bg-white/5" />
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
