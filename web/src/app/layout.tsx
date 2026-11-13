import React from 'react';
import type { Metadata } from 'next';
import AppShell from '@/components/layout/AppShell';
import AdSenseScript from '@/components/common/AdSenseScript';
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
  return (
    <html lang="en" className="dark">
      <head>
        {/* Early Theme Sync */}
        <script
          dangerouslySetInnerHTML={{
            __html: `(function(){try{var t=localStorage.getItem('fc-theme');if(t==='light'){document.documentElement.classList.remove('dark');document.documentElement.classList.add('light');}}catch(e){}})()`,
          }}
        />
        {/* Official Google AdSense Account Verification */}
        <meta name="google-adsense-account" content="ca-pub-6717632447198427" />
        <script
          async
          src="https://pagead2.googlesyndication.com/pagead/js/adsbygoogle.js?client=ca-pub-6717632447198427"
          crossOrigin="anonymous"
        />
        {/* Google Identity Services (Client-side in-app login) */}
        <script
          async
          src="https://accounts.google.com/gsi/client"
        />
      </head>
      <body className="min-h-screen bg-[#101418] text-[#F1F5F9] antialiased selection:bg-[#7098F8]/30 selection:text-white">
        <AdSenseScript />
        <AppShell>{children}</AppShell>
      </body>
    </html>
  );
}
