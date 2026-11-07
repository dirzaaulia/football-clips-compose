import React from 'react';
import type { Metadata } from 'next';
import ToolsTabsClient from '@/components/tools/ToolsTabsClient';
import { Wrench, ArrowLeft } from 'lucide-react';
import Link from 'next/link';

export const metadata: Metadata = {
  title: 'Football Analytics & Tactical Tools Suite - H2H Comparator, Form Matrix & Broadcasts',
  description:
    'Interactive European football tools: Head-to-Head Tactical Comparator, Live League Form & Clean Sheet Matrix, and Global Where-to-Watch Broadcast Directory.',
  alternates: {
    canonical: 'https://fc.dirzaaulia.com/tools',
  },
  openGraph: {
    title: 'Football Analytics & Tactical Tools Suite',
    description: 'Compare club tactical profiles, analyze league form momentum, and search official broadcasters.',
    url: 'https://fc.dirzaaulia.com/tools',
    type: 'website',
  },
};

export default function ToolsPage() {
  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'WebApplication',
    name: 'Football Clips Analytics & Tactical Tools',
    url: 'https://fc.dirzaaulia.com/tools',
    applicationCategory: 'SportsApplication',
    operatingSystem: 'All',
    description: 'Head-to-Head club tactical comparator, league form matrix, and global broadcast guide.',
  };

  return (
    <div className="max-w-[1400px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      <script
        type="application/ld+json"
        dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }}
      />

      {/* Header */}
      <div className="space-y-3">
        <Link
          href="/"
          className="inline-flex items-center gap-1.5 text-xs font-bold text-[#94A3B8] hover:text-white transition-colors mb-1"
        >
          <ArrowLeft className="w-3.5 h-3.5" />
          <span>Back to Match Centre</span>
        </Link>
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-[#7098F8]/10 border border-[#7098F8]/30 flex items-center justify-center text-[#7098F8]">
            <Wrench className="w-5 h-5" />
          </div>
          <div>
            <h1 className="text-2xl sm:text-3xl font-black text-white tracking-tight">
              Football Tactical Tools Suite
            </h1>
            <p className="text-xs sm:text-sm text-[#94A3B8]">
              Authentic tactical comparison models, live league form indicators, and verified broadcaster directories.
            </p>
          </div>
        </div>
      </div>

      {/* Interactive Tabs Client Component */}
      <ToolsTabsClient />
    </div>
  );
}
