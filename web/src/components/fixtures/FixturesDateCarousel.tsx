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
}: FixturesDateCarouselProps) {
  const days = useMemo(() => {
    const list = [];
    const base = new Date(selectedDate);
    const todayIso = new Date().toISOString().split('T')[0];
    for (let i = -2; i <= 2; i++) {
      const d = new Date(base);
      d.setDate(d.getDate() + i);
      const iso = d.toISOString().split('T')[0];
      const isToday = iso === todayIso;
      const dayName = isToday
        ? 'Today'
        : d.toLocaleDateString('en-GB', { weekday: 'short' });
      const dateLabel = d.toLocaleDateString('en-GB', { day: 'numeric', month: 'short' });
      list.push({ iso, isToday, dayName, dateLabel });
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
    <div className="flex flex-wrap items-center justify-between gap-4 bg-[#1B1B1F] rounded-xl p-3 border border-white/5">
      {/* Left: Date Selector with Prev / Next Chevrons */}
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={onPrevDay}
          className="p-2 rounded-lg bg-[#0D0E11] hover:bg-[#292A2E] text-white/70 hover:text-white border border-white/5 transition-colors cursor-pointer"
          title="Previous Day"
        >
          <ChevronLeft className="w-4 h-4" />
        </button>

        <div className="flex items-center gap-2.5 px-3 py-1.5 rounded-lg bg-[#0D0E11] border border-white/10">
          <CalendarIcon className="w-4 h-4 text-[#DFFF00]" />
          <span className="font-tech text-xs sm:text-sm font-bold text-white tracking-wide">
            {formattedHeader}
          </span>
        </div>

        <button
          type="button"
          onClick={onNextDay}
          className="p-2 rounded-lg bg-[#0D0E11] hover:bg-[#292A2E] text-white/70 hover:text-white border border-white/5 transition-colors cursor-pointer"
          title="Next Day"
        >
          <ChevronRight className="w-4 h-4" />
        </button>
      </div>

      {/* Right: 5-Day Horizontal Pill Strip */}
      <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-0.5">
        {days.map((d) => {
          const isSelected = d.iso === selectedDate;
          return (
            <button
              key={d.iso}
              type="button"
              onClick={() => onSelectDate(d.iso)}
              className={`px-3.5 py-1.5 rounded-lg text-center min-w-[82px] transition-all cursor-pointer ${
                isSelected
                  ? 'bg-[#DFFF00] text-[#0A0A0C] font-bold shadow-[0_0_12px_rgba(223,255,0,0.25)]'
                  : 'bg-[#0D0E11] hover:bg-[#292A2E] text-white/60 hover:text-white border border-white/5'
              }`}
            >
              <span
                className={`block text-[10px] font-tech uppercase tracking-wider ${
                  isSelected ? 'text-[#0A0A0C]/80 font-extrabold' : 'text-white/45'
                }`}
              >
                {d.dayName}
              </span>
              <span className="font-tech text-xs font-bold">{d.dateLabel}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
}
