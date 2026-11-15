'use client';

import React, { useState } from 'react';
import TacticalZone14OverloadEngine from './TacticalZone14OverloadEngine';
import TacticalDuelsMomentum from './TacticalDuelsMomentum';
import H2HComparator from '@/components/tools/H2HComparator';
import LeagueFormMatrix from '@/components/tools/LeagueFormMatrix';
import BroadcastDirectory from '@/components/tools/BroadcastDirectory';
import { Network, Swords, Activity, Tv, Sparkles } from 'lucide-react';

type ToolTab = 'spatial' | 'duels' | 'h2h' | 'form' | 'broadcast';

export default function ToolsTabsClient() {
  const [activeTab, setActiveTab] = useState<ToolTab>('spatial');

  const tabs = [
    { id: 'spatial' as ToolTab, label: 'Zone 14 Overload Engine', icon: Network },
    { id: 'duels' as ToolTab, label: 'Tactical Duels & Momentum', icon: Activity },
    { id: 'h2h' as ToolTab, label: 'H2H Comparator', icon: Swords },
    { id: 'form' as ToolTab, label: 'League Form Matrix', icon: Sparkles },
    { id: 'broadcast' as ToolTab, label: 'Broadcast Directory', icon: Tv },
  ];

  return (
    <div className="space-y-6">
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
                  ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/40 shadow-sm'
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
        {activeTab === 'h2h' && <H2HComparator />}
        {activeTab === 'form' && <LeagueFormMatrix />}
        {activeTab === 'broadcast' && <BroadcastDirectory />}
      </div>
    </div>
  );
}
