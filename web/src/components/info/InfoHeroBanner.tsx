import React from 'react';
import { Info, Sparkles } from 'lucide-react';

export default function InfoHeroBanner() {
  return (
    <div className="rounded-3xl border border-white/10 bg-gradient-to-r from-[#121316] via-[#1A1C22] to-[#121316] p-6 sm:p-8 relative overflow-hidden mb-6 shadow-2xl">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
        <div className="space-y-2.5 max-w-2xl">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-tech font-bold tracking-wider uppercase bg-[#DFFF00] text-[#0A0A0C]">
            <Sparkles className="w-3.5 h-3.5 fill-current" />
            <span>Preferences Synced to OTT Edge Cloud</span>
          </div>
          <h1 className="font-scoreboard text-2xl sm:text-4xl text-white uppercase tracking-wide leading-none">
            Settings &amp; App Preferences
          </h1>
          <p className="text-xs sm:text-sm text-white/70 font-tech leading-relaxed">
            Configure spoiler protection buffers, real-time match alert push topics, and cross-platform entitlement syncing.
          </p>
        </div>

        <div className="hidden sm:flex w-16 h-16 rounded-2xl bg-white/5 border border-white/10 items-center justify-center shrink-0 shadow-lg">
          <Info className="w-8 h-8 text-[#DFFF00]" />
        </div>
      </div>
    </div>
  );
}
