'use client';

import React, { useEffect, useRef } from 'react';
import { useProStatus } from '@/hooks/useProStatus';

interface AdSenseBannerProps {
  slotId?: string;
  format?: 'auto' | 'rectangle' | 'horizontal';
  className?: string;
}

export default function AdSenseBanner({
  slotId = '5616816386',
  format = 'auto',
  className = '',
}: AdSenseBannerProps) {
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
      const insElement = insRef.current;
      if (insElement && !insElement.getAttribute('data-adsbygoogle-status')) {
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
    <div
      className={`overflow-hidden rounded-2xl bg-[#181C20]/80 border border-[#334155]/60 p-3 text-center transition-all ${className}`}
    >
      <div className="flex items-center justify-between px-2 mb-1.5">
        <span className="text-[10px] uppercase tracking-wider text-[#94A3B8] font-bold">
          Advertisement
        </span>
      </div>

      <ins
        ref={insRef}
        className="adsbygoogle"
        style={{ display: 'block', minHeight: format === 'horizontal' ? '60px' : '90px' }}
        data-ad-client={clientId}
        data-ad-slot={slotId}
        data-ad-format={format}
        data-full-width-responsive="true"
      />
    </div>
  );
}
