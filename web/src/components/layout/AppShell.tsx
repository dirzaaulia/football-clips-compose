'use client';

import React, { useState, useEffect } from 'react';
import { usePathname } from 'next/navigation';
import TopHeaderNav from '@/components/layout/TopHeaderNav';
import LiveScoreTicker from '@/components/layout/LiveScoreTicker';
import Footer from '@/components/layout/Footer';
import SplashScreen from '@/components/common/SplashScreen';
import CookieConsentBanner from '@/components/common/CookieConsentBanner';
import PlayStoreInstallBanner from '@/components/common/PlayStoreInstallBanner';
import FloatingMiniPlayer from '@/components/player/FloatingMiniPlayer';
import QuickCommandPalette from '@/components/search/QuickCommandPalette';
import QueryProvider from '@/components/providers/QueryProvider';
import PaywallModal from '@/components/billing/PaywallModal';
import AdSenseScript from '@/components/common/AdSenseScript';
import { useProStatus } from '@/hooks/useProStatus';

interface AppShellProps {
  children: React.ReactNode;
}

export default function AppShell({ children }: AppShellProps) {
  const pathname = usePathname();
  const hasPro = useProStatus();
  const [isPaywallOpen, setIsPaywallOpen] = useState(false);
  const isTelemetry = pathname?.startsWith('/telemetry') ?? false;

  useEffect(() => {
    const handleOpenPaywall = () => setIsPaywallOpen(true);
    window.addEventListener('fc-open-paywall', handleOpenPaywall);
    return () => window.removeEventListener('fc-open-paywall', handleOpenPaywall);
  }, []);

  if (isTelemetry) {
    return (
      <QueryProvider>
        <div className="min-h-screen w-full bg-[#0A0A0C] text-slate-100 flex flex-col">
          <main className="flex-1 w-full">{children}</main>
        </div>
      </QueryProvider>
    );
  }

  const isShorts = pathname?.startsWith('/shorts') ?? false;

  return (
    <QueryProvider>
      <div className={`min-h-screen w-full bg-[#0A0A0C] text-[#F1F5F9] flex flex-col ${isShorts ? 'h-screen overflow-hidden' : ''}`}>
        <SplashScreen />

        {/* Stitch 1:1 Fixed Top OTT Header Nav */}
        <TopHeaderNav
          onOpenPaywall={() => setIsPaywallOpen(true)}
          isPro={hasPro}
        />

        {/* Main Content Area offset by top header */}
        <div className={`flex-1 flex flex-col min-w-0 pt-12 relative ${isShorts ? 'h-[calc(100vh-3rem)] overflow-hidden' : ''}`}>
          {!isShorts && <LiveScoreTicker />}
          <main className={`flex-1 ${isShorts ? 'h-full overflow-hidden' : ''}`}>{children}</main>
          {!isShorts && <Footer />}
          <QuickCommandPalette />
          <CookieConsentBanner />
          <PlayStoreInstallBanner />
        </div>

        {/* Global Pro / Paywall Modal */}
        <PaywallModal
          isOpen={isPaywallOpen}
          onClose={() => setIsPaywallOpen(false)}
        />

        {/* Floating Docked Mini-Player */}
        <FloatingMiniPlayer />

        {/* Selective Google AdSense Integration (suppressed on /telemetry) */}
        <AdSenseScript />
      </div>
    </QueryProvider>
  );
}
