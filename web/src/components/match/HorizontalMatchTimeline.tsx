'use client';

import React, { useState } from 'react';
import { MatchEvent } from '@/lib/database';
import { ShieldAlert, Users } from 'lucide-react';

interface HorizontalMatchTimelineProps {
  events?: MatchEvent[] | null;
  homeName: string;
  awayName: string;
  homeColor?: string | null;
  awayColor?: string | null;
  isSpoilerFree?: boolean;
  isLive?: boolean;
}

export default function HorizontalMatchTimeline({
  events,
  homeName,
  awayName,
  homeColor,
  awayColor,
  isSpoilerFree = false,
  isLive = false,
}: HorizontalMatchTimelineProps) {
  const [isRevealed, setIsRevealed] = useState(!isSpoilerFree || isLive);
  const [hoveredEvent, setHoveredEvent] = useState<MatchEvent | null>(null);

  const effectiveHomeColor = homeColor || '#DFFF00';
  const effectiveAwayColor = awayColor || '#38BDF8';

  if (!events || events.length === 0) {
    return (
      <div className="py-6 text-center text-xs font-tech text-white/50 bg-[#121316] rounded-2xl border border-white/10">
        No match events recorded yet for this fixture.
      </div>
    );
  }

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
      <div className="py-6 text-center text-xs font-tech text-white/50 bg-[#121316] rounded-2xl border border-white/10">
        No goals, cards, or key events recorded.
      </div>
    );
  }

  if (isSpoilerFree && !isLive && !isRevealed) {
    return (
      <div
        onClick={() => setIsRevealed(true)}
        className="p-5 bg-[#121316] border border-[#DFFF00]/30 hover:border-[#DFFF00] rounded-2xl cursor-pointer transition-all flex items-center justify-between group shadow-lg select-none"
      >
        <div className="flex items-center gap-3">
          <ShieldAlert className="w-5 h-5 text-[#DFFF00] group-hover:scale-110 transition-transform" />
          <div>
            <div className="text-xs font-tech font-bold text-white uppercase tracking-wider">Interactive Match Timeline Protected</div>
            <div className="text-[11px] font-tech text-white/50">Tap to reveal chronological goals, cards and events</div>
          </div>
        </div>
        <span className="px-4 py-2 bg-[#DFFF00] hover:bg-[#cbe800] text-[#0A0A0C] font-tech text-xs font-black uppercase tracking-wider rounded-xl shadow-md">
          Reveal Timeline ▾
        </span>
      </div>
    );
  }

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
    <div className="p-4 sm:p-5 bg-[#121316] rounded-2xl border border-white/10 space-y-4 sm:space-y-6 shadow-xl overflow-hidden select-none">
      {/* Header Info */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between text-xs pb-3 border-b border-white/10 gap-2">
        <div className="flex items-center gap-2">
          <span className="font-scoreboard text-sm uppercase tracking-wider text-white">
            Match Timeline
          </span>
          <span className="text-[10px] text-white/40 sm:hidden font-tech">
            (Swipe horizontally ↔)
          </span>
        </div>
        <div className="flex items-center gap-3 sm:gap-4 text-[11px] text-white/60 font-tech">
          <span className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full" style={{ backgroundColor: effectiveHomeColor }} /> {homeName} (Top)</span>
          <span className="flex items-center gap-1.5"><span className="w-2 h-2 rounded-full" style={{ backgroundColor: effectiveAwayColor }} /> {awayName} (Bottom)</span>
        </div>
      </div>

      {/* Horizontal Pitch Axis */}
      <div className="overflow-x-auto scrollbar-none py-1 -mx-2 px-2 sm:mx-0 sm:px-0">
        <div className="relative min-w-[540px] sm:min-w-full pt-12 pb-14 px-6 sm:px-8">
          <div className="relative h-2 w-full bg-[#1A1C22] rounded-full overflow-visible border border-white/10">
            <div className="absolute left-[50%] -top-2 bottom-0 w-0.5 h-6 bg-white/20 -translate-x-1/2">
              <span className="absolute -bottom-5 -translate-x-1/2 text-[10px] font-mono font-bold text-white/60">HT</span>
            </div>

            <span className="absolute -left-2 -bottom-5 text-[10px] font-mono font-bold text-white/40">0&apos;</span>
            <span className="absolute -right-2 -bottom-5 text-[10px] font-mono font-bold text-white/40">90&apos;+</span>

            {filteredEvents.map((e, idx) => {
              const minute = parseMinute(e.clock);
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
                  <div
                    className="absolute left-1/2 -translate-x-1/2 w-0.5"
                    style={{
                      height: isHome ? '1rem' : '0.75rem',
                      top: isHome ? '1.5rem' : '-0.5rem',
                      backgroundColor: `${isHome ? effectiveHomeColor : effectiveAwayColor}99`,
                    }}
                  />

                  <div
                    className={`flex items-center gap-1 px-2 py-0.5 rounded-full text-[10px] font-tech font-bold shadow-md border ${badge.color}`}
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

      {/* Active Inspection Drawer */}
      <div className="min-h-[44px] px-4 py-2.5 rounded-xl bg-[#0A0A0C] border border-white/10 flex flex-col sm:flex-row sm:items-center justify-between text-xs gap-2">
        {hoveredEvent ? (
          <div className="flex items-center gap-3 min-w-0">
            <span className="text-base shrink-0">{getEventBadge(hoveredEvent).icon}</span>
            <div className="min-w-0 font-tech">
              <span className="font-extrabold text-white mr-2 truncate">{hoveredEvent.player || hoveredEvent.text}</span>
              <span className="text-white/60 text-[11px] block sm:inline">
                {hoveredEvent.assist ? ` (Assist: ${hoveredEvent.assist})` : ''}
              </span>
            </div>
          </div>
        ) : (
          <div className="flex items-center gap-2 text-white/50 text-[11px] font-tech">
            <Users className="w-3.5 h-3.5 text-[#DFFF00] shrink-0" />
            <span>Tap or hover any event badge on the timeline to inspect player and assist details</span>
          </div>
        )}
        {hoveredEvent && (
          <span className="text-[10px] font-tech font-black text-[#DFFF00] uppercase tracking-wider self-start sm:self-auto shrink-0">
            {getEventBadge(hoveredEvent).label}
          </span>
        )}
      </div>
    </div>
  );
}
