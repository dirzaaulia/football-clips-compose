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
      <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 bg-[#181C20] border border-amber-500/30 rounded-2xl p-2 mb-3">
        <div className="flex items-center gap-1.5 overflow-x-auto scrollbar-none">
          <button
            onClick={() => setArchiveFilter('ALL')}
            className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
              archiveFilter === 'ALL'
                ? 'bg-gradient-to-r from-amber-600 to-amber-500 text-white shadow-sm'
                : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
            }`}
          >
            All Matches
          </button>
          <button
            onClick={() => setArchiveFilter('HIGHLIGHTS')}
            className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
              archiveFilter === 'HIGHLIGHTS'
                ? 'bg-gradient-to-r from-red-600 to-red-500 text-white shadow-sm'
                : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
            }`}
          >
            <Film className="w-3.5 h-3.5 text-red-400" />
            <span>With Highlights</span>
          </button>
        </div>

        <div className="flex items-center gap-2 bg-amber-500/10 border border-amber-500/30 rounded-xl px-3 py-1.5 text-xs font-bold text-amber-400">
          <Trophy className="w-3.5 h-3.5 text-amber-400" />
          <span>Archive Season {currentOption.shortLabel || currentOption.label}</span>
        </div>
      </div>
    );
  }

  return (
    <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-3 bg-[#181C20] border border-[#334155]/60 rounded-2xl p-2 mb-3">
      {/* Category / Status Filter Tabs */}
      <div className="flex items-center gap-1 bg-[#101418] p-1 rounded-xl overflow-x-auto scrollbar-none">
        <button
          onClick={() => setActiveTab('ALL')}
          className={`px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
            activeTab === 'ALL'
              ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
              : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
          }`}
        >
          All
        </button>
        <button
          onClick={() => setActiveTab('LIVE')}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
            activeTab === 'LIVE'
              ? 'bg-gradient-to-r from-red-600 to-rose-500 text-white shadow-[0_0_12px_rgba(239,68,68,0.4)]'
              : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
          }`}
        >
          <Radio className="w-3.5 h-3.5 text-red-400 animate-pulse" />
          <span>Live</span>
        </button>
        <button
          onClick={() => setActiveTab('RESULTS')}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
            activeTab === 'RESULTS'
              ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
              : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
          }`}
        >
          <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
          <span>Results</span>
        </button>
        <button
          onClick={() => setActiveTab('UPCOMING')}
          className={`flex items-center gap-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all cursor-pointer whitespace-nowrap ${
            activeTab === 'UPCOMING'
              ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white border border-[#7098F8]/50 shadow-[0_0_12px_rgba(112,152,248,0.3)]'
              : 'text-[#94A3B8] hover:text-white hover:bg-[#1E2227]'
          }`}
        >
          <Clock className="w-3.5 h-3.5 text-[#4FC3F7]" />
          <span>Upcoming</span>
        </button>
      </div>

      {/* Compact Date Stepper */}
      <div className="flex items-center justify-between sm:justify-end gap-1.5 bg-[#1E2227]/80 rounded-xl px-2 py-1 border border-[#334155]/60">
        <button
          onClick={() => changeDay(-1)}
          aria-label="Previous day"
          className="p-1 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#252B33] transition-all cursor-pointer"
        >
          <ChevronLeft className="w-3.5 h-3.5" />
        </button>
        <div className="flex items-center gap-1.5 px-2 text-xs font-bold text-white">
          <CalendarIcon className="w-3.5 h-3.5 text-[#7098F8]" />
          <span>{formattedDate}</span>
        </div>
        <button
          onClick={() => changeDay(1)}
          aria-label="Next day"
          className="p-1 rounded-lg text-[#94A3B8] hover:text-white hover:bg-[#252B33] transition-all cursor-pointer"
        >
          <ChevronRight className="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  );
}
