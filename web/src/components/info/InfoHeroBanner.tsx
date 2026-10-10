import React from 'react';
import { Info } from 'lucide-react';

export default function InfoHeroBanner() {
  return (
    <div className="rounded-3xl border border-[#334155]/60 bg-gradient-to-r from-[#1E3A8A]/30 via-[#181C20] to-[#101418] p-6 sm:p-8 relative overflow-hidden mb-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-6">
        <div className="space-y-2 max-w-2xl">
          <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-black tracking-wider uppercase bg-[#7098F8] text-[#101418]">
            Knowledge Hub
          </div>
          <h1 className="text-xl sm:text-3xl font-black text-white tracking-tight">
            Football Highlights & Clips — Info & Resources
          </h1>
          <p className="text-xs sm:text-sm text-[#94A3B8] leading-relaxed">
            Learn about our verified data sources, cross-platform entitlement syncing, independent development, and privacy standards.
          </p>
        </div>

        <div className="hidden sm:flex w-16 h-16 rounded-2xl bg-[#7098F8]/15 border border-[#7098F8]/40 items-center justify-center shrink-0 shadow-[0_0_20px_rgba(112,152,248,0.2)]">
          <Info className="w-8 h-8 text-[#7098F8]" />
        </div>
      </div>
    </div>
  );
}
