'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { ShieldCheck, X } from 'lucide-react';

const CONSENT_STORAGE_KEY = 'fc-cookie-consent-v1';

export default function CookieConsentBanner() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    try {
      const consent = localStorage.getItem(CONSENT_STORAGE_KEY);
      if (!consent) {
        // Small delay to ensure smooth entry without jarring initial load
        const timer = setTimeout(() => setVisible(true), 600);
        return () => clearTimeout(timer);
      }
    } catch {
      // LocalStorage unavailable
    }
  }, []);

  const handleAccept = () => {
    try {
      localStorage.setItem(CONSENT_STORAGE_KEY, 'accepted');
    } catch {
      // ignore
    }
    setVisible(false);
  };

  const handleDismiss = () => {
    try {
      localStorage.setItem(CONSENT_STORAGE_KEY, 'dismissed');
    } catch {
      // ignore
    }
    setVisible(false);
  };

  if (!visible) return null;

  return (
    <aside
      aria-label="Privacy and Cookie Consent"
      className="fixed bottom-4 right-4 z-50 max-w-sm sm:max-w-md w-[calc(100%-2rem)] bg-[#121316]/95 border border-white/15 backdrop-blur-lg rounded-2xl p-4 shadow-[0_10px_35px_rgba(0,0,0,0.6)] space-y-3 select-none animate-in fade-in slide-in-from-bottom-3 duration-300"
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex items-center gap-2">
          <div className="w-7 h-7 rounded-lg bg-[#DFFF00]/10 border border-[#DFFF00]/30 flex items-center justify-center text-[#DFFF00] shrink-0">
            <ShieldCheck className="w-4 h-4" />
          </div>
          <span className="text-xs font-bold text-white font-tech uppercase tracking-wider">
            Privacy &amp; Cookie Consent
          </span>
        </div>
        <button
          onClick={handleDismiss}
          className="text-white/40 hover:text-white p-1 rounded-lg transition-colors cursor-pointer"
          title="Dismiss banner"
          aria-label="Dismiss banner"
        >
          <X className="w-4 h-4" />
        </button>
      </div>

      <p className="text-[11px] text-[#A1A1AA] leading-relaxed font-tech">
        We utilize necessary cookies, anonymous telemetry, and advertising cookies to ensure site functionality, deliver verified match telemetry, and support free sports journalism. Read our{' '}
        <Link href="/privacy" className="text-[#DFFF00] hover:underline font-bold">
          Privacy Policy
        </Link>
        .
      </p>

      <div className="flex items-center justify-end gap-2 pt-1 font-tech">
        <button
          onClick={handleDismiss}
          className="px-3 py-1.5 rounded-xl text-xs text-white/60 hover:text-white bg-white/5 border border-white/10 transition-colors cursor-pointer"
        >
          Essential Only
        </button>
        <button
          onClick={handleAccept}
          className="px-4 py-1.5 rounded-xl text-xs font-bold text-[#0A0A0C] bg-[#DFFF00] hover:bg-[#cbe800] shadow-[0_0_12px_rgba(223,255,0,0.3)] transition-all cursor-pointer"
        >
          Accept All
        </button>
      </div>
    </aside>
  );
}
