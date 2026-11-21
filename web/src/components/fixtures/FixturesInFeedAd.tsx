'use client';

import React, { useEffect, useRef } from 'react';
import { useProStatus } from '@/hooks/useProStatus';

export default function FixturesInFeedAd() {
  const hasPro = useProStatus();
  const clientId = process.env.NEXT_PUBLIC_ADSENSE_CLIENT_ID || 'ca-pub-6717632447198427';
  const insRef = useRef<HTMLModElement>(null);
  const isPushed = useRef(false);

  useEffect(() => {
    if (hasPro || typeof window === 'undefined' || isPushed.current) return;

    const isDev =
      window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
    if (isDev) return;

    try {
      const ins = insRef.current;
      if (ins && !ins.getAttribute('data-adsbygoogle-status')) {
        ((window as unknown as { adsbygoogle?: unknown[] }).adsbygoogle =
          (window as unknown as { adsbygoogle?: unknown[] }).adsbygoogle || []).push({});
        isPushed.current = true;
      }
    } catch {
      // Ignored
    }
  }, [hasPro]);

  // Temporarily suppressed during AdSense site review to prevent "Valuable inventory: No content" rejection
  // Auto Ads via head script handles evaluation; manual slots re-enable post-approval.
  return null;

  return (
    <div className="w-full rounded-2xl bg-[#121316]/70 border border-white/5 p-4 flex flex-col justify-between shadow-lg my-3">
      <div className="flex items-center justify-between pb-2 mb-2 border-b border-white/5">
        <span className="text-[9px] font-tech uppercase tracking-widest text-white/40 border border-white/10 px-2 py-0.5 rounded">
          Advertisement
        </span>
        <span className="text-[9px] font-mono text-white/20">Google AdSense</span>
      </div>
      <div className="min-h-[90px] flex items-center justify-center overflow-hidden rounded-xl bg-black/20">
        <ins
          ref={insRef}
          className="adsbygoogle"
          style={{ display: 'block', width: '100%', minHeight: '90px' }}
          data-ad-client={clientId}
          data-ad-slot="5616816386"
          data-ad-format="horizontal"
          data-full-width-responsive="true"
        />
      </div>
    </div>
  );
}
