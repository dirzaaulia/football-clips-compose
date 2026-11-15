'use client';

import React, { useMemo } from 'react';
import { ChevronLeft, ChevronRight, Calendar as CalendarIcon } from 'lucide-react';

interface FixturesDateCarouselProps {
  selectedDate: string;
  onSelectDate: (date: string) => void;
  onPrevDay: () => void;
  onNextDay: () => void;
  matchCountsByDate?: Record<string, { total: number; live: number; finished: number }>;
}

export default function FixturesDateCarousel({
  selectedDate,
  onSelectDate,
  onPrevDay,
  onNextDay,
  matchCountsByDate = {},
}: FixturesDateCarouselProps) {
  // Generate a window of 7 days centered or anchored around selectedDate
  const days = useMemo(() => {
    const list = [];
    const base = new Date(selectedDate);
    // 3 days before, selected day, 3 days after
    for (let i = -3; i <= 3; i++) {
      const d = new Date(base);
      d.setDate(d.getDate() + i);
      const iso = d.toISOString().split('T')[0];
      const isToday = iso === new Date().toISOString().split('T')[0];
      const dayName = d.toLocaleDateString('en-GB', { weekday: 'short' });
      const dayNum = d.getDate();
      list.push({ iso, isToday, dayName, dayNum });
    }
    return list;
  }, [selectedDate]);

  const formattedHeader = useMemo(() => {
    return new Date(selectedDate).toLocaleDateString('en-GB', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
  }, [selectedDate]);

  return (
    <div className="w-full flex flex-col gap-3">
      {/* Date Navigation Bar */}
      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <CalendarIcon className="w-4 h-4 text-[#DFFF00]" />
          <span className="font-tech text-xs sm:text-sm font-bold text-white uppercase tracking-wider">
            {formattedHeader}
          </span>
        </div>

        <div className="flex items-center gap-1.5">
          <button
            onClick={onPrevDay}
            className="w-8 h-8 rounded-lg bg-[#1A1C22] hover:bg-[#252830] border border-white/10 text-white/70 hover:text-white flex items-center justify-center transition-colors cursor-pointer"
            title="Previous Day"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>
          <button
            onClick={onNextDay}
            className="w-8 h-8 rounded-lg bg-[#1A1C22] hover:bg-[#252830] border border-white/10 text-white/70 hover:text-white flex items-center justify-center transition-colors cursor-pointer"
            title="Next Day"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Date Carousel Pills */}
      <div className="grid grid-cols-7 gap-2 overflow-x-auto pb-1 scrollbar-none">
        {days.map((d) => {
          const isSelected = d.iso === selectedDate;
          const info = matchCountsByDate[d.iso];
          const badgeText = info
            ? info.live > 0
              ? `${info.live} LIVE`
              : info.finished > 0
              ? `${info.finished} FT`
              : `${info.total} FIX`
            : d.isToday
            ? 'Today'
            : '';

          return (
            <button
              key={d.iso}
              onClick={() => onSelectDate(d.iso)}
              className={`min-h-[56px] py-2 px-1.5 rounded-xl border flex flex-col items-center justify-center transition-all cursor-pointer ${
                isSelected
                  ? 'bg-[#1A1C22] border-[#DFFF00] shadow-[0_0_12px_rgba(223,255,0,0.2)]'
                  : 'bg-[#121316] border-white/10 hover:border-white/20 hover:bg-[#1A1C22]/60'
              }`}
            >
              <span
                className={`text-[10px] font-tech uppercase tracking-wider font-semibold ${
                  isSelected ? 'text-[#DFFF00]' : 'text-white/50'
                }`}
              >
                {d.dayName}
              </span>
              <span
                className={`font-scoreboard text-lg sm:text-xl leading-none my-0.5 ${
                  isSelected ? 'text-white' : 'text-white/80'
                }`}
              >
                {d.dayNum}
              </span>
              {badgeText && (
                <span
                  className={`text-[9px] font-tech font-bold uppercase tracking-wider px-1 rounded ${
                    isSelected
                      ? 'bg-[#DFFF00] text-[#0A0A0C]'
                      : info?.live
                      ? 'bg-[#FF2E4D] text-white'
                      : 'text-white/40'
                  }`}
                >
                  {badgeText}
                </span>
              )}
            </button>
          );
        })}
      </div>
    </div>
  );
}
