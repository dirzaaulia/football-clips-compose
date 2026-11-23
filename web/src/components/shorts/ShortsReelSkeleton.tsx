import React from 'react';

export default function ShortsReelSkeleton() {
  return (
    <div className="w-full h-full flex justify-center items-center animate-pulse select-none bg-[#0A0A0C]">
      {/* Center 9:16 Video Chassis + Action Rail + Steppers */}
      <div className="relative flex items-center justify-center gap-4 sm:gap-6 h-full py-2">
        <div className="relative w-[360px] sm:w-[410px] md:w-[430px] h-[calc(100vh-5rem)] max-h-[820px] rounded-3xl overflow-hidden bg-[#121316] border border-white/10 shadow-2xl flex flex-col justify-between p-5">
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
    </div>
  );
}
