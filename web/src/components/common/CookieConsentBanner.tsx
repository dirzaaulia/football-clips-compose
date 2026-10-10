'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { ShieldCheck, X } from 'lucide-react';

export default function CookieConsentBanner() {
  const [show, setShow] = useState(false);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (typeof window !== 'undefined' && !localStorage.getItem('fc_cookie_consent')) {
        setShow(true);
      }
    }, 1500);
    return () => clearTimeout(timer);
  }, []);

  const handleConsent = (level: 'all' | 'essential') => {
    if (typeof window !== 'undefined') {
      localStorage.setItem('fc_cookie_consent', level);
    }
    setShow(false);
  };

  if (!show) return null;

  return (
    <div className="fixed bottom-28 md:bottom-8 inset-x-4 md:inset-x-auto md:right-8 max-w-sm mx-auto md:mx-0 z-30 animate-in slide-in-from-bottom duration-300 pointer-events-auto">
      <div className="rounded-2xl bg-[#181C20]/95 backdrop-blur-xl border border-[#334155]/90 shadow-[0_12px_36px_rgba(0,0,0,0.85)] p-4 space-y-2.5">
        <div className="flex items-start justify-between gap-3">
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-[#1E3A8A]/50 border border-[#7098F8]/40 flex items-center justify-center shrink-0">
              <ShieldCheck className="w-4 h-4 text-[#4FC3F7]" />
            </div>
            <h4 className="text-xs font-bold text-white tracking-tight">Cookie &amp; Privacy Choices</h4>
          </div>
          <button
            onClick={() => handleConsent('essential')}
            className="text-[#94A3B8] hover:text-white p-1 rounded-lg hover:bg-[#1E2227] cursor-pointer"
            aria-label="Dismiss"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>

        <p className="text-[11px] text-[#94A3B8] leading-relaxed">
          We use essential cookies to maintain user sessions and Google AdSense / Analytics cookies to serve relevant sports advertisements. Consult our{' '}
          <Link href="/privacy" className="text-[#7098F8] hover:underline font-semibold">
            Privacy Policy
          </Link>{' '}
          or manage your{' '}
          <a
            href="https://adssettings.google.com"
            target="_blank"
            rel="noopener noreferrer"
            className="text-[#4FC3F7] hover:underline font-semibold"
          >
            Google Ad Settings
          </a>.
        </p>

        <div className="flex items-center gap-2 pt-1">
          <button
            onClick={() => handleConsent('all')}
            className="flex-1 py-1.5 px-3 rounded-xl bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white font-bold text-[11px] hover:brightness-110 transition-all shadow-[0_0_12px_rgba(112,152,248,0.25)] cursor-pointer"
          >
            Accept All
          </button>
          <button
            onClick={() => handleConsent('essential')}
            className="py-1.5 px-3 rounded-xl bg-[#1E2227] hover:bg-[#252A30] text-[#94A3B8] hover:text-white border border-[#334155] font-semibold text-[11px] transition-colors cursor-pointer"
          >
            Essential Only
          </button>
        </div>
      </div>
    </div>
  );
}
