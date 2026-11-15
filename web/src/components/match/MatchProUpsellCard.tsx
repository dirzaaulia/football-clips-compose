'use client';

import React from 'react';
import Link from 'next/link';
import { Sparkles, ArrowRight } from 'lucide-react';

export default function MatchProUpsellCard() {
  return (
    <div className="w-full rounded-2xl bg-[#0A0A0C] border border-white/10 p-5 shadow-xl flex flex-col gap-3.5 relative overflow-hidden select-none">
      {/* Background Subtle Volt Blur */}
      <div className="absolute -right-8 -top-8 w-32 h-32 rounded-full bg-[#DFFF00]/10 blur-2xl pointer-events-none" />

      <div className="flex items-center justify-between z-10">
        <span className="flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-[#DFFF00] text-[#0A0A0C] font-tech text-[10px] font-black uppercase tracking-wider">
          <Sparkles className="w-3 h-3 fill-[#0A0A0C]" />
          <span>FootballClips Pro</span>
        </span>
        <span className="font-tech text-[10px] text-white/50 uppercase tracking-widest">
          Cancel Anytime
        </span>
      </div>

      <div className="space-y-1 z-10">
        <h4 className="font-scoreboard text-base sm:text-lg text-white uppercase tracking-wide leading-snug">
          Stream in Crisp HD with Zero Ads
        </h4>
        <p className="font-tech text-xs text-white/60 leading-relaxed">
          Access high-bitrate highlights, multi-angle tactical pitch radar, and synchronized debriefs completely uninterrupted.
        </p>
      </div>

      <div className="flex items-baseline gap-1.5 z-10 pt-1">
        <span className="font-scoreboard text-2xl text-[#DFFF00]">Rp 29.000</span>
        <span className="font-tech text-xs text-white/50">/ month</span>
      </div>

      <Link
        href="/info#pro"
        className="w-full py-2.5 rounded-xl bg-[#DFFF00] hover:bg-[#cbe800] text-[#0A0A0C] font-tech text-xs font-black uppercase tracking-wider flex items-center justify-center gap-2 transition-all shadow-[0_0_15px_rgba(223,255,0,0.3)] hover:scale-[1.02] active:scale-[0.98] z-10"
      >
        <span>Upgrade to Pro Now</span>
        <ArrowRight className="w-3.5 h-3.5" />
      </Link>
    </div>
  );
}
