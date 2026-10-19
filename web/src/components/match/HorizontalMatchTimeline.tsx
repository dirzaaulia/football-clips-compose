'use client';

import React, { useState } from 'react';
import { MatchEvent } from '@/lib/database';
import { ShieldAlert, Users } from 'lucide-react';

interface HorizontalMatchTimelineProps {
  events?: MatchEvent[] | null;
  homeName: string;
  awayName: string;
  isSpoilerFree?: boolean;
  isLive?: boolean;
}

export default function HorizontalMatchTimeline({
  events,
  homeName,
  awayName,
  isSpoilerFree = false,
  isLive = false,
}: HorizontalMatchTimelineProps) {
  const [isRevealed, setIsRevealed] = useState(!isSpoilerFree || isLive);
  const [hoveredEvent, setHoveredEvent] = useState<MatchEvent | null>(null);

  if (!events || events.length === 0) {
    return (
      <div className="py-6 text-center text-xs text-[#94A3B8] bg-[#101418] rounded-2xl border border-[#334155]/50">
        No match events recorded yet for this fixture.
      </div>
    );
  }

  // Filter out non-event items like general whistles
  const filteredEvents = events.filter((e) => {
    const t = (e.type || '').toLowerCase();
    const text = (e.text || '').toLowerCase();
    return (
      t.includes('goal') ||
      t.includes('card') ||
      t.includes('sub') ||
      t.includes('penalty') ||
      text.includes('goal') ||
      text.includes('card')
    );
  });

  if (filteredEvents.length === 0) {
    return (
      <div className="py-6 text-center text-xs text-[#94A3B8] bg-[#101418] rounded-2xl border border-[#334155]/50">
        No goals, cards, or key events recorded.
      </div>
    );
  }

  // Spoiler Guard: strictly for finished matches when spoiler mode is ON
  if (isSpoilerFree && !isLive && !isRevealed) {
    return (
      <div
        onClick={() => setIsRevealed(true)}
        className="p-5 bg-[#101418] border border-[#7098F8]/40 hover:border-[#7098F8] rounded-2xl cursor-pointer transition-all flex items-center justify-between group shadow-lg"
      >
        <div className="flex items-center gap-3">
          <ShieldAlert className="w-5 h-5 text-[#4FC3F7] group-hover:scale-110 transition-transform" />
          <div>
            <div className="text-xs font-bold text-white">Interactive Match Timeline Protected</div>
            <div className="text-[11px] text-[#94A3B8]">Tap to reveal chronological goals, cards and events</div>
          </div>
        </div>
        <span className="px-3.5 py-1.5 bg-[#1E3A8A] text-white text-xs font-bold rounded-xl shadow-md">
          Reveal Timeline ▾
        </span>
      </div>
    );
  }

  // Parse clock string into a numeric minute (e.g. "45'+2" -> 47, "78'" -> 78)
  const parseMinute = (clock?: string): number => {
    if (!clock) return 45;
    const clean = clock.replace(/'/g, '').trim();
    if (clean.includes('+')) {
      const parts = clean.split('+');
      return (parseInt(parts[0], 10) || 45) + (parseInt(parts[1], 10) || 1);
    }
    return Math.min(Math.max(parseInt(clean, 10) || 1, 1), 120);
  };

  const getEventBadge = (e: MatchEvent) => {
    const t = (e.type || '').toLowerCase();
    const txt = (e.text || '').toLowerCase();
    if (t.includes('goal') || txt.includes('goal')) {
      return { icon: '⚽', color: 'bg-emerald-500 text-white border-emerald-400', label: 'Goal' };
    }
    if (t.includes('red') || txt.includes('red card')) {
      return { icon: '🟥', color: 'bg-rose-600 text-white border-rose-400', label: 'Red Card' };
    }
    if (t.includes('yellow') || txt.includes('yellow card')) {
      return { icon: '🟨', color: 'bg-amber-400 text-black border-amber-300', label: 'Yellow Card' };
    }
    return { icon: '🔄', color: 'bg-sky-500 text-white border-sky-300', label: 'Sub' };
  };

  return (
    <div className="p-4 sm:p-5 bg-[#101418] rounded-3xl border border-[#334155]/70 space-y-4 sm:space-y-6 shadow-xl overflow-hidden">
      {/* Header Info */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between text-xs pb-3 border-b border-[#334155]/60 gap-2">
        <div className="flex items-center gap-2">
          <span className="font-extrabold text-[#7098F8] uppercase tracking-wider text-[11px] sm:text-xs">
            Match Timeline
          </span>
          <span className="text-[10px] text-[#94A3B8] sm:hidden font-medium">
            (Swipe horizontally ↔)
          </span>
        </div>
        <div className="flex items-center gap-3 sm:gap-4 text-[11px] text-[#94A3B8]">
          <span className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-[#7098F8]" /> {homeName} (Top)</span>
          <span className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full bg-[#4FC3F7]" /> {awayName} (Bottom)</span>
        </div>
      </div>

      {/* Horizontal Pitch Axis with Smooth Mobile Horizontal Scrolling */}
      <div className="overflow-x-auto scrollbar-none py-1 -mx-2 px-2 sm:mx-0 sm:px-0">
        <div className="relative min-w-[540px] sm:min-w-full pt-12 pb-14 px-6 sm:px-8">
          {/* Central Time Bar */}
          <div className="relative h-2 w-full bg-[#1E293B] rounded-full overflow-visible border border-[#334155]">
            {/* Half-time marker 45' */}
            <div className="absolute left-[50%] -top-2 bottom-0 w-0.5 h-6 bg-[#64748B] -translate-x-1/2">
              <span className="absolute -bottom-5 -translate-x-1/2 text-[10px] font-black text-[#94A3B8]">HT</span>
            </div>

            {/* Start and Full-Time labels */}
            <span className="absolute -left-2 -bottom-5 text-[10px] font-black text-[#64748B]">0&apos;</span>
            <span className="absolute -right-2 -bottom-5 text-[10px] font-black text-[#64748B]">90&apos;+</span>

            {/* Render Event Pins along horizontal axis */}
            {filteredEvents.map((e, idx) => {
              const minute = parseMinute(e.clock);
              // Distribute 0' to 95' across 0% to 100%
              const pct = Math.min(Math.max((minute / 95) * 100, 3), 97);
              const isHome = e.side === 'home';
              const badge = getEventBadge(e);

              return (
                <div
                  key={e.id || idx}
                  style={{ left: `${pct}%` }}
                  onClick={() => setHoveredEvent(e)}
                  onMouseEnter={() => setHoveredEvent(e)}
                  onMouseLeave={() => setHoveredEvent(null)}
                  className={`absolute -translate-x-1/2 cursor-pointer z-10 transition-transform hover:scale-125 ${
                    isHome ? '-top-10' : 'top-4'
                  }`}
                >
                  {/* Connecting Pin line */}
                  <div
                    className={`absolute left-1/2 -translate-x-1/2 w-0.5 ${
                      isHome ? 'top-6 h-4 bg-[#7098F8]/60' : '-top-2 h-3 bg-[#4FC3F7]/60'
                    }`}
                  />

                  {/* Event Marker Bubble */}
                  <div
                    className={`flex items-center gap-1 px-1.5 py-0.5 rounded-full text-[10px] font-black shadow-md border ${badge.color}`}
                    title={`${e.clock} • ${e.player || e.text}`}
                  >
                    <span>{badge.icon}</span>
                    <span>{e.clock || `${minute}'`}</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Active Hover / Inspection Drawer */}
      <div className="min-h-[44px] px-3.5 sm:px-4 py-2.5 rounded-2xl bg-[#181C20] border border-[#334155]/60 flex flex-col sm:flex-row sm:items-center justify-between text-xs gap-2">
        {hoveredEvent ? (
          <div className="flex items-center gap-3 min-w-0">
            <span className="text-base shrink-0">{getEventBadge(hoveredEvent).icon}</span>
            <div className="min-w-0">
              <span className="font-extrabold text-white mr-2 truncate">{hoveredEvent.player || hoveredEvent.text}</span>
              <span className="text-[#94A3B8] text-[11px] block sm:inline">
                {hoveredEvent.side === 'home' ? homeName : awayName} • {hoveredEvent.clock}
                {hoveredEvent.assist ? ` (Assist: ${hoveredEvent.assist})` : ''}
              </span>
            </div>
          </div>
        ) : (
          <div className="flex items-center gap-2 text-[#94A3B8] text-[11px]">
            <Users className="w-3.5 h-3.5 text-[#7098F8] shrink-0" />
            <span>Tap or hover any event badge on the timeline to inspect player and assist details</span>
          </div>
        )}
        {hoveredEvent && (
          <span className="text-[10px] font-bold text-[#7098F8] uppercase tracking-wider self-start sm:self-auto shrink-0">
            {getEventBadge(hoveredEvent).label}
          </span>
        )}
      </div>
    </div>
  );
}
