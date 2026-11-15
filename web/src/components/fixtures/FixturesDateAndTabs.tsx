'use client';

import React from 'react';
import {
  Calendar as CalendarIcon,
  ChevronLeft,
  ChevronRight,
  CheckCircle2,
  Clock,
  Radio,
  Trophy,
  Film,
} from 'lucide-react';

export type FixtureTab = 'ALL' | 'LIVE' | 'RESULTS' | 'UPCOMING';

interface FixturesDateAndTabsProps {
  selectedSeason: number;
  activeTab: FixtureTab;
  setActiveTab: (tab: FixtureTab) => void;
  formattedDate: string;
  changeDay: (delta: number) => void;
  archiveFilter: 'ALL' | 'HIGHLIGHTS';
  setArchiveFilter: (filter: 'ALL' | 'HIGHLIGHTS') => void;
  currentOption: { shortLabel?: string; label?: string };
}

export default function FixturesDateAndTabs({
  selectedSeason,
  activeTab,
  setActiveTab,
  formattedDate,
  changeDay,
  archiveFilter,
  setArchiveFilter,
  currentOption,
}: FixturesDateAndTabsProps) {
  if (selectedSeason !== 2026) {
    return (
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 bg-[#121316] border border-[#DFFF00]/30 rounded-2xl p-2.5 mb-4 select-none">
        <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar">
          <button
            onClick={() => setArchiveFilter('ALL')}
            className={`px-3.5 py-1.5 rounded-xl font-tech text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
              archiveFilter === 'ALL'
                ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_12px_rgba(223,255,0,0.3)]'
                : 'text-white/60 hover:text-white hover:bg-white/5'
            }`}
          >
            All Matches
          </button>
          <button
            onClick={() => setArchiveFilter('HIGHLIGHTS')}
            className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-xl font-tech text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
              archiveFilter === 'HIGHLIGHTS'
                ? 'bg-[#FF2E4D] text-white shadow-[0_0_12px_rgba(255,46,77,0.4)]'
                : 'text-white/60 hover:text-white hover:bg-white/5'
            }`}
          >
            <Film className="w-3.5 h-3.5 text-white" />
            <span>With Highlights</span>
          </button>
        </div>

        <div className="flex items-center gap-2 bg-[#DFFF00]/10 border border-[#DFFF00]/30 rounded-xl px-3 py-1.5 font-tech text-xs font-bold text-[#DFFF00]">
          <Trophy className="w-3.5 h-3.5 text-[#DFFF00]" />
          <span>Archive Season {currentOption.shortLabel || currentOption.label}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 bg-[#121316] border border-white/10 rounded-2xl p-2 mb-4 select-none">
      {/* Category / Status Filter Tabs */}
      <div className="flex items-center gap-1 bg-[#0A0A0C] p-1 rounded-xl overflow-x-auto no-scrollbar border border-white/5">
        <button
          onClick={() => setActiveTab('ALL')}
          className={`px-3.5 py-1.5 rounded-lg font-tech text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
            activeTab === 'ALL'
              ? 'bg-[#DFFF00] text-[#0A0A0C] shadow-[0_0_10px_rgba(223,255,0,0.4)]'
              : 'text-white/60 hover:text-white hover:bg-white/5'
          }`}
        >
          All
        </button>
        <button
          onClick={() => setActiveTab('LIVE')}
          className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg font-tech text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
            activeTab === 'LIVE'
              ? 'bg-[#FF2E4D] text-white shadow-[0_0_10px_rgba(255,46,77,0.4)]'
              : 'text-white/60 hover:text-white hover:bg-white/5'
          }`}
        >
          <Radio className="w-3.5 h-3.5 text-white animate-pulse" />
          <span>Live</span>
        </button>
        <button
          onClick={() => setActiveTab('RESULTS')}
          className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg font-tech text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
            activeTab === 'RESULTS'
              ? 'bg-[#1A1C22] text-[#DFFF00] border border-[#DFFF00]/40'
              : 'text-white/60 hover:text-white hover:bg-white/5'
          }`}
        >
          <CheckCircle2 className="w-3.5 h-3.5 text-[#DFFF00]" />
          <span>Results</span>
        </button>
        <button
          onClick={() => setActiveTab('UPCOMING')}
          className={`flex items-center gap-1.5 px-3.5 py-1.5 rounded-lg font-tech text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
            activeTab === 'UPCOMING'
              ? 'bg-[#1A1C22] text-[#38BDF8] border border-[#38BDF8]/40'
              : 'text-white/60 hover:text-white hover:bg-white/5'
          }`}
        >
          <Clock className="w-3.5 h-3.5 text-[#38BDF8]" />
          <span>Upcoming</span>
        </button>
      </div>

      {/* Date Stepper */}
      <div className="flex items-center justify-between sm:justify-end gap-1.5 bg-[#0A0A0C] rounded-xl px-2 py-1 border border-white/10">
        <button
          onClick={() => changeDay(-1)}
          aria-label="Previous day"
          className="p-1.5 rounded-lg text-white/50 hover:text-white hover:bg-white/5 transition-all cursor-pointer"
        >
          <ChevronLeft className="w-4 h-4" />
        </button>
        <div className="flex items-center gap-1.5 px-2 font-tech text-xs font-bold text-white">
          <CalendarIcon className="w-3.5 h-3.5 text-[#DFFF00]" />
          <span>{formattedDate}</span>
        </div>
        <button
          onClick={() => changeDay(1)}
          aria-label="Next day"
          className="p-1.5 rounded-lg text-white/50 hover:text-white hover:bg-white/5 transition-all cursor-pointer"
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>
    </div>
  );
}
