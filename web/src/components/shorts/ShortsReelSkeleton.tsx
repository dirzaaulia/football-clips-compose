import React from 'react';

export default function ShortsReelSkeleton() {
  return (
    <div className="w-full max-w-7xl mx-auto px-4 flex justify-center items-center gap-8 h-full animate-pulse select-none">
      {/* Left ShortsChannelRail 1:1 Twin Skeleton */}
      <aside className="hidden lg:flex flex-col gap-3 w-56 shrink-0">
        <div className="bg-[#121316] border border-white/10 rounded-2xl p-3 space-y-2">
          {[1, 2, 3, 4].map((i) => (
            <div
              key={i}
              className={`h-10 rounded-xl px-3 flex items-center gap-3 ${
                i === 1
                  ? 'bg-[#1A1C22] border border-[#DFFF00]/30'
                  : 'bg-transparent'
              }`}
            >
              <div className="w-4 h-4 rounded bg-[#DFFF00]/30" />
              <div className="w-24 h-3.5 rounded bg-white/15" />
            </div>
          ))}
        </div>
        <div className="bg-[#121316] border border-white/10 rounded-2xl p-3 space-y-2.5">
          <div className="w-28 h-3 rounded bg-white/15" />
          {[1, 2, 3, 4, 5].map((i) => (
            <div key={i} className="flex items-center gap-2.5 py-1">
              <div className="w-6 h-6 rounded-full bg-white/10 shrink-0" />
              <div className="w-28 h-3 rounded bg-white/10" />
            </div>
          ))}
        </div>
      </aside>

      {/* Center 9:16 Video Chassis + Action Rail + Steppers */}
      <div className="relative flex items-center justify-center gap-4 h-full py-2">
        <div className="relative w-[360px] sm:w-[410px] h-[calc(100vh-5rem)] max-h-[800px] rounded-2xl overflow-hidden bg-[#121316] border border-white/10 shadow-2xl flex flex-col justify-between p-5">
          <div className="self-end w-9 h-9 rounded-full bg-white/10" />
          <div className="space-y-3">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-full bg-[#1A1C22] border border-[#DFFF00]/30" />
              <div className="space-y-1.5">
                <div className="w-28 h-3.5 rounded bg-white/20" />
                <div className="w-20 h-2.5 rounded bg-[#DFFF00]/20" />
              </div>
            </div>
            <div className="w-full h-4 rounded bg-white/15" />
            <div className="w-3/4 h-4 rounded bg-white/10" />
          </div>
        </div>

        {/* Desktop Action Rail Skeleton */}
        <div className="hidden md:flex flex-col items-center justify-end gap-4 pb-3">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="flex flex-col items-center gap-1.5">
              <div className="w-11 h-11 rounded-full bg-[#1A1C22] border border-white/10" />
              <div className="w-8 h-2.5 rounded bg-white/10" />
            </div>
          ))}
        </div>

        {/* Up/Down Steppers Skeleton */}
        <div className="hidden md:flex flex-col gap-2">
          <div className="w-10 h-10 rounded-full bg-[#1A1C22] border border-white/10" />
          <div className="w-10 h-10 rounded-full bg-[#1A1C22] border border-white/10" />
        </div>
      </div>

      {/* Right Up Next Queue 1:1 Twin Skeleton */}
      <aside className="hidden xl:flex flex-col w-72 shrink-0 gap-3">
        <div className="bg-[#121316] border border-white/10 rounded-2xl p-4 space-y-3">
          <div className="flex items-center justify-between pb-2.5 border-b border-white/10">
            <div className="w-32 h-4 rounded bg-white/15" />
            <div className="w-12 h-4 rounded-full bg-[#DFFF00]/20" />
          </div>
          <div className="space-y-2.5">
            {[1, 2, 3].map((i) => (
              <div
                key={i}
                className="flex items-center gap-3 p-2 rounded-xl bg-[#1A1C22] border border-white/5"
              >
                <div className="w-12 h-20 rounded-lg bg-[#0A0A0C] shrink-0" />
                <div className="space-y-1.5 flex-1">
                  <div className="w-20 h-2.5 rounded bg-[#DFFF00]/20" />
                  <div className="w-full h-3.5 rounded bg-white/15" />
                  <div className="w-4/5 h-3.5 rounded bg-white/10" />
                </div>
              </div>
            ))}
          </div>
        </div>
      </aside>
    </div>
  );
}
