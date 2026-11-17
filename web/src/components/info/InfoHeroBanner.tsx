import React from 'react';
import { Info, Sparkles } from 'lucide-react';

export default function InfoHeroBanner() {
  return (
    <div className="rounded-2xl border border-white/10 bg-gradient-to-r from-[#121316] via-[#1A1C22] to-[#121316] p-6 sm:p-8 relative overflow-hidden shadow-2xl">
      <div className="absolute -right-20 -top-20 w-72 h-72 rounded-full bg-[#DFFF00]/5 blur-3xl pointer-events-none" />
      <div className="relative z-10 flex flex-col sm:flex-row sm:items-center justify-between gap-6">
        <div className="space-y-2.5 max-w-2xl">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-tech font-bold tracking-wider uppercase bg-[#DFFF00] text-[#0A0A0C]">
            <Sparkles className="w-3.5 h-3.5 fill-current" />
            <span>Knowledge Hub • Preferences Synced</span>
          </div>
          <h1 className="font-scoreboard text-2xl sm:text-4xl text-white uppercase tracking-wide leading-none">
            Settings &amp; App Resources
          </h1>
          <p className="text-xs sm:text-sm text-white/70 font-tech leading-relaxed">
            Configure spoiler protection, favorite club alerts, cross-platform PRO
            entitlement syncing, and review verified data sources &amp; privacy standards.
          </p>
        </div>

        <div className="hidden sm:flex w-16 h-16 rounded-2xl bg-[#121316] border border-[#DFFF00]/30 items-center justify-center shrink-0 shadow-[0_0_20px_rgba(223,255,0,0.1)]">
          <Info className="w-8 h-8 text-[#DFFF00]" />
        </div>
      </div>
    </div>
  );
}
