'use client';

import React from 'react';
import { Video, Sparkles, History } from 'lucide-react';

export type WaterfallTab = 'overview' | 'candidates' | 'cycles';

interface WaterfallTabsProps {
  activeTab: WaterfallTab;
  onTabChange: (tab: WaterfallTab) => void;
  candidatesCount: number;
  cyclesCount: number;
}

export function WaterfallTabs({
  activeTab,
  onTabChange,
  candidatesCount,
  cyclesCount,
}: WaterfallTabsProps) {
  const tabs = [
    {
      id: 'overview' as const,
      label: 'Overview & Video',
      icon: Video,
      badge: null,
    },
    {
      id: 'candidates' as const,
      label: 'AI & Candidates',
      icon: Sparkles,
      badge: candidatesCount > 0 ? candidatesCount : null,
    },
    {
      id: 'cycles' as const,
      label: 'Cycles & Logs',
      icon: History,
      badge: cyclesCount > 1 ? `${cyclesCount} runs` : null,
    },
  ];

  return (
    <div className="flex items-center gap-1 p-1 bg-slate-900/90 rounded-xl border border-slate-800 text-xs font-semibold">
      {tabs.map((tab) => {
        const Icon = tab.icon;
        const isActive = activeTab === tab.id;
        return (
          <button
            key={tab.id}
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              onTabChange(tab.id);
            }}
            className={`flex-1 py-1.5 px-2.5 rounded-lg text-center transition-all flex items-center justify-center gap-1.5 cursor-pointer ${
              isActive
                ? 'bg-[#1E3A8A] text-white shadow-sm border border-[#7098F8]/40'
                : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800/60 border border-transparent'
            }`}
          >
            <Icon className={`w-3.5 h-3.5 ${isActive ? 'text-[#7098F8]' : 'text-slate-500'}`} />
            <span>{tab.label}</span>
            {tab.badge !== null && (
              <span
                className={`ml-1 px-1.5 py-0.2 rounded-full text-[10px] font-mono font-bold ${
                  isActive
                    ? 'bg-blue-500/30 text-blue-200'
                    : 'bg-slate-800 text-slate-400 border border-slate-700'
                }`}
              >
                {tab.badge}
              </span>
            )}
          </button>
        );
      })}
    </div>
  );
}
