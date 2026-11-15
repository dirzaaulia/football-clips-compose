import React from 'react';
import type { Metadata } from 'next';
import ToolsTabsClient from '@/components/tools/ToolsTabsClient';
import { Wrench, ArrowLeft, Cpu } from 'lucide-react';
import Link from 'next/link';

export const metadata: Metadata = {
  title: 'Football Analytics & Tactical Tools Suite - H2H Comparator, Form Matrix & Broadcasts',
  description:
    'Interactive European football tools: Zone 14 Overload Engine, Head-to-Head Tactical Comparator, Live League Form, and Global Broadcast Directory.',
  alternates: {
    canonical: 'https://footballclips.web.app/tools',
  },
};

export default function ToolsPage() {
  return (
    <div className="max-w-[1600px] w-full mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-6 select-none">
      {/* Header */}
      <div className="space-y-2">
        <div className="flex items-center gap-2">
          <span className="px-2.5 py-0.5 rounded bg-[#DFFF00]/15 text-[#DFFF00] font-tech text-[10px] font-bold uppercase tracking-wider border border-[#DFFF00]/30">
            PRO COACH SUITE • TELEMETRY ENGINE
          </span>
          <span className="text-xs text-white/50 font-tech">UCL KO 2026/27</span>
        </div>
        <div className="flex items-center justify-between gap-4">
          <div>
            <h1 className="font-scoreboard text-3xl sm:text-4xl text-white uppercase tracking-wide leading-none">
              Tactical Tools &amp; Match Telemetry
            </h1>
            <p className="text-xs sm:text-sm text-white/60 font-tech mt-1">
              Head-to-head tactical matrix, zone 14 territorial dominance, expected goals progression, and player duels.
            </p>
          </div>
          <Link
            href="/"
            className="hidden sm:inline-flex items-center gap-1.5 px-3 py-1.5 rounded-xl bg-[#1A1C22] border border-white/10 text-white/70 hover:text-white font-tech text-xs transition-colors"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Match Centre</span>
          </Link>
        </div>
      </div>

      {/* Interactive Tabs Client Component */}
      <ToolsTabsClient />
    </div>
  );
}
