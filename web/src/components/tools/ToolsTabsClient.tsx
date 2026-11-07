'use client';

import React, { useState } from 'react';
import H2HComparator from '@/components/tools/H2HComparator';
import LeagueFormMatrix from '@/components/tools/LeagueFormMatrix';
import BroadcastDirectory from '@/components/tools/BroadcastDirectory';
import { Swords, Activity, Tv } from 'lucide-react';

type ToolTab = 'h2h' | 'form' | 'broadcast';

export default function ToolsTabsClient() {
  const [activeTab, setActiveTab] = useState<ToolTab>('h2h');

  const tabs = [
    { id: 'h2h' as ToolTab, label: 'H2H Tactical Comparator', icon: Swords },
    { id: 'form' as ToolTab, label: 'Form & Momentum Matrix', icon: Activity },
    { id: 'broadcast' as ToolTab, label: 'Global Where-to-Watch', icon: Tv },
  ];

  return (
    <div className="space-y-6">
      {/* Tab Switcher */}
      <div className="flex gap-2 p-1.5 rounded-2xl bg-[#181C20] border border-[#334155]/60 overflow-x-auto scrollbar-none">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const isActive = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              type="button"
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-4 py-2.5 rounded-xl text-xs sm:text-sm font-extrabold transition-all cursor-pointer whitespace-nowrap ${
                isActive
                  ? 'bg-[#7098F8] text-[#101418] shadow-md shadow-[#7098F8]/20'
                  : 'text-[#94A3B8] hover:text-white hover:bg-white/5'
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
        {activeTab === 'h2h' && <H2HComparator />}
        {activeTab === 'form' && <LeagueFormMatrix />}
        {activeTab === 'broadcast' && <BroadcastDirectory />}
      </div>
    </div>
  );
}
