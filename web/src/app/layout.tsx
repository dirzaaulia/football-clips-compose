import React, { Suspense } from 'react';
import type { Metadata } from 'next';
import Sidebar from '@/components/layout/Sidebar';
import BottomNav from '@/components/layout/BottomNav';
import Footer from '@/components/layout/Footer';
import SplashScreen from '@/components/common/SplashScreen';
import CookieConsentBanner from '@/components/common/CookieConsentBanner';
import PlayStoreInstallBanner from '@/components/common/PlayStoreInstallBanner';
import FloatingMiniPlayer from '@/components/player/FloatingMiniPlayer';
import './globals.css';

export const metadata: Metadata = {
  title: 'Football Highlights & Clips - Official Match Highlights, Live Scores & Schedules',
  description:
    'Watch verified official football match highlights, decisive goals, live scores, fixtures, and standings for Premier League, UEFA Champions League, La Liga, Serie A, and Bundesliga.',
  keywords: [
    'football highlights',
    'soccer clips',
    'match recaps',
    'tactical analysis',
    'Premier League',
    'Champions League',
    'La Liga',
    'Serie A',
    'Bundesliga',
  ],
  metadataBase: new URL('https://fc.dirzaaulia.com'),
  alternates: {
    canonical: 'https://fc.dirzaaulia.com/',
  },
  openGraph: {
    title: 'Football Highlights & Clips - Official Highlights & Tactical Analysis',
    description: 'Watch verified official European football match highlights, live scores, and tactical recaps.',
    url: 'https://fc.dirzaaulia.com',
    siteName: 'Football Highlights & Clips',
    type: 'website',
  },
  icons: {
    icon: '/favicon.png',
    shortcut: '/favicon-32x32.png',
    apple: '/apple-touch-icon.png',
  },
  robots: {
    index: true,
    follow: true,
    googleBot: {
      index: true,
      follow: true,
      'max-video-preview': -1,
      'max-image-preview': 'large',
      'max-snippet': -1,
    },
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  const adsenseClientId = process.env.NEXT_PUBLIC_ADSENSE_CLIENT_ID || 'ca-pub-6717632447198427';

  return (
    <html lang="en" className="dark">
      <head>
        {/* Google AdSense Official Verification Snippet */}
        <script
          async
          src={`https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=${adsenseClientId}`}
          crossOrigin="anonymous"
        />
        {/* Google Identity Services (Client-side in-app login) */}
        <script
          async
          src="https://accounts.google.com/gsi/client"
        />
      </head>
      <body className="min-h-screen bg-[#101418] text-[#F1F5F9] antialiased flex flex-col md:flex-row selection:bg-[#7098F8]/30 selection:text-white">
        <SplashScreen />
        {/* Left Side Navigation Rail */}
        <Suspense fallback={<div className="w-64 bg-[#101418] hidden md:block" />}>
          <Sidebar />
        </Suspense>

        {/* Main Content Area */}
        <div className="flex-1 flex flex-col min-w-0 pb-28 md:pb-0 relative">
          <main className="flex-1">{children}</main>
          <Footer />
          {/* Cookie & GDPR Consent Banner (Positioned within content area) */}
          <CookieConsentBanner />
          {/* Google Play Store Android App Install Prompt */}
          <PlayStoreInstallBanner />
        </div>

        {/* Floating Docked Mini-Player (Bottom-Right) */}
        <FloatingMiniPlayer />

        {/* Mobile Navigation Dock */}
        <BottomNav />
      </body>
    </html>
  );
}
