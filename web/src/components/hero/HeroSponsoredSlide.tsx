'use client';

import React from 'react';
import { ExternalLink, Sparkles } from 'lucide-react';
import NativeInFeedAdCard from '@/components/ads/NativeInFeedAdCard';

interface HeroSponsoredSlideProps {
  onLearnMore?: () => void;
}

export default function HeroSponsoredSlide({
  onLearnMore,
}: HeroSponsoredSlideProps) {
  return (
    <div className="relative w-full h-full min-h-[580px] lg:min-h-[660px] flex flex-col justify-end p-6 sm:p-10 lg:p-14 select-none">
      {/* Immersive Branded Mesh Background */}
      <div className="absolute inset-0 bg-gradient-to-tr from-[#0A0A0C] via-[#121316] to-[#1A1C22] z-0 overflow-hidden">
        <div className="absolute -top-24 -right-24 w-96 h-96 bg-[#DFFF00]/10 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute bottom-10 left-10 w-80 h-80 bg-blue-500/10 rounded-full blur-3xl pointer-events-none" />
      </div>

      {/* Smooth Multi-Stage Bottom Transition Scrim into #0A0A0C */}
      <div className="absolute inset-0 bg-gradient-to-t from-[#0A0A0C] via-[#0A0A0C]/80 via-[#0A0A0C]/30 to-transparent z-10" />

      {/* Content Container */}
      <div className="relative z-20 max-w-3xl space-y-4">
        <div className="flex items-center gap-2">
          <span className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#DFFF00] text-[#0A0A0C] font-tech text-xs font-black uppercase tracking-wider shadow-[0_0_15px_rgba(223,255,0,0.4)]">
            <Sparkles className="w-3.5 h-3.5 fill-[#0A0A0C]" />
            <span>Sponsored Spotlight</span>
          </span>
          <span className="px-2.5 py-0.5 rounded-full bg-black/60 border border-white/10 font-tech text-[10px] text-white/60 uppercase tracking-widest">
            Promoted Partner
          </span>
        </div>

        <h1 className="font-scoreboard text-3xl sm:text-5xl lg:text-6xl uppercase text-white tracking-wide leading-tight drop-shadow-lg">
          Official Broadcast &amp; Performance Partner
        </h1>

        <p className="font-tech text-xs sm:text-sm text-white/75 max-w-xl leading-relaxed">
          Experience ultra-low latency European match streams, accredited press feeds, and tactical breakdown hubs.
        </p>

        <div className="pt-2">
          <NativeInFeedAdCard slotId="5616816386" className="max-w-lg" />
        </div>
      </div>
    </div>
  );
}
