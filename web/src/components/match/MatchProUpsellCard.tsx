'use client';

import React from 'react';
import { Sparkles, Check, ArrowRight } from 'lucide-react';
import { useProStatus } from '@/hooks/useProStatus';
import { trackPaywallView } from '@/lib/analytics';

export default function MatchProUpsellCard() {
  const hasPro = useProStatus();

  if (hasPro) return null;

  const handleOpenPaywall = () => {
    trackPaywallView('Match Centre Sidebar');
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('fc-open-paywall'));
    }
  };

  return (
    <div className="w-full rounded-2xl bg-[#0A0A0C] border border-white/10 p-5 shadow-xl flex flex-col gap-3.5 relative overflow-hidden select-none">
      <div className="absolute -right-8 -top-8 w-32 h-32 rounded-full bg-[#DFFF00]/10 blur-2xl pointer-events-none" />

      <div className="flex items-center justify-between z-10">
        <span className="flex items-center gap-1.5 px-2.5 py-0.5 rounded-full bg-[#DFFF00] text-[#0A0A0C] font-tech text-[10px] font-black uppercase tracking-wider">
          <Sparkles className="w-3 h-3 fill-[#0A0A0C]" />
          <span>FootballClips Pro</span>
        </span>
        <span className="font-tech text-[10px] text-white/50 uppercase tracking-widest">
          Ad-Free Sync
        </span>
      </div>

      <div className="space-y-1.5 z-10">
        <h4 className="font-scoreboard text-xl text-white uppercase tracking-wide leading-tight">
          Go 100% Ad-Free Across Web &amp; Android
        </h4>
        <p className="font-tech text-xs text-white/60 leading-relaxed">
          Remove all banner and native advertisements, customize merchandise recommendations, and sync your Pro status with your Google account.
        </p>
      </div>

      <div className="space-y-1.5 z-10 font-tech text-[11px] text-white/80">
        <div className="flex items-center gap-2">
          <Check className="w-3.5 h-3.5 text-[#DFFF00] shrink-0" />
          <span>Zero banner or feed ads across all screens</span>
        </div>
        <div className="flex items-center gap-2">
          <Check className="w-3.5 h-3.5 text-[#DFFF00] shrink-0" />
          <span>Toggle club merchandise shelves on or off</span>
        </div>
        <div className="flex items-center gap-2">
          <Check className="w-3.5 h-3.5 text-[#DFFF00] shrink-0" />
          <span>Cross-platform sync with the Android app</span>
        </div>
      </div>

      <button
        type="button"
        onClick={handleOpenPaywall}
        className="w-full py-2.5 px-4 rounded-xl bg-[#DFFF00] hover:bg-[#c8e600] text-[#0A0A0C] font-tech text-xs font-extrabold uppercase tracking-wider flex items-center justify-center gap-2 transition-all shadow-[0_0_16px_rgba(223,255,0,0.2)] z-10 cursor-pointer"
      >
        <span>Upgrade to Pro</span>
        <ArrowRight className="w-3.5 h-3.5" />
      </button>
    </div>
  );
}
