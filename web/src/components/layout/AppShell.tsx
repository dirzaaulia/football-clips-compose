'use client';

import React, { Suspense } from 'react';
import { usePathname } from 'next/navigation';
import Sidebar from '@/components/layout/Sidebar';
import BottomNav from '@/components/layout/BottomNav';
import Footer from '@/components/layout/Footer';
import SplashScreen from '@/components/common/SplashScreen';
import CookieConsentBanner from '@/components/common/CookieConsentBanner';
import PlayStoreInstallBanner from '@/components/common/PlayStoreInstallBanner';
import FloatingMiniPlayer from '@/components/player/FloatingMiniPlayer';
import LiveScoreTicker from '@/components/layout/LiveScoreTicker';
import QuickCommandPalette from '@/components/search/QuickCommandPalette';
import QueryProvider from '@/components/providers/QueryProvider';

interface AppShellProps {
  children: React.ReactNode;
}

export default function AppShell({ children }: AppShellProps) {
  const pathname = usePathname();
  const isTelemetry = pathname?.startsWith('/telemetry') ?? false;

  if (isTelemetry) {
    return (
      <QueryProvider>
        <div className="min-h-screen w-full bg-slate-950 text-slate-100 flex flex-col">
          <main className="flex-1 w-full">{children}</main>
        </div>
      </QueryProvider>
    );
  }

  return (
    <QueryProvider>
      <div className="min-h-screen w-full bg-[#101418] text-[#F1F5F9] flex flex-col md:flex-row">
      <SplashScreen />

      {/* Left Side Navigation Rail */}
      <Suspense fallback={<div className="w-60 lg:w-64 bg-[#101418] hidden md:block" />}>
        <Sidebar />
      </Suspense>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0 pb-28 md:pb-0 relative">
        <LiveScoreTicker />
        <main className="flex-1">{children}</main>
        <Footer />
        <QuickCommandPalette />
        <CookieConsentBanner />
        <PlayStoreInstallBanner />
      </div>

      {/* Floating Docked Mini-Player */}
      <FloatingMiniPlayer />

      {/* Mobile Bottom Floating Navigation Bar */}
      <BottomNav />
    </div>
    </QueryProvider>
  );
}
