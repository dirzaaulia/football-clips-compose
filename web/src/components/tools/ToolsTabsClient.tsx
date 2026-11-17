'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import TacticalZone14OverloadEngine from './TacticalZone14OverloadEngine';
import TacticalDuelsMomentum from './TacticalDuelsMomentum';
import H2HComparator from '@/components/tools/H2HComparator';
import LeagueFormMatrix from '@/components/tools/LeagueFormMatrix';
import BroadcastDirectory from '@/components/tools/BroadcastDirectory';
import { Network, Swords, Activity, Tv, Sparkles, ArrowLeft } from 'lucide-react';

type ToolTab = 'spatial' | 'duels' | 'h2h' | 'form' | 'broadcast';

export default function ToolsTabsClient() {
  const [activeTab, setActiveTab] = useState<ToolTab>('spatial');
  const [deepLink, setDeepLink] = useState<{
    matchId?: string | null;
    home?: string | null;
    away?: string | null;
    comp?: string | null;
  }>({});

  useEffect(() => {
    if (typeof window === 'undefined') return;
    const params = new URLSearchParams(window.location.search);
    const tabParam = params.get('tab') as ToolTab | null;
    const matchId = params.get('matchId');
    const home = params.get('home');
    const away = params.get('away');
    const comp = params.get('comp');

    const timer = setTimeout(() => {
      if (tabParam && ['spatial', 'duels', 'h2h', 'form', 'broadcast'].includes(tabParam)) {
        setActiveTab(tabParam);
      } else if (home || away || comp) {
        setActiveTab('h2h');
      }
      setDeepLink({ matchId, home, away, comp });
    }, 0);
    return () => clearTimeout(timer);
  }, []);

  const tabs = [
    { id: 'spatial' as ToolTab, label: 'Attacking Zones & Pitch Control', icon: Network },
    { id: 'duels' as ToolTab, label: 'Player Duels & Momentum', icon: Activity },
    { id: 'h2h' as ToolTab, label: 'H2H & Club Comparator', icon: Swords },
    { id: 'form' as ToolTab, label: 'League Form Matrix', icon: Sparkles },
    { id: 'broadcast' as ToolTab, label: 'Broadcast Directory', icon: Tv },
  ];

  return (
    <div className="space-y-6">
      {/* Contextual Return Banner when deep-linked from a specific Match Centre */}
      {deepLink.matchId && (
        <div className="flex flex-wrap items-center justify-between gap-3 px-4 py-3 rounded-xl bg-[#1B1B1F] border border-[#DFFF00]/30 font-tech text-xs">
          <div className="flex items-center gap-2 text-white/80">
            <span className="px-2 py-0.5 rounded bg-[#DFFF00] text-[#0A0A0C] font-extrabold uppercase text-[10px]">
              Match Context Active
            </span>
            <span>
              Comparing{' '}
              <strong className="text-white">
                {deepLink.home || 'Home'} vs {deepLink.away || 'Away'}
              </strong>{' '}
              (#{deepLink.matchId})
            </span>
          </div>
          <Link
            href={`/match/${deepLink.matchId}`}
            className="inline-flex items-center gap-1.5 text-[#DFFF00] hover:underline font-bold"
          >
            <ArrowLeft className="w-3.5 h-3.5" />
            <span>Return to Match Centre</span>
          </Link>
        </div>
      )}

      {/* Tab Switcher */}
      <div className="flex gap-2 p-1.5 rounded-2xl bg-[#121316] border border-white/10 overflow-x-auto scrollbar-none">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl font-tech text-xs uppercase tracking-wider font-bold transition-all cursor-pointer whitespace-nowrap ${
                isActive
                  ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_15px_rgba(223,255,0,0.25)]'
                  : 'text-white/60 hover:text-white hover:bg-white/5'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>

      {/* Tab Content Panes */}
      <div className="animate-in fade-in duration-200">
        {activeTab === 'spatial' && <TacticalZone14OverloadEngine />}
        {activeTab === 'duels' && <TacticalDuelsMomentum />}
        {activeTab === 'h2h' && (
          <H2HComparator
            initialComp={deepLink.comp}
            initialHome={deepLink.home}
            initialAway={deepLink.away}
            initialMatchId={deepLink.matchId}
          />
        )}
        {activeTab === 'form' && <LeagueFormMatrix />}
        {activeTab === 'broadcast' && <BroadcastDirectory />}
      </div>
    </div>
  );
}
