'use client';

import React, { useEffect, useRef } from 'react';
import { useProStatus } from '@/hooks/useProStatus';

interface NativeInFeedAdCardProps {
  slotId?: string;
}

export default function NativeInFeedAdCard({
  slotId = '5616816386',
}: NativeInFeedAdCardProps) {
  const hasPro = useProStatus();
  const clientId = process.env.NEXT_PUBLIC_ADSENSE_CLIENT_ID || 'ca-pub-6717632447198427';
  const insRef = useRef<HTMLModElement>(null);
  const isPushed = useRef(false);

  useEffect(() => {
    if (hasPro) return;
    if (typeof window === 'undefined') return;
    if (isPushed.current) return;

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

  if (hasPro) return null;

  return (
    <div className="relative bg-[#1E2227] border border-[#334155] rounded-2xl overflow-hidden p-4 flex flex-col justify-between transition-all duration-300 min-h-[310px]">
      {/* Top Header Pill */}
      <div className="flex items-center justify-between pb-3 border-b border-[#334155]/60 mb-3">
        <span className="text-[10px] font-bold uppercase tracking-wider text-[#94A3B8]">
          Advertisement
        </span>
      </div>

      {/* Ad Body */}
      <div className="flex-1 flex flex-col items-center justify-center my-2 overflow-hidden">
        <ins
          ref={insRef}
          className="adsbygoogle"
          style={{ display: 'block', width: '100%', minHeight: '180px' }}
          data-ad-client={clientId}
          data-ad-slot={slotId}
          data-ad-format="rectangle"
          data-full-width-responsive="true"
        />
      </div>

      {/* Bottom Label */}
      <div className="pt-3 border-t border-[#334155]/50 flex items-center justify-between text-[11px] text-[#94A3B8]">
        <span>Sponsored Content</span>
        <span className="text-[10px] text-[#7098F8] font-bold">Ad</span>
      </div>
    </div>
  );
}
