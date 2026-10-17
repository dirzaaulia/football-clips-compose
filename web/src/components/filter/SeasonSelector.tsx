'use client';

import React from 'react';
import { Calendar, ChevronDown, Archive } from 'lucide-react';
import { useSeason, AVAILABLE_SEASONS } from '@/hooks/useSeason';

interface SeasonSelectorProps {
  variant?: 'pill' | 'select' | 'compact';
  className?: string;
}

export default function SeasonSelector({
  variant = 'select',
  className = '',
}: SeasonSelectorProps) {
  const { selectedSeason, setSeason, isArchiveSeason } = useSeason();

  if (variant === 'pill') {
    return (
      <div className={`flex items-center bg-[#181C20] border border-[#334155] p-1 rounded-xl shadow-sm ${className}`}>
        {AVAILABLE_SEASONS.map((s) => {
          const isSelected = s.year === selectedSeason;
          return (
            <button
              key={s.year}
              type="button"
              onClick={() => setSeason(s.year)}
              className={`px-3 py-1.5 rounded-lg text-xs font-bold transition-all cursor-pointer flex items-center gap-1.5 ${
                isSelected
                  ? s.isCurrent
                    ? 'bg-gradient-to-r from-[#1E3A8A] to-[#7098F8] text-white shadow-sm'
                    : 'bg-gradient-to-r from-amber-600 to-amber-500 text-white shadow-sm'
                  : 'text-[#94A3B8] hover:text-white'
              }`}
            >
              {!s.isCurrent && <Archive className="w-3 h-3" />}
              <span>{s.label}</span>
            </button>
          );
        })}
      </div>
    );
  }

  if (variant === 'compact') {
    return (
      <div
        className={`relative inline-flex items-center gap-1.5 bg-[#181C20] border ${
          isArchiveSeason ? 'border-amber-500/50 bg-amber-500/10' : 'border-[#334155]'
        } hover:border-[#7098F8]/60 rounded-xl px-2.5 py-1.5 transition-all shadow-sm ${className}`}
      >
        <Calendar className={`w-3.5 h-3.5 shrink-0 ${isArchiveSeason ? 'text-amber-400' : 'text-[#7098F8]'}`} />
        <select
          aria-label="Select season"
          value={selectedSeason}
          onChange={(e) => setSeason(Number(e.target.value))}
          className={`bg-transparent text-xs font-bold focus:outline-none cursor-pointer pr-4 appearance-none ${
            isArchiveSeason ? 'text-amber-300' : 'text-white'
          }`}
        >
          {AVAILABLE_SEASONS.map((s) => (
            <option key={s.year} value={s.year} className="bg-[#181C20] text-white">
              {s.shortLabel} {s.isCurrent ? '• Active' : '• Archive'}
            </option>
          ))}
        </select>
        <ChevronDown className="w-3 h-3 text-[#94A3B8] absolute right-2 pointer-events-none" />
      </div>
    );
  }

  // Default 'select' variant
  return (
    <div
      className={`relative inline-flex items-center gap-2 bg-[#181C20] border ${
        isArchiveSeason
          ? 'border-amber-500/50 bg-amber-500/5 shadow-[0_0_12px_rgba(245,158,11,0.15)]'
          : 'border-[#334155] hover:border-[#7098F8]/50'
      } rounded-2xl px-3.5 py-2 transition-all shadow-md group ${className}`}
    >
      <div className="flex items-center gap-1.5 shrink-0">
        {isArchiveSeason ? (
          <Archive className="w-4 h-4 text-amber-400 group-hover:scale-110 transition-transform" />
        ) : (
          <Calendar className="w-4 h-4 text-[#7098F8] group-hover:scale-110 transition-transform" />
        )}
        <span className="text-xs text-[#94A3B8] font-medium hidden sm:inline">Season:</span>
      </div>
      <div className="relative flex items-center">
        <select
          aria-label="Select season"
          value={selectedSeason}
          onChange={(e) => setSeason(Number(e.target.value))}
          className={`bg-transparent text-xs sm:text-sm font-bold focus:outline-none cursor-pointer pr-5 appearance-none ${
            isArchiveSeason ? 'text-amber-300' : 'text-white'
          }`}
        >
          {AVAILABLE_SEASONS.map((s) => (
            <option key={s.year} value={s.year} className="bg-[#181C20] text-white">
              {s.label}
            </option>
          ))}
        </select>
        <ChevronDown className="w-3.5 h-3.5 text-[#94A3B8] absolute right-0 pointer-events-none group-hover:text-white transition-colors" />
      </div>
    </div>
  );
}
