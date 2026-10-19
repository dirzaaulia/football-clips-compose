'use client';

import React, { useEffect, useRef } from 'react';
import { useProStatus } from '@/hooks/useProStatus';

interface NativeInFeedAdCardProps {
  slotId?: string;
  variant?: 'feed' | 'carousel';
  className?: string;
}

export default function NativeInFeedAdCard({
  slotId = '5616816386',
  variant = 'feed',
  className = '',
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

  const isCarousel = variant === 'carousel';

  return (
    <div
      className={`relative bg-[#1E2227] border border-[#334155] rounded-3xl overflow-hidden p-4 flex flex-col justify-between transition-all duration-300 select-none ${
        isCarousel
          ? 'w-[310px] sm:w-[360px] h-[310px] sm:h-[330px] shrink-0'
          : 'min-h-[310px] w-full'
      } ${className}`}
    >
      {/* Top Header Pill */}
      <div className="flex items-center justify-between pb-2 border-b border-[#334155]/60 mb-2">
        <span className="text-[10px] font-bold uppercase tracking-wider text-[#94A3B8]">
          Advertisement
        </span>
      </div>

      {/* Ad Body */}
      <div className="flex-1 flex flex-col items-center justify-center my-1 overflow-hidden">
        <ins
          ref={insRef}
          className="adsbygoogle"
          style={{ display: 'block', width: '100%', height: '100%', minHeight: isCarousel ? '170px' : '180px', maxHeight: isCarousel ? '190px' : undefined }}
          data-ad-client={clientId}
          data-ad-slot={slotId}
          data-ad-format="rectangle"
          data-full-width-responsive="true"
        />
      </div>

      {/* Bottom Label */}
      <div className="pt-2 border-t border-[#334155]/40 flex items-center justify-end text-[10px] text-[#64748B]">
        <span>Google AdSense</span>
      </div>
    </div>
  );
}
