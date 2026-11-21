'use client';

import React, { useState, useEffect } from 'react';
import Image from 'next/image';
import { X } from 'lucide-react';
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
    <aside
      aria-label="Download Football Clips on Google Play"
      className="fixed bottom-20 md:bottom-6 left-4 md:left-6 z-40 max-w-sm w-[calc(100%-2rem)] bg-[#121316]/95 backdrop-blur-xl border border-white/10 hover:border-[#DFFF00]/30 shadow-[0_12px_40px_rgba(0,0,0,0.8)] rounded-2xl p-4 transition-all duration-300 animate-in fade-in slide-in-from-bottom-5"
    >
      <div className="flex items-start gap-3.5">
        <div className="relative w-11 h-11 rounded-xl overflow-hidden border border-[#DFFF00]/30 bg-gradient-to-br from-[#1A1C23] to-[#121316] shadow-sm shadow-[#DFFF00]/10 shrink-0">
          <Image
            src="/app_icon.png"
            alt="Football Clips Android App"
            width={44}
            height={44}
            className="object-cover"
          />
        </div>

        <div className="flex-1 min-w-0 space-y-1.5">
          <div className="flex items-center justify-between">
            <h4 className="text-xs font-bold text-white tracking-tight flex items-center gap-1.5">
              <span>Football Clips on Android</span>
              <span className="text-[9px] font-black uppercase tracking-wider bg-[#DFFF00]/15 text-[#DFFF00] border border-[#DFFF00]/30 px-1.5 py-0.5 rounded-full font-tech">
                Official
              </span>
            </h4>
            <button
              onClick={handleDismiss}
              className="text-slate-400 hover:text-white p-1 -mr-1 -mt-1 rounded-lg transition-colors cursor-pointer"
              aria-label="Dismiss Android banner"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <p className="text-[11px] text-slate-300/90 leading-snug">
            60FPS HD highlights, live goal alerts, and spoiler-free mode on your Android device.
          </p>

          <div className="pt-1 flex items-center justify-between gap-2">
            <GooglePlayBadge
              height={34}
              onClick={() => {
                try {
                  localStorage.setItem('footballclips_playstore_dismissed', 'true');
                } catch {}
              }}
            />
            <button
              onClick={handleDismiss}
              className="text-xs text-slate-400 hover:text-white font-medium px-2 py-1 rounded-lg transition-colors cursor-pointer"
            >
              Later
            </button>
          </div>
        </div>
      </div>
    </aside>
  );
}
