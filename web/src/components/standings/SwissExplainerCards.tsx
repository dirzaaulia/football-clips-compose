'use client';

import React from 'react';

export default function SwissExplainerCards() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
      <div className="bg-[#1A1C22] border border-[#DFFF00]/30 rounded-xl p-3.5 flex items-start gap-3 shadow-md">
        <span className="w-2.5 h-2.5 rounded-full bg-[#DFFF00] mt-1 shrink-0 shadow-[0_0_8px_rgba(223,255,0,0.5)]" />
        <div className="flex flex-col">
          <span className="text-xs font-bold text-white uppercase font-tech tracking-wider">
            1st – 8th (Direct R16)
          </span>
          <span className="text-[11px] text-white/60 font-body">
            Direct qualification. Seeded in Round of 16 draw.
          </span>
        </div>
      </div>

      <div className="bg-[#1A1C22] border border-[#38BDF8]/30 rounded-xl p-3.5 flex items-start gap-3 shadow-md">
        <span className="w-2.5 h-2.5 rounded-full bg-[#38BDF8] mt-1 shrink-0 shadow-[0_0_8px_rgba(56,189,248,0.5)]" />
        <div className="flex flex-col">
          <span className="text-xs font-bold text-white uppercase font-tech tracking-wider">
            9th – 24th (Play-offs)
          </span>
          <span className="text-[11px] text-white/60 font-body">
            Two-legged knockout phase play-offs to reach R16.
          </span>
        </div>
      </div>

      <div className="bg-[#1A1C22] border border-white/10 rounded-xl p-3.5 flex items-start gap-3 shadow-md">
        <span className="w-2.5 h-2.5 rounded-full bg-[#FF2E4D] mt-1 shrink-0 shadow-[0_0_8px_rgba(255,46,77,0.5)]" />
        <div className="flex flex-col">
          <span className="text-xs font-bold text-white uppercase font-tech tracking-wider">
            25th – 36th (Eliminated)
          </span>
          <span className="text-[11px] text-white/60 font-body">
            Eliminated from European competition. No dropping down.
          </span>
        </div>
      </div>
    </div>
  );
}
