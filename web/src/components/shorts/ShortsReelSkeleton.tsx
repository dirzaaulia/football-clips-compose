import React from 'react';

export default function ShortsReelSkeleton() {
  return (
    <div className="h-full w-full flex items-center justify-center p-2 sm:p-4 animate-pulse">
      <div className="flex items-end justify-center gap-3 sm:gap-4 h-full max-h-[820px]">
        {/* Outer 9:16 Video Chassis */}
        <div className="relative aspect-[9/16] h-full max-h-[820px] w-auto rounded-3xl overflow-hidden bg-[#101418] border border-white/10 shadow-[0_0_50px_rgba(0,0,0,0.8)] select-none shrink-0 flex flex-col justify-between p-5">
          {/* Ambient Rim Lighting */}
          <div className="absolute -inset-0.5 bg-gradient-to-b from-[#7098F8]/15 via-transparent to-[#7098F8]/10 rounded-3xl pointer-events-none -z-10 blur-sm" />

          {/* Shimmer Wave Background */}
          <div className="absolute inset-0 bg-gradient-to-b from-slate-900/60 via-slate-800/40 to-slate-950/80" />

          {/* Top-Right Mute Pill Skeleton */}
          <div className="self-end z-10 w-9 h-9 rounded-full bg-white/10" />

          {/* Bottom Info and Mobile Action Rail Skeleton */}
          <div className="z-10 flex items-end justify-between gap-3 w-full">
            <div className="space-y-2.5 max-w-[70%]">
              {/* Channel Pill Skeleton */}
              <div className="flex items-center gap-2">
                <div className="w-7 h-7 rounded-full bg-white/15 shrink-0" />
                <div className="h-3.5 w-24 rounded-full bg-white/15" />
              </div>
              {/* Title Lines Skeleton */}
              <div className="h-4 w-48 rounded bg-white/20" />
              <div className="h-3 w-32 rounded bg-white/10" />
            </div>

            {/* Mobile Rail Skeleton */}
            <div className="flex md:hidden flex-col items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-white/10" />
              <div className="w-10 h-10 rounded-full bg-white/10" />
              <div className="w-10 h-10 rounded-full bg-white/10" />
            </div>
          </div>
        </div>

        {/* Desktop Action Rail Skeleton (Outside Chassis) */}
        <div className="hidden md:flex flex-col items-center gap-4 pb-4">
          <div className="flex flex-col items-center gap-1.5">
            <div className="w-11 h-11 rounded-full bg-white/10" />
            <div className="h-2 w-5 rounded bg-white/10" />
          </div>
          <div className="flex flex-col items-center gap-1.5">
            <div className="w-11 h-11 rounded-full bg-white/10" />
            <div className="h-2 w-7 rounded bg-white/10" />
          </div>
          <div className="flex flex-col items-center gap-1.5">
            <div className="w-11 h-11 rounded-full bg-white/10" />
            <div className="h-2 w-7 rounded bg-white/10" />
          </div>
          <div className="flex flex-col items-center gap-1.5">
            <div className="w-11 h-11 rounded-full bg-white/10" />
            <div className="h-2 w-8 rounded bg-white/10" />
          </div>
        </div>
      </div>
    </div>
  );
}
