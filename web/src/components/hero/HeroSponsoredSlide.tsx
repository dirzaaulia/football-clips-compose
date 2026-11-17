'use client';

import React, { useEffect, useRef } from 'react';
import Link from 'next/link';
import { Sparkles, ShieldCheck } from 'lucide-react';

const ADSENSE_PUBLISHER_ID =
  process.env.NEXT_PUBLIC_ADSENSE_CLIENT_ID || 'ca-pub-6717632447198427';

declare global {
  interface Window {
    adsbygoogle?: unknown[];
  }
}

export default function HeroSponsoredSlide() {
  const pushed = useRef(false);

  useEffect(() => {
    if (pushed.current) return;
    const timer = setTimeout(() => {
      try {
        if (typeof window !== 'undefined') {
          window.adsbygoogle = window.adsbygoogle || [];
          window.adsbygoogle.push({});
          pushed.current = true;
        }
      } catch {
        // Adblocker or script pending
      }
    }, 250);
    return () => clearTimeout(timer);
  }, []);

  return (
    <div className="relative w-full h-full min-h-[580px] lg:min-h-[660px] flex flex-col justify-end p-6 sm:p-10 lg:p-14 select-none">
      {/* Subtle Dark Stadium Mesh Background */}
      <div className="absolute inset-0 bg-gradient-to-tr from-[#0A0A0C] via-[#121316] to-[#1A1C22] z-0 overflow-hidden">
        <div className="absolute -top-24 -right-24 w-96 h-96 bg-[#DFFF00]/5 rounded-full blur-3xl pointer-events-none" />
        <div className="absolute bottom-10 left-10 w-80 h-80 bg-[#38BDF8]/5 rounded-full blur-3xl pointer-events-none" />
      </div>

      {/* Bottom Scrim */}
      <div className="absolute inset-0 bg-gradient-to-t from-[#0A0A0C] via-[#0A0A0C]/85 to-transparent z-10 pointer-events-none" />

      {/* Google AdSense Review-Compliant Display Container */}
      <div className="relative z-20 max-w-4xl w-full space-y-3 pb-6">
        <div className="flex items-center justify-between gap-3">
          <span className="px-2.5 py-0.5 rounded bg-white/5 border border-white/10 font-tech text-[10px] text-white/50 uppercase tracking-widest">
            Advertisement
          </span>
          <Link
            href="/settings"
            className="inline-flex items-center gap-1.5 text-[11px] font-tech text-[#DFFF00]/80 hover:text-[#DFFF00] transition-colors"
          >
            <Sparkles className="w-3 h-3" />
            <span>Remove Ads with FootballClips PRO</span>
          </Link>
        </div>

        <div className="w-full rounded-2xl bg-[#121316]/90 border border-white/10 p-4 sm:p-6 shadow-2xl min-h-[250px] flex flex-col items-center justify-center relative overflow-hidden">
          <ins
            className="adsbygoogle"
            style={{ display: 'block', width: '100%', minHeight: '220px' }}
            data-ad-client={ADSENSE_PUBLISHER_ID}
            data-ad-slot="5616816386"
            data-ad-format="auto"
            data-full-width-responsive="true"
          />
          <div className="flex items-center gap-2 text-[10px] font-tech text-white/35 mt-2">
            <ShieldCheck className="w-3.5 h-3.5 text-[#DFFF00]/50" />
            <span>Google AdSense Verified Display Unit • Publisher {ADSENSE_PUBLISHER_ID}</span>
          </div>
        </div>
      </div>
    </div>
  );
}
