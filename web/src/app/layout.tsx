import React from 'react';
import type { Metadata } from 'next';
import { Bebas_Neue, Space_Grotesk, Inter } from 'next/font/google';
import AppShell from '@/components/layout/AppShell';
import './globals.css';

const bebasNeue = Bebas_Neue({
  weight: '400',
  subsets: ['latin'],
  variable: '--font-bebas',
  display: 'swap',
});

const spaceGrotesk = Space_Grotesk({
  subsets: ['latin'],
  variable: '--font-space',
  display: 'swap',
});

const inter = Inter({
  subsets: ['latin'],
  variable: '--font-inter',
  display: 'swap',
});

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
    <html lang="en" className={`dark ${bebasNeue.variable} ${spaceGrotesk.variable} ${inter.variable}`}>
      <head>
        {/* Official Native Google Play Smart App Banner & Android App Link */}
        <meta name="google-play-app" content="app-id=com.dirzaaulia.footballclips" />
        <link rel="alternate" href="android-app://com.dirzaaulia.footballclips/https/fc.dirzaaulia.com" />
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
      <body className="min-h-screen bg-[#0A0A0C] text-[#E3E2E6] font-body-ott antialiased selection:bg-[#DFFF00]/30 selection:text-white">
        <AppShell>{children}</AppShell>
      </body>
    </html>
  );
}
