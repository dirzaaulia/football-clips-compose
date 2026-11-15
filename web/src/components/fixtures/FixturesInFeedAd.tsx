'use client';

import React from 'react';
import { ExternalLink, Sparkles } from 'lucide-react';

export default function FixturesInFeedAd() {
  return (
    <div className="w-full rounded-2xl bg-gradient-to-r from-[#1A1C22] via-[#121316] to-[#1A1C22] border border-[#DFFF00]/20 p-4 sm:p-5 flex flex-col md:flex-row md:items-center justify-between gap-4 shadow-lg my-2">
      <div className="flex flex-col gap-1">
        <div className="flex items-center gap-2">
          <span className="px-2 py-0.5 rounded bg-[#DFFF00] text-[#0A0A0C] font-tech text-[10px] font-bold uppercase tracking-wider">
            Sponsored Showcase
          </span>
          <span className="text-[11px] font-tech text-white/50 uppercase tracking-wider">
            Official Equipment
          </span>
        </div>
        <h4 className="font-scoreboard text-xl sm:text-2xl text-white tracking-wide uppercase">
          Adidas Predator 2026: Unleash Surgical Zone 14 Precision
        </h4>
        <p className="text-xs text-white/70 max-w-xl">
          Engineered for elite ball control and clinical finishing. Experience the new volt colorway worn by Europe&apos;s top playmakers.
        </p>
      </div>

      <div className="shrink-0 flex items-center">
        <a
          href="https://www.adidas.com/football"
          target="_blank"
          rel="noopener noreferrer"
          className="inline-flex items-center gap-2 px-4 py-2 rounded-xl bg-white/10 hover:bg-white/20 text-white font-tech text-xs font-bold uppercase border border-white/20 transition-all cursor-pointer"
        >
          <Sparkles className="w-3.5 h-3.5 text-[#DFFF00]" />
          <span>Shop Collection</span>
          <ExternalLink className="w-3.5 h-3.5 text-white/60" />
        </a>
      </div>
    </div>
  );
}
