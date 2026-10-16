'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { Smartphone, X, ExternalLink } from 'lucide-react';

const PLAY_STORE_URL = 'https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips';

export default function PlayStoreInstallBanner() {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    try {
      const dismissed = localStorage.getItem('footballclips_playstore_dismissed');
      if (!dismissed) {
        // Show after a subtle 1.5s delay so page content loads smoothly
        const timer = setTimeout(() => setIsVisible(true), 1500);
        return () => clearTimeout(timer);
      }
    } catch {
      // Fallback for private browsing
      setIsVisible(true);
    }
  }, []);

  const handleDismiss = () => {
    setIsVisible(false);
    try {
      localStorage.setItem('footballclips_playstore_dismissed', 'true');
    } catch {}
  };

  const handleInstallClick = () => {
    try {
      localStorage.setItem('footballclips_playstore_dismissed', 'true');
    } catch {}
    window.open(PLAY_STORE_URL, '_blank', 'noopener,noreferrer');
  };

  if (!isVisible) return null;

  return (
    <div className="fixed bottom-20 md:bottom-6 right-4 md:right-6 z-50 max-w-sm w-[calc(100%-2rem)] bg-[#181C20] border border-[#7098F8]/40 shadow-[0_8px_32px_rgba(0,0,0,0.5)] rounded-2xl p-4 transition-all duration-300 animate-in fade-in slide-in-from-bottom-5">
      <div className="flex items-start gap-3">
        {/* App Icon */}
        <div className="relative w-11 h-11 rounded-xl overflow-hidden border border-[#7098F8]/40 bg-[#101418] shrink-0">
          <Image
            src="/app_icon.png"
            alt="Football Clips Android App"
            width={44}
            height={44}
            className="object-cover"
          />
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0">
          <div className="flex items-center justify-between">
            <h4 className="text-xs font-black text-white tracking-tight flex items-center gap-1.5">
              <span>FootballClips on Android</span>
              <span className="text-[9px] font-bold bg-[#7098F8]/20 text-[#7098F8] px-1.5 py-0.5 rounded">Native</span>
            </h4>
            <button
              onClick={handleDismiss}
              className="text-[#94A3B8] hover:text-white p-1 -mr-1 -mt-1 rounded-lg transition-colors cursor-pointer"
              aria-label="Dismiss banner"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <p className="text-[11px] text-[#94A3B8] mt-1 leading-snug">
            Enjoy 60fps HD highlights, instant goal notifications, and spoiler-free mode on your Android device.
          </p>

          {/* Action Row */}
          <div className="mt-3 flex items-center gap-2">
            <button
              onClick={handleInstallClick}
              className="flex-1 flex items-center justify-center gap-1.5 bg-[#7098F8] hover:bg-[#5b85f0] text-black font-black text-xs py-2 px-3 rounded-xl transition-all shadow-[0_0_12px_rgba(112,152,248,0.3)] cursor-pointer"
            >
              <Smartphone className="w-3.5 h-3.5" />
              <span>Install from Google Play</span>
              <ExternalLink className="w-3 h-3 ml-0.5 opacity-70" />
            </button>
            <button
              onClick={handleDismiss}
              className="text-xs text-[#94A3B8] hover:text-white font-medium px-2 py-1.5 rounded-lg transition-colors cursor-pointer"
            >
              Later
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
