'use client';

import React, { useState, useEffect, useRef } from 'react';
import { ArrowLeft, ExternalLink, ShieldCheck, Loader2, RefreshCw } from 'lucide-react';
import { PADDLE_PAYWALL_URL } from '@/lib/revenuecat';

interface PaywallInlineCheckoutProps {
  userId: string;
  onBack: () => void;
  onSuccess: () => void;
}

export default function PaywallInlineCheckout({
  userId,
  onBack,
  onSuccess,
}: PaywallInlineCheckoutProps) {
  const [isLoading, setIsLoading] = useState(true);
  const iframeRef = useRef<HTMLIFrameElement | null>(null);
  const checkoutUrl = `${PADDLE_PAYWALL_URL}/${userId}`;

  const handleIframeLoad = () => {
    setIsLoading(false);
    try {
      const href = iframeRef.current?.contentWindow?.location?.href;
      if (
        href &&
        (href.includes('dirzaaulia.com') ||
          href.includes('localhost') ||
          href.includes(window.location.host))
      ) {
        onSuccess();
      }
    } catch {
      // Cross-origin on pay.rev.cat
    }
  };

  useEffect(() => {
    const handleMessage = (event: MessageEvent) => {
      const origin = event.origin || '';
      if (origin.includes('rev.cat') || origin.includes('paddle.com')) {
        const data = event.data;
        if (
          data?.type === 'checkout-completed' ||
          data?.event === 'checkout.completed' ||
          data?.status === 'completed' ||
          data?.success
        ) {
          onSuccess();
        }
      }
    };

    window.addEventListener('message', handleMessage);
    return () => window.removeEventListener('message', handleMessage);
  }, [onSuccess]);

  return (
    <div className="flex-1 flex flex-col space-y-3 h-full min-h-[520px]">
      {/* Top Header Controls */}
      <div className="flex items-center justify-between gap-2 pb-2 border-b border-[#334155]/50 shrink-0">
        <button
          onClick={onBack}
          className="flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#1E2227] hover:bg-[#334155]/60 text-xs font-semibold text-[#F1F5F9] transition-colors cursor-pointer"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Plans</span>
        </button>

        <a
          href={checkoutUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-1 text-[11px] text-[#94A3B8] hover:text-white transition-colors"
        >
          <span>Open in new window</span>
          <ExternalLink className="w-3 h-3" />
        </a>
      </div>

      {/* Inline Frame Container: weight 1f / flex-1 */}
      <div className="relative w-full flex-1 min-h-[460px] bg-[#101418] rounded-2xl border border-[#334155]/60 overflow-hidden shadow-2xl">
        {isLoading && (
          <div className="absolute inset-0 z-10 flex flex-col items-center justify-center bg-[#101418] space-y-3">
            <Loader2 className="w-8 h-8 text-[#D4AF37] animate-spin" />
            <p className="text-xs text-[#94A3B8] font-medium">Loading secure payment portal...</p>
          </div>
        )}

        <iframe
          ref={iframeRef}
          src={checkoutUrl}
          onLoad={handleIframeLoad}
          className="w-full h-full border-0"
          title="Paddle Secure Checkout"
          allow="payment *"
          sandbox="allow-forms allow-scripts allow-same-origin allow-popups allow-modals"
        />
      </div>

      {/* Post-Payment Status Sync */}
      <div className="flex items-center justify-between pt-1 shrink-0">
        <div className="flex items-center gap-1.5 text-[11px] text-[#94A3B8]">
          <ShieldCheck className="w-3.5 h-3.5 text-emerald-400" />
          <span>256-Bit SSL Encrypted by Paddle & RevenueCat</span>
        </div>

        <button
          onClick={onSuccess}
          className="flex items-center gap-1 text-[11px] text-[#D4AF37] hover:underline font-semibold cursor-pointer"
        >
          <RefreshCw className="w-3 h-3" />
          <span>Finished? Verify Premium</span>
        </button>
      </div>
    </div>
  );
}
