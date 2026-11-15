'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { Smartphone, X, ExternalLink } from 'lucide-react';

const PLAY_STORE_URL = 'https://play.google.com/store/apps/details?id=com.dirzaaulia.footballclips';

import GooglePlayBadge from './GooglePlayBadge';

export default function PlayStoreInstallBanner() {
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    try {
      const dismissed = localStorage.getItem('footballclips_playstore_dismissed');
      if (!dismissed) {
        const timer = setTimeout(() => setIsVisible(true), 1500);
        return () => clearTimeout(timer);
      }
    } catch {
      setIsVisible(true);
    }
  }, []);

  const handleDismiss = () => {
    setIsVisible(false);
    try {
      localStorage.setItem('footballclips_playstore_dismissed', 'true');
    } catch {}
  };

  if (!isVisible) return null;

  return (
    <div className="fixed bottom-20 md:bottom-6 right-4 md:right-6 z-50 max-w-sm w-[calc(100%-2rem)] bg-[#0C1017]/95 backdrop-blur-xl border border-white/10 shadow-[0_12px_40px_rgba(0,0,0,0.8)] rounded-3xl p-4 transition-all duration-300 animate-in fade-in slide-in-from-bottom-5">
      <div className="flex items-start gap-3.5">
        {/* App Icon */}
        <div className="relative w-12 h-12 rounded-2xl overflow-hidden border border-cyan-500/30 bg-gradient-to-br from-blue-600/30 to-cyan-500/20 shadow-md shadow-cyan-500/10 shrink-0">
          <Image
            src="/app_icon.png"
            alt="Football Highlights & Clips Android App"
            width={48}
            height={48}
            className="object-cover"
          />
        </div>

        {/* Content */}
        <div className="flex-1 min-w-0 space-y-2">
          <div className="flex items-center justify-between">
            <h4 className="text-xs font-black text-white tracking-tight flex items-center gap-1.5">
              <span>Football Clips on Android</span>
              <span className="text-[9px] font-black bg-cyan-950 text-cyan-400 border border-cyan-500/30 px-1.5 py-0.5 rounded-full">Official</span>
            </h4>
            <button
              onClick={handleDismiss}
              className="text-slate-400 hover:text-white p-1 -mr-1 -mt-1 rounded-lg transition-colors cursor-pointer"
              aria-label="Dismiss banner"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <p className="text-[11px] text-slate-400 leading-snug">
            60FPS HD highlights, live goal alerts, and spoiler-free mode on your Android device.
          </p>

          {/* Action Row */}
          <div className="pt-1 flex items-center justify-between gap-2">
            <GooglePlayBadge
              height={36}
              onClick={() => {
                try {
                  localStorage.setItem('footballclips_playstore_dismissed', 'true');
                } catch {}
              }}
            />
            <button
              onClick={handleDismiss}
              className="text-[11px] text-slate-400 hover:text-white font-semibold px-2 py-1 rounded-lg transition-colors cursor-pointer"
            >
              Later
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
